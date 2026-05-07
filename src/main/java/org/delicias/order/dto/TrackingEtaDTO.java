package org.delicias.order.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TrackingEtaDTO(
        Long orderId,
        Double lat,
        Double lng,
        Double distance,
        Double duration,
        String route
) { }
