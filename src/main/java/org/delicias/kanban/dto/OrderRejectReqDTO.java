package org.delicias.kanban.dto;

import jakarta.validation.constraints.NotNull;

public record OrderRejectReqDTO(
        @NotNull
        String message
) {
}
