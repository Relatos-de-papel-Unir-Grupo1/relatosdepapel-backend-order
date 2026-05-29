package com.relatosdepapel.orders.service;

import com.relatosdepapel.orders.dto.response.CatalogueBookResponse;
import com.relatosdepapel.orders.entity.Order;
import com.relatosdepapel.orders.entity.OrderItem;
import com.relatosdepapel.orders.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final int MAX_RETRIES = 5;
    private static final int RETRY_DELAY_MS = 3000;

    private final OrderRepository orderRepository;
    private final WebClient webClient;

    @Value("${app.seed.orders-count:10}")
    private int ordersToCreate;

    @Override
    public void run(String... args) throws Exception {
        log.info("Verificando si se deben cargar datos de prueba (app.seed.enabled=true)...");

        if (orderRepository.count() > 0) {
            log.info("La base de datos de órdenes ya contiene datos. No se realizará la carga inicial.");
            return;
        }

        log.info("Iniciando carga de datos de prueba para órdenes...");

        try {
            // 1. Obtener libros del microservicio de catálogo
            log.info("Intentando obtener libros del catálogo...");
            List<CatalogueBookResponse.BookData> books = fetchBooksFromCatalogueWithRetry();

            if (books.isEmpty()) {
                log.warn("No se encontraron libros en el catálogo después de varios intentos. No se crearán órdenes de prueba.");
                return;
            }
            log.info("Se obtuvieron {} libros del catálogo.", books.size());

            // 2. Crear un número configurable de órdenes de prueba
            Random random = new Random();
            for (int i = 0; i < ordersToCreate; i++) {
                // Selecciona un libro aleatorio para cada orden
                CatalogueBookResponse.BookData book = books.get(random.nextInt(books.size()));
                long userId = random.nextInt(10) + 1; // IDs de usuario de 1 a 10
                String status = i % 3 == 0 ? "COMPLETED" : (i % 3 == 1 ? "PROCESS" : "CANCELLED");
                createOrder(book, userId, status, random);
            }

            log.info("Carga de {} órdenes de prueba finalizada exitosamente.", ordersToCreate);

        } catch (Exception e) {
            log.error("Error durante la carga de datos de prueba para órdenes. Causa: {}", e.getMessage(), e);
        }
    }

    private List<CatalogueBookResponse.BookData> fetchBooksFromCatalogueWithRetry() {
        return webClient.get()
                    .uri("http://catalogue/api/v1/books")
                    .retrieve()
                    .bodyToFlux(CatalogueBookResponse.BookData.class)
                    .collectList()
                    .retryWhen(Retry.backoff(MAX_RETRIES, Duration.ofMillis(RETRY_DELAY_MS))
                            .doBeforeRetry(retrySignal -> log.warn("No se pudo conectar con el catálogo. Reintentando... (Intento {})", retrySignal.totalRetries() + 1))
                            .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) ->
                                    new IllegalStateException("Servicio de catálogo no disponible después de " + MAX_RETRIES + " intentos.")))
                    .onErrorReturn(Collections.emptyList()) // Si falla después de reintentos, devuelve lista vacía
                    .block();
    }

    private void createOrder(CatalogueBookResponse.BookData book, Long userId, String status, Random random) {
        BigDecimal unitPrice = book.getUnitPrice() != null ? book.getUnitPrice() : BigDecimal.valueOf(25.50);
        int quantity = random.nextInt(3) + 1; // Cantidad entre 1 y 3
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

        Order order = Order.builder()
                .orderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .orderDate(LocalDateTime.now().minusDays(random.nextInt(30)))
                .userId(userId)
                .status(status)
                .total(subtotal)
                .build();

        OrderItem item = OrderItem.builder()
                .bookId(book.getId())
                .quantity(quantity)
                .unitPrice(unitPrice)
                .subtotal(subtotal)
                .build();

        order.addItem(item);
        orderRepository.save(order);
        log.debug("Orden de prueba '{}' creada para el libro ID {} y usuario {}", status, book.getId(), userId);
    }
}