package org.delicias.stripe.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.delicias.stripe.dto.IntentResponseDTO;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.math.BigDecimal;

@ApplicationScoped
public class StripePaymentService {

    @ConfigProperty(name = "stripe.secret.key")
    String stripeSecretKey;


    @PostConstruct
    void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    private static final String CURRENCY = "mxn";

    public IntentResponseDTO createPaymentIntent(
            Long orderId,
            String code,
            BigDecimal amount
    ) throws StripeException {

        validateAmount(amount);

        long amountInCents = convertToStripeAmount(amount);

        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setAmount(amountInCents)
                        .setCurrency(CURRENCY)
                        .addPaymentMethodType("card")
                        .putMetadata("order_id", String.valueOf(orderId))
                        .putMetadata("code", code)
                        .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);


        return new IntentResponseDTO(
                paymentIntent.getClientSecret(),
                paymentIntent.getId()
        );
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El monto debe ser mayor a cero"
            );
        }
    }

    private long convertToStripeAmount(BigDecimal amount) {
        return amount
                .multiply(BigDecimal.valueOf(100))
                .longValueExact();
    }

}
