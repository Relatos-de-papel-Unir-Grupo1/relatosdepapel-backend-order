package com.relatosdepapel.orders.service.impl;

import com.relatosdepapel.orders.dto.request.CreateOrderRequest;
import com.relatosdepapel.orders.dto.request.OrderItemRequest;
import com.relatosdepapel.orders.dto.response.CatalogueBookResponse;
import com.relatosdepapel.orders.dto.response.CatalogueBookResponse.BookData;
import com.relatosdepapel.orders.dto.response.OrderItemResponse;
import com.relatosdepapel.orders.dto.response.OrderResponse;
import com.relatosdepapel.orders.entity.Order;
import com.relatosdepapel.orders.entity.OrderItem;
import com.relatosdepapel.orders.exception.CatalogueCommunicationException;
import com.relatosdepapel.orders.exception.InsufficientStockException;
import com.relatosdepapel.orders.exception.ResourceNotFoundException;
import com.relatosdepapel.orders.repository.OrderRepository;
import com.relatosdepapel.orders.service.interfaces.OrderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final WebClient webClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("[Orders Flow] Iniciando creación de orden para el usuario: {}", request.getUserId());

        // 1. Generar número de orden único y robusto
        String orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .orderDate(LocalDateTime.now())
                .userId(request.getUserId())
                .status("PROCESS")
                .total(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        BigDecimal totalOrder = BigDecimal.ZERO;

        // 2. Procesar cada item secuencialmente dentro de la transacción
        for (OrderItemRequest itemReq : request.getItems()) {
            log.info("[Orders Flow] Validando libro ID: {}, cantidad solicitada: {}", itemReq.getBookId(), itemReq.getQuantity());

            // A. Consultar microservicio de catálogo mediante WebClient LoadBalanced y usando el nombre del servicio Eureka
            BookData catalogueResp;
            try {
                catalogueResp = webClient.get()
                        .uri("http://catalogue/api/v1/books/{id}", itemReq.getBookId())
                        .retrieve()
                        .bodyToMono(BookData.class)
                        .block(); // Bloqueo síncrono para mantener consistencia dentro del flujo de transacción
            } catch (Exception e) {
                log.error("Fallo en la comunicación remota con catalogue-service para ID: {}", itemReq.getBookId(), e);
                throw new CatalogueCommunicationException("Servicio de catálogo fuera de línea temporalmente. Intente más tarde.");
            }

            if (catalogueResp == null) {
                throw new ResourceNotFoundException("El libro con ID " + itemReq.getBookId() + " no está disponible en nuestro catálogo.");
            }

            BookData bookData = catalogueResp;

            // B. Validar stock disponible
            if (bookData.getStock() < itemReq.getQuantity()) {
                throw new InsufficientStockException("El libro '" + bookData.getTitle() 
                        + "' no tiene suficiente stock disponible. Solicitado: " 
                        + itemReq.getQuantity() + ", Disponible: " + bookData.getStock());
            }

            // C. Calcular precio unitario
            BigDecimal unitPrice = bookData.getUnitPrice() != null ? bookData.getUnitPrice() : BigDecimal.valueOf(19.90);
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));

            // D. Descontar stock en el catálogo de forma transaccional remota
            try {
                webClient.patch()
                        .uri("http://catalogue/api/v1/books/{id}", itemReq.getBookId())
                        .bodyValue(Map.of("stock", bookData.getStock() - itemReq.getQuantity())) // Enviar cantidad a descontar
                        .retrieve()
                        .bodyToMono(Void.class)
                        .block();
            } catch (Exception e) {
                log.error("Error al actualizar y decrementar stock remoto para ID: {}", itemReq.getBookId(), e);
                throw new InsufficientStockException("Error al reservar el inventario del libro: '" + bookData.getTitle() + "'.");
            }

            // E. Crear y vincular detalle
            OrderItem orderItem = OrderItem.builder()
                    .bookId(itemReq.getBookId())
                    .quantity(itemReq.getQuantity())
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .build();

            order.addItem(orderItem);
            totalOrder = totalOrder.add(subtotal);
        }

        // 3. Completar y guardar orden
        order.setTotal(totalOrder);
        order.setStatus("COMPLETED");
        
        Order savedOrder = orderRepository.save(order);
        log.info("[Orders Flow] Orden creada exitosamente con número: {} e ID: {}", savedOrder.getOrderNumber(), savedOrder.getId());
        
        return mapToResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada con ID: " + id));
        return mapToResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findByUserId(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findAll() {
        return orderRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private OrderResponse mapToResponse(Order order) {
        if (order == null) {
            return null;
        }

        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .bookId(item.getBookId())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .orderDate(order.getOrderDate())
                .total(order.getTotal())
                .status(order.getStatus())
                .userId(order.getUserId())
                .items(itemResponses)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
