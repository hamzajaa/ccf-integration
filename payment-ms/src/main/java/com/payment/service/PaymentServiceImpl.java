package com.payment.service;

import ccf.ccf.verification.MonitorConsistency;
import com.payment.ccf.PaymentCcfIntegration;
import com.payment.dto.PaymentResponse;
import com.payment.event.PaymentAuthorizedEvent;
import com.payment.event.PaymentFailedEvent;
import com.payment.kafka.PaymentEventProducer;
import com.payment.model.Payment;
import com.payment.model.PaymentStatus;
import com.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventProducer eventProducer;
    private final PaymentCcfIntegration ccfIntegration;
    private final Random random = new Random();

    @Override
    @MonitorConsistency(contractId = "OrderPaymentConsistency")
    public PaymentResponse processPayment(Long orderId, BigDecimal amount) {
        log.info("Processing payment for order: {} with amount: {}", orderId, amount);

        // Simulate payment processing (70% success, 30% failure for testing)
        boolean paymentSuccess = simulatePaymentGateway(amount);

        Payment payment = Payment.builder()
                .orderId(orderId)
                .amount(amount)
                .status(paymentSuccess ? PaymentStatus.AUTHORIZED : PaymentStatus.DECLINED)
                .paymentMethod("CARD")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        payment = paymentRepository.save(payment);
        log.info("Payment saved with ID: {} and status: {}", payment.getId(), payment.getStatus());

        ccfIntegration.validatePaymentConsistency(payment);

        if (paymentSuccess) {
            PaymentAuthorizedEvent event = PaymentAuthorizedEvent.builder()
                    .paymentId(payment.getId())
                    .orderId(orderId)
                    .amount(amount)
                    .timestamp(LocalDateTime.now().toString())
                    .build();

            eventProducer.sendPaymentAuthorizedEvent(event);
            log.info("Payment authorized for order: {}", orderId);
        } else {
            PaymentFailedEvent event = PaymentFailedEvent.builder()
                    .paymentId(payment.getId())
                    .orderId(orderId)
                    .reason("Insufficient funds")
                    .timestamp(LocalDateTime.now().toString())
                    .build();

            eventProducer.sendPaymentFailedEvent(event);
            log.warn("Payment declined for order: {}", orderId);
        }

        return mapToResponse(payment);
    }

    @Override
    public PaymentResponse getPayment(String paymentId) {
        log.info("Fetching payment: {}", paymentId);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
        return mapToResponse(payment);
    }

    private boolean simulatePaymentGateway(BigDecimal amount) {
        // 70% success rate for testing
//        return random.nextInt(100) < 70;
        boolean paymentSuccess = false;
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.error("Amount must be greater than zero");
        } else {

            paymentSuccess = amount.compareTo(BigDecimal.valueOf(20)) > 0;
        }
        return paymentSuccess;
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}