package com.relatosdepapel.orders.service.interfaces;

import com.relatosdepapel.orders.dto.request.CreateOrderRequest;
import com.relatosdepapel.orders.dto.response.OrderResponse;

import java.util.List;

public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest request);
    OrderResponse findById(Long id);
    List<OrderResponse> findByUserId(Long userId);
    List<OrderResponse> findAll();
}
