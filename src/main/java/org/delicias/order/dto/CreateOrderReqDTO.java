package org.delicias.order.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateOrderReqDTO(
        @NotNull(message = "ShoppingCartId is mandatory")
        UUID shoppingCartId,
        String notes
) { }
