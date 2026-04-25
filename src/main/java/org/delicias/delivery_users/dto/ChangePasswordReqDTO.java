package org.delicias.delivery_users.dto;

import jakarta.validation.constraints.NotNull;

public record ChangePasswordReqDTO(

        @NotNull(message = "password is mandatory")
        Integer deliveryUserId,

        @NotNull(message = "password is mandatory")
        String password
) {
}
