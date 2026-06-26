package org.delicias.order.dto;

import jakarta.validation.constraints.NotNull;
import org.delicias.order.payment.PaymentMethod;

import java.util.UUID;

public record CreateOrderReqDTO(
        @NotNull(message = "ShoppingCartId is mandatory")
        UUID shoppingCartId,

        @NotNull(message = "paymentMethod is mandatory")
        PaymentMethod paymentMethod,

        String notes
) { }
