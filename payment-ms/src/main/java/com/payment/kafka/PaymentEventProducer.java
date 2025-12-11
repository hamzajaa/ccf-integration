package com.payment.kafka;

import com.payment.event.PaymentAuthorizedEvent;
import com.payment.event.PaymentFailedEvent;
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
public class PaymentEventProducer {

    private static final String PAYMENT_EVENTS_TOPIC = "payment-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendPaymentAuthorizedEvent(PaymentAuthorizedEvent event) {
        log.info("Publishing PaymentAuthorizedEvent for order: {}", event.getOrderId());

        Message<PaymentAuthorizedEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, PAYMENT_EVENTS_TOPIC)
                .setHeader(KafkaHeaders.KEY, "paymentAuthorized")
                .setHeader("__TypeId__", "paymentAuthorized")
                .build();

        kafkaTemplate.send(message);
    }

    public void sendPaymentFailedEvent(PaymentFailedEvent event) {
        log.info("Publishing PaymentFailedEvent for order: {}", event.getOrderId());

        Message<PaymentFailedEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, PAYMENT_EVENTS_TOPIC)
                .setHeader(KafkaHeaders.KEY, "paymentFailed")
                .setHeader("__TypeId__", "paymentFailed")
                .build();

        kafkaTemplate.send(message);
    }
}