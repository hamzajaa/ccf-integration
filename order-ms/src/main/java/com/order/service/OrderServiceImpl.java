package com.order.service;


import ccf.ccf.verification.MonitorConsistency;
import com.order.ccf.OrderCcfIntegration;
import com.order.dto.CreateOrderRequest;
import com.order.dto.OrderResponse;
import com.order.event.OrderCancelledEvent;
import com.order.event.OrderConfirmedEvent;
import com.order.event.OrderCreatedEvent;
import com.order.kafka.OrderEventProducer;
import com.order.model.Order;
import com.order.model.OrderItem;
import com.order.model.OrderStatus;
import com.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventProducer eventProducer;
    private final OrderCcfIntegration ccfIntegration;

    @Override
    @Transactional
    @MonitorConsistency(contractId = "OrderPaymentConsistency")
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order for customer: {}", request.getCustomerId());

        BigDecimal total = request.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (!total.equals(request.getTotalPrice())) {
            log.error("Contract violation: TOTAL_MATCH invariant failed");
            throw new RuntimeException("Expected: " + total + " , " + "Got: " + request.getTotalPrice());
        }

        Order order = Order.builder()
                .customerId(request.getCustomerId())
                .totalAmount(total)
                .status(OrderStatus.PENDING)
                .build();

        Order finalOrder = order;
        request.getItems().forEach(itemDto -> {
            OrderItem item = OrderItem.builder()
                    .productId(itemDto.getProductId())
                    .quantity(itemDto.getQuantity())
                    .price(itemDto.getPrice())
                    .build();
            finalOrder.addItem(item);
        });

        order = orderRepository.save(order);
        log.info("Order created with ID: {}", order.getId());

        ccfIntegration.validateOrderConsistency(order);

        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(order.getId())
                .customerId(order.getCustomerId())
                .totalAmount(order.getTotalAmount())
                .timestamp(LocalDateTime.now().toString())
                .build();

        eventProducer.sendOrderCreatedEvent(event);

        return mapToResponse(order);
    }

    @Override
    public OrderResponse getOrder(Long orderId) {
        log.info("Fetching order: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        return mapToResponse(order);
    }

    @Override
    @Transactional
    @MonitorConsistency(contractId = "OrderPaymentConsistency")
    public OrderResponse confirmOrder(Long orderId) {
        log.info("Confirming order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        order.setStatus(OrderStatus.CONFIRMED);
        order = orderRepository.save(order);

        ccfIntegration.validateOrderConsistency(order);

        OrderConfirmedEvent event = OrderConfirmedEvent.builder()
                .orderId(order.getId())
                .timestamp(LocalDateTime.now().toString())
                .build();

        eventProducer.sendOrderConfirmedEvent(event);

        log.info("Order confirmed: {}", orderId);
        return mapToResponse(order);
    }

    @Override
    @Transactional
    @MonitorConsistency(contractId = "OrderPaymentConsistency")
    public OrderResponse cancelOrder(Long orderId, String reason) {
        log.info("Cancelling order: {} with reason: {}", orderId, reason);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        order.setStatus(OrderStatus.CANCELLED);
        order = orderRepository.save(order);

        OrderCancelledEvent event = OrderCancelledEvent.builder()
                .orderId(order.getId())
                .reason(reason)
                .timestamp(LocalDateTime.now().toString())
                .build();

        eventProducer.sendOrderCancelledEvent(event);

        log.info("Order cancelled: {}", orderId);
        return mapToResponse(order);
    }

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }
}