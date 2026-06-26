package org.delicias.order.dto;

import org.delicias.order.payment.PaymentMethod;

public record CreateOrderResponseDTO(
        PaymentMethod paymentMethod,
        String clientSecret
) {
}
