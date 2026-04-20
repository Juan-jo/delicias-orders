package org.delicias.kanban.dto;

import lombok.Builder;
import org.delicias.common.dto.order.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Builder
public record KanbanDetailDTO(
        Long kanbanId,
        Order order
) {

    @Builder
    public record Order(
            Long orderId,
            List<Line> lines,
            BigDecimal totalAmount,
            String paymentType,
            Instant createdAt,
            OrderStatus status,
            Instant readyForDelivery
    ) { }

    @Builder
    public record Line(
            String productName,
            String pictureUrl,
            List<String> attrs,
            Short qty,
            BigDecimal priceTotal
    ) { }

}
