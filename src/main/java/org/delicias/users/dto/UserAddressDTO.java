package org.delicias.users.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserAddressDTO(
        Integer id,
        @JsonProperty("type_address")
        String typeAddress,
        String details,
        String street,
        double latitude,
        double longitude,
        String address,
        String indications
) { }
