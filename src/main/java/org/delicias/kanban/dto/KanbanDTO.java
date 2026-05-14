package org.delicias.kanban.dto;


import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
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
            String code,
            String status,
            BigDecimal totalAmount,
            List<ProductItem> products,
            Instant createdAt,
            Instant readyForDeliveryDate
    ) {}

    @Builder
    public record ProductItem(
            String name,
            Short qty,
            List<String> attrValuesDesc
    ) {}


}
