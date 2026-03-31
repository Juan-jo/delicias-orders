package org.delicias.kanban.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import org.delicias.common.dto.order.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
            @JsonFormat(pattern = "dd-MM HH:mm")
            LocalDateTime createdAt,
            OrderStatus status
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
