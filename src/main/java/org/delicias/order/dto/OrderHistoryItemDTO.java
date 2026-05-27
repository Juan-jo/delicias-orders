package org.delicias.order.dto;

import lombok.Builder;
import org.delicias.common.dto.order.OrderStatus;

import java.time.Instant;

@Builder
public record OrderHistoryItemDTO(
        Long orderId,
        String restaurantName,
        String restaurantPictureUrl,
        OrderStatus status,
        Instant orderedAt
) { }
