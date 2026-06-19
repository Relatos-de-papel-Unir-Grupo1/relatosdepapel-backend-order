package com.relatosdepapel.orders.event.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderCreatedEventBody {
    private String orderNumber;
    private LocalDateTime orderDate;
    private BigDecimal total;
    private String status;
    private Long userId;
    private List<OrderItemEvent> orderItems;
}
