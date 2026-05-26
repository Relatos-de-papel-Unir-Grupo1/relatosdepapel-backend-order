package com.relatosdepapel.orders.controller;

import com.relatosdepapel.orders.dto.request.CreateOrderRequest;
import com.relatosdepapel.orders.dto.response.ApiResponse;
import com.relatosdepapel.orders.dto.response.OrderResponse;
import com.relatosdepapel.orders.service.interfaces.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse order = orderService.createOrder(request);
        ApiResponse<OrderResponse> response = ApiResponse.<OrderResponse>builder()
                .success(true)
                .message("Compra realizada y registrada con éxito")
                .data(order)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> findById(@PathVariable Long id) {
        OrderResponse order = orderService.findById(id);
        ApiResponse<OrderResponse> response = ApiResponse.<OrderResponse>builder()
                .success(true)
                .message("Detalle de la orden de compra recuperado correctamente")
                .data(order)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> findByUserId(@PathVariable Long userId) {
        List<OrderResponse> orders = orderService.findByUserId(userId);
        ApiResponse<List<OrderResponse>> response = ApiResponse.<List<OrderResponse>>builder()
                .success(true)
                .message("Historial de compras del usuario recuperado correctamente")
                .data(orders)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> findAll() {
        List<OrderResponse> orders = orderService.findAll();
        ApiResponse<List<OrderResponse>> response = ApiResponse.<List<OrderResponse>>builder()
                .success(true)
                .message("Listado general de órdenes de compra")
                .data(orders)
                .build();
        return ResponseEntity.ok(response);
    }
}
