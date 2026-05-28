package com.relatosdepapel.orders.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

    @NotNull(message = "El ID de usuario es obligatorio")
    private Long userId;

    @NotEmpty(message = "La orden debe contener al menos un ítem")
    private List<OrderItemRequest> items;
}
