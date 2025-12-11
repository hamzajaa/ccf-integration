package com.order.service;

import com.order.dto.CreateOrderRequest;
import com.order.dto.OrderResponse;

public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest request);
    OrderResponse getOrder(Long orderId);
    OrderResponse confirmOrder(Long orderId);
    OrderResponse cancelOrder(Long orderId, String reason);
}