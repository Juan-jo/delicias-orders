package org.delicias.order.dto;

import java.util.UUID;

public record TrackingReqDTO(
        UUID deliveryUserOrderRelId
) { }
