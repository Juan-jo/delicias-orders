package org.delicias.order.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import org.delicias.common.dto.order.OrderStatus;
import org.delicias.common.dto.order.OrderTrackingStatus;

import java.util.UUID;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TrackingDTO(
        UUID deliveryUserOrderRelId,
        OrderTrackingStatus status,
        Order order
) {

    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Order(
            Long orderId,
            String code,
            OrderStatus status,
            Destination destination
    ) {}



    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Destination(
            String name,
            String pictureUrl,
            String street,
            String address,
            String indications,
            double latitude,
            double longitude
    ) {}
}
