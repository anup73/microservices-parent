package com.agent.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long paymentId,
        Long orderId,
        String orderNumber,
        Long customerId,
        String paymentMethod,
        String paymentStatus,
        BigDecimal amount,
        String currency,
        String transactionReference,
        LocalDateTime paidAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
