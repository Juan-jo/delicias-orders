package org.delicias.order.dto;


import jakarta.validation.constraints.NotNull;

public record CancelOrderReqDTO(

        @NotNull
        String message
) { }
