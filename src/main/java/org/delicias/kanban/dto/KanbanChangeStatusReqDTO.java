package org.delicias.kanban.dto;

import jakarta.validation.constraints.NotNull;
import org.delicias.common.dto.order.OrderStatus;


public record KanbanChangeStatusReqDTO(
        @NotNull
        OrderStatus status,
        String notes
) {
}
