package org.delicias.restaurants.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.delicias.common.dto.restaurant.StoreType;

public record PosRestaurantDTO(
        Integer id,
        String name,
        String photo,
        double latitude,
        double longitude,
        String address,
        @JsonProperty("store_type")
        StoreType storeType
) { }
