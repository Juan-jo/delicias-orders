package org.delicias.order.dto;

import lombok.Builder;
import org.delicias.common.dto.order.OrderStatus;

import java.time.Instant;

@Builder
public record OrderedDTO(
        Long orderId,
        String code,
        OrderStatus status,
        Instant orderedAt,
        Restaurant restaurant
) {

    @Builder
    public record Restaurant(
            String name,
            String pictureUrl
    ){}
}
