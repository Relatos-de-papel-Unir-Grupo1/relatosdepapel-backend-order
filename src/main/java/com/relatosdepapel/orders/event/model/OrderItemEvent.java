package com.relatosdepapel.orders.event.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemEvent {
    private Long idBook;
    private Integer quantity;
    private BigDecimal subTotal;
}