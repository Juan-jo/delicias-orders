package org.delicias.stripe.webhook;


import com.stripe.exception.EventDataObjectDeserializationException;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/payment/v1/webhook")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class StripeWebhookResource {

    @Inject
    StripeWebhookService stripeWebhookService;

    @POST
    public Response receiveWebhook(
            @HeaderParam("Stripe-Signature") String signature,
            String payload
    ) throws EventDataObjectDeserializationException {

        stripeWebhookService.processWebhook(
                payload,
                signature
        );

        return Response.ok().build();
    }
}