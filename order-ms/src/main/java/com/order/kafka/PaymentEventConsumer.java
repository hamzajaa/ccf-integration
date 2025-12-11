package com.order.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.order.event.PaymentAuthorizedEvent;
import com.order.event.PaymentFailedEvent;
import com.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "payment-events", groupId = "order-service-group")
    public void consumePaymentEvents(
            @Payload String message,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key) {

        log.info("Received payment event with key: {}", key);

        try {
            if ("paymentAuthorized".equals(key)) {
                PaymentAuthorizedEvent event = objectMapper.readValue(message, PaymentAuthorizedEvent.class);
                handlePaymentAuthorized(event);
            } else if ("paymentFailed".equals(key)) {
                PaymentFailedEvent event = objectMapper.readValue(message, PaymentFailedEvent.class);
                handlePaymentFailed(event);
            } else {
                log.warn("Unknown payment event key: {}", key);
            }
        } catch (Exception e) {
            log.error("Error processing payment event: {}", e.getMessage(), e);
        }
    }

    private void handlePaymentAuthorized(PaymentAuthorizedEvent event) {
        log.info("Payment authorized for order: {}", event.getOrderId());
        orderService.confirmOrder(event.getOrderId());
    }

    private void handlePaymentFailed(PaymentFailedEvent event) {
        log.warn("Payment failed for order: {}", event.getOrderId());
        orderService.cancelOrder(event.getOrderId(), event.getReason());
    }
}