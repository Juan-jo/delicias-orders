package org.delicias.order.payment;

import com.stripe.exception.StripeException;
import org.delicias.order.domain.model.PosOrder;
import org.delicias.order.payment.dto.PaymentResultDTO;

public interface PaymentStrategy {
    PaymentResultDTO processPayment(PosOrder order) throws StripeException;
}
