package org.delicias.kanban.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Builder
public record KanbanDTO(
        Integer id,
        String label,
        List<Board> children
) {

    @Builder
    public record Board(
            String id,
            List<BoardItem> children
    ) {}

    @Builder
    public record BoardItem(
            Long kanbanId,
            Long orderId,
            String status,
            BigDecimal totalAmount,
            List<ProductItem> products,
            @JsonFormat(pattern = "HH:mm")
            LocalDateTime createdAt
    ) {}

    @Builder
    public record ProductItem(
            String name,
            Short qty,
            List<String> attrValuesDesc
    ) {}


}
