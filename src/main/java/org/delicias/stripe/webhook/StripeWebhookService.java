package org.delicias.stripe.webhook;

import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import org.delicias.common.dto.order.OrderStatus;
import org.delicias.order.domain.model.PaymentTransaction;
import org.delicias.order.domain.model.PosOrder;
import org.delicias.order.domain.repository.PosOrderRepository;
import org.delicias.order.payment.PaymentStatus;
import org.delicias.order.service.OrderChangeStatusService;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Instant;

@ApplicationScoped
public class StripeWebhookService {

    @ConfigProperty(name = "stripe.webhook.secret")
    String endpointSecret;

    @Inject
    PosOrderRepository orderRepository;

    @Inject
    OrderChangeStatusService changeStatusService;

    @Transactional
    public void processWebhook(
            String payload,
            String signature
    ) throws EventDataObjectDeserializationException {

        Event event;

        try {

            event = Webhook.constructEvent(
                    payload,
                    signature,
                    endpointSecret
            );

            System.out.println(
                    "----- Evento: "
                            + event.toJson());

            System.out.println(
                    "----- Version: "
                            + event.getApiVersion());

            System.out.println("----- isPresent: "
                            + event.getDataObjectDeserializer().getObject().isPresent());

        } catch (SignatureVerificationException e) {

            throw new WebApplicationException(
                    "Invalid signature",
                    400
            );
        }

        switch (event.getType()) {

            case "payment_intent.succeeded" ->
                    handleSucceeded(event);

            case "payment_intent.payment_failed" ->
                    handleFailed(event);

            default ->
                    System.out.println(
                            "Evento ignorado: "
                                    + event.getType());
        }
    }

    private PaymentIntent extractPaymentIntent(Event event) throws EventDataObjectDeserializationException {

        EventDataObjectDeserializer deserializer =
                event.getDataObjectDeserializer();

        if (deserializer.getObject().isPresent()) {
            return (PaymentIntent) deserializer.getObject().get();
        }

        return (PaymentIntent) deserializer.deserializeUnsafe();
    }

    private void handleSucceeded(Event event) throws EventDataObjectDeserializationException {

        PaymentIntent paymentIntent = extractPaymentIntent(event);

        String orderId =
                paymentIntent
                        .getMetadata()
                        .get("order_id");

        String paymentIntentId = paymentIntent.getId();

        PosOrder order = orderRepository.findById(Long.valueOf(orderId));

        if (order == null) {
            return;
        }

        //order.setPaymentStatus(PaymentStatus.SUCCEEDED);
        //order.setStatus(OrderStatus.ORDERED);
        changeStatusService.changeStatus(order, OrderStatus.ORDERED);

        PaymentTransaction tx = PaymentTransaction.findByTransactionId(paymentIntentId)
                .orElse(null);

        if(tx == null) {
            return;
        }

        tx.setStatus(PaymentStatus.SUCCEEDED);
        tx.setPaidAt(Instant.now());
        tx.setUpdatedAt(Instant.now());

    }

    private void handleFailed(Event event) throws EventDataObjectDeserializationException {

        PaymentIntent paymentIntent = extractPaymentIntent(event);

        String paymentIntentId = paymentIntent.getId();

        PaymentTransaction tx = PaymentTransaction.findByTransactionId(paymentIntentId)
                .orElse(null);

        if (tx == null) {
            return;
        }

        tx.setStatus(PaymentStatus.FAILED);
        tx.setFailureReason(
                paymentIntent.getLastPaymentError() != null
                        ? paymentIntent.getLastPaymentError().getMessage()
                        : "Unknown error"
        );

        PosOrder order = tx.getOrder();
        order.setPaymentStatus(PaymentStatus.FAILED);

    }
}
