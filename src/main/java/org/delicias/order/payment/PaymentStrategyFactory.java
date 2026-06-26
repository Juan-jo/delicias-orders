package org.delicias.order.payment;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.delicias.order.payment.method.CashPaymentStrategy;
import org.delicias.order.payment.method.StripePaymentStrategy;

@ApplicationScoped
public class PaymentStrategyFactory {

    @Inject
    CashPaymentStrategy cash;

    @Inject
    StripePaymentStrategy stripe;

    public PaymentStrategy getStrategy(PaymentMethod method) {
        return switch (method) {
            case CASH -> cash;
            case CARD -> stripe;
        };
    }
}
