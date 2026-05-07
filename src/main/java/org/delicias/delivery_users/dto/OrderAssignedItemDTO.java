package org.delicias.delivery_users.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import org.delicias.common.dto.order.OrderStatus;
import org.delicias.common.dto.order.OrderTrackingStatus;

import java.time.Instant;
import java.util.UUID;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OrderAssignedItemDTO(
        UUID id,
        OrderTrackingStatus status,
        Order order
) {
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Order(
            Long orderId,
            Instant deliveryAssignedAt,
            String code,
            OrderStatus status,
            Destination destination

    ) { }
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Destination(
            String name,
            String pictureUrl,
            String street,
            String address
    ) {}
}
