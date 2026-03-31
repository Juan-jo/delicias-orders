package org.delicias.restaurants.dto;

public record PosRestaurantDTO(
        Integer id,
        String name,
        String photo,
        double latitude,
        double longitude,
        String address
) { }
