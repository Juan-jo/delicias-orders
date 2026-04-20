package org.delicias.order.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import org.delicias.common.dto.order.OrderStatus;

import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserOrderDTO(
        Long orderId,
        Restaurant restaurant,
        OrderStatus status,
        List<Line> lines,
        DeliveryAddress deliveryAddress,
        DeliveryUser deliveryUser
) {

    @Builder
    public record Restaurant(
            String name,
            String pictureUrl
    ){}

    @Builder
    public record Line(
            String name,
            String pictureUrl,
            Short qty,
            List<String> attributes
    ){}

    @Builder
    public record DeliveryAddress(

            String street,
            String address,
            String details,
            String indications
    ){}

    @Builder
    public record DeliveryUser(
            String name,
            String lastName,
            String pictureUrl
    ){}

}
