package org.delicias.stripe.dto;

public record IntentResponseDTO(
        String clientSecret,
        String paymentIntentId
) {
}
