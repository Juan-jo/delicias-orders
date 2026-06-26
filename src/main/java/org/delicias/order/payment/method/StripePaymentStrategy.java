package org.delicias.order.payment.method;

import com.stripe.exception.StripeException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.delicias.common.dto.order.OrderStatus;
import org.delicias.order.domain.model.PaymentTransaction;
import org.delicias.order.domain.model.PosOrder;
import org.delicias.order.payment.PaymentMethod;
import org.delicias.order.payment.PaymentStatus;
import org.delicias.order.payment.PaymentStrategy;
import org.delicias.order.payment.dto.PaymentResultDTO;
import org.delicias.order.service.OrderChangeStatusService;
import org.delicias.stripe.dto.IntentResponseDTO;
import org.delicias.stripe.service.StripePaymentService;

import java.time.Instant;

@ApplicationScoped
public class StripePaymentStrategy implements PaymentStrategy {

    private static final PaymentMethod CARD = PaymentMethod.CARD;

    @Inject
    StripePaymentService paymentService;

    @Inject
    OrderChangeStatusService changeStatusService;

    @Override
    public PaymentResultDTO processPayment(PosOrder order) throws StripeException {

        order.setPaymentMethod(CARD);
        order.setPaymentStatus(PaymentStatus.PROCESSING);
        order.persist();

        changeStatusService.changeStatus(order, OrderStatus.PENDING_PAYMENT);

        IntentResponseDTO response = paymentService.createPaymentIntent(
                order.getId(),
                order.getCode(),
                order.getTotalAmount()
        );

        PaymentTransaction.builder()
                .order(order)
                .provider("STRIPE")
                .transactionId(response.paymentIntentId())
                .status(PaymentStatus.PROCESSING)
                .amount(order.getTotalAmount())
                .currency("mxn")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build().persist();

        System.out.println("-------------------- PaymentIntentId -- "+response.paymentIntentId());
        System.out.println("-------------------- ClientSecret ----- "+response.clientSecret());

        return new PaymentResultDTO(
                CARD,
                response.clientSecret()
        );
    }


}
