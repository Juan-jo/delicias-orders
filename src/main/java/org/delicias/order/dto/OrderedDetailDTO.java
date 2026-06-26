package org.delicias.order.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import org.delicias.common.dto.order.OrderStatus;
import org.delicias.order.payment.PaymentMethod;
import org.delicias.order.payment.PaymentStatus;

import java.math.BigDecimal;
import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OrderedDetailDTO(
        Long orderId,
        String code,
        OrderStatus status,
        BigDecimal subtotalAmount,
        BigDecimal totalAmount,
        List<Line> lines,
        DeliveryAddress deliveryAddress,
        DeliveryUser deliveryUser,
        List<Adjustment> adjustments,
        String rejectOrCancelMessage,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus
) {

    @Builder
    public record Line(
            String name,
            String pictureUrl,
            Short qty,
            List<String> attributes,
            BigDecimal total
    ){}

    @Builder
    public record DeliveryAddress(
            String addressType,
            String name,
            List<String> address
    ){}

    @Builder
    public record DeliveryUser(
            String name,
            String lastName,
            String pictureUrl
    ){}

    @Builder
    public record Adjustment(
            String name,
            BigDecimal amount
    ){}
}
