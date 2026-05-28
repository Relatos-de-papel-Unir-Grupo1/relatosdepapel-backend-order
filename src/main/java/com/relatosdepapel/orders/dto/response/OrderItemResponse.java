package com.relatosdepapel.orders.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {
    private Long id;
    private Long bookId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}
