package com.payment.kafka;

import com.payment.event.OrderCreatedEvent;
import com.payment.service.PaymentService;
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
public class OrderEventConsumer {

    private final PaymentService paymentService;

    @KafkaListener(topics = "order-events", groupId = "payment-service-group")
    public void consumeOrderEvents(
            @Payload OrderCreatedEvent event,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key) {

        log.info("Received order event with key: {}", key);

        if ("orderCreated".equals(key)) {
            handleOrderCreated(event);
        }
    }

    private void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Processing payment for order: {}", event.getOrderId());
        paymentService.processPayment(event.getOrderId(), event.getTotalAmount());
    }
}