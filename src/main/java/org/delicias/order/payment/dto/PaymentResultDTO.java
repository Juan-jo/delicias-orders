package org.delicias.order.payment.dto;

import org.delicias.order.payment.PaymentMethod;

public record PaymentResultDTO(
        PaymentMethod method,
        String clientSecret
) { }

