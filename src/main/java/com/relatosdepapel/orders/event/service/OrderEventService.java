package com.relatosdepapel.orders.event.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.relatosdepapel.orders.entity.Order;
import com.relatosdepapel.orders.entity.OrderItem;
import com.relatosdepapel.orders.event.model.EventHeader;
import com.relatosdepapel.orders.event.model.OrderCreatedEvent;
import com.relatosdepapel.orders.event.model.OrderCreatedEventBody;
import com.relatosdepapel.orders.event.model.OrderItemEvent;
import org.springframework.beans.factory.annotation.Value;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventService {
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.orders}")
    private String ordersExchange;

    @Value("${rabbitmq.routing.key.order.created}")
    private String orderCreatedRoutingKey;

    public void publishOrderCreatedEvent(Order order) {
        try {
            OrderCreatedEvent event = buildOrderCreatedEvent(order);
            rabbitTemplate.convertAndSend(ordersExchange, orderCreatedRoutingKey, event);
            log.info("Evento de pedido creado exitosamente. Order: {}, EventID: {}", order.getId(), event.getHeader().getEventId());

        } catch (Exception e) {
            log.error("Error al publicar el evento de pedido creado. Order: {}, Error: {}", order.getId(), e.getMessage());
        }
    }

    public OrderCreatedEvent buildOrderCreatedEvent(Order order) {

        String eventId = UUID.randomUUID().toString();

        EventHeader header = EventHeader.builder()
                .eventId(eventId)
                .version("1.0")
                .eventType("ORDER_CREATED")
                .timestamp(LocalDateTime.now())
                .build();

        List<OrderItemEvent> orderItems = order.getItems().stream()
                .map(this::mapToOrderItemEvent)
                .toList();

        OrderCreatedEventBody body = OrderCreatedEventBody.builder()
                .orderNumber(order.getOrderNumber())
                .orderDate(order.getOrderDate())
                .total(order.getTotal())
                .status(order.getStatus())
                .userId(order.getUserId())
                .orderItems(orderItems)
                .build();

        return OrderCreatedEvent.builder()
                .header(header)
                .body(body)
                .build();    
    }

    private OrderItemEvent mapToOrderItemEvent(OrderItem item) {
        return OrderItemEvent.builder()
                .idBook(item.getBookId())                
                .quantity(item.getQuantity())
                .subTotal(item.getSubtotal())
                .build();
    }
}
