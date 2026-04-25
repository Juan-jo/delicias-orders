package org.delicias.delivery_users.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;


@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DeliveryUserDTO(
        Integer id,
        String name,
        String lastName,
        String username,
        String email,
        String pictureUrl
) { }
