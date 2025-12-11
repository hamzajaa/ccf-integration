package com.order.kafka;

import com.order.event.OrderCancelledEvent;
import com.order.event.OrderConfirmedEvent;
import com.order.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private static final String ORDER_EVENTS_TOPIC = "order-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Publishing OrderCreatedEvent for order: {}", event.getOrderId());

        Message<OrderCreatedEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, ORDER_EVENTS_TOPIC)
                .setHeader(KafkaHeaders.KEY, "orderCreated")
                .setHeader("__TypeId__", "orderCreated")  // Add type header
                .build();

        kafkaTemplate.send(message);
    }

    public void sendOrderConfirmedEvent(OrderConfirmedEvent event) {
        log.info("Publishing OrderConfirmedEvent for order: {}", event.getOrderId());

        Message<OrderConfirmedEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, ORDER_EVENTS_TOPIC)
                .setHeader(KafkaHeaders.KEY, "orderConfirmed")
                .setHeader("__TypeId__", "orderConfirmed")
                .build();

        kafkaTemplate.send(message);
    }

    public void sendOrderCancelledEvent(OrderCancelledEvent event) {
        log.info("Publishing OrderCancelledEvent for order: {}", event.getOrderId());

        Message<OrderCancelledEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, ORDER_EVENTS_TOPIC)
                .setHeader(KafkaHeaders.KEY, "orderCancelled")
                .setHeader("__TypeId__", "orderCancelled")
                .build();

        kafkaTemplate.send(message);
    }
}