package com.order.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentAuthorizedEvent {
    private String paymentId;
    private Long orderId;
    private BigDecimal amount;
    private String timestamp;
}