package org.delicias.delivery_users.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import org.delicias.common.dto.delivery.DeliveryUserStatus;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DeliveryUserItemDTO(
        Integer id,
        String name,
        DeliveryUserStatus status,
        String username,
        String email,
        String pictureUrl
) { }
