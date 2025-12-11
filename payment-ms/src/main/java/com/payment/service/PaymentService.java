package com.payment.service;

import com.payment.dto.PaymentResponse;

import java.math.BigDecimal;

public interface PaymentService {
    PaymentResponse processPayment(Long orderId, BigDecimal amount);
    PaymentResponse getPayment(String paymentId);
}