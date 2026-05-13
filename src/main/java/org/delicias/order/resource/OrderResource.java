package org.delicias.order.resource;

import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.delicias.common.roles.Roles;
import org.delicias.order.dto.CreateOrderReqDTO;
import org.delicias.order.dto.UserOrderReqType;
import org.delicias.order.service.PosOrderService;
import org.delicias.order.service.TrackingOrderService;
import org.delicias.order.service.UserOrdersService;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

import static java.time.ZoneOffset.UTC;

@Authenticated
@Path("/api/orders")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class OrderResource {

    @Inject
    PosOrderService orderService;

    @Inject
    UserOrdersService userOrdersService;

    @Inject
    TrackingOrderService trackingOrderService;

    @POST
    @RolesAllowed({Roles.ROLE_MOBILE_USER})
    public Response create(
            @Valid CreateOrderReqDTO reqDTO
    ) {

        orderService.createOrder(reqDTO);
        return Response.status(Response.Status.CREATED).build();
    }

    @GET
    @RolesAllowed({Roles.ROLE_MOBILE_USER})
    @Path("/ordered")
    public Response getOrdered() {
        return Response.ok(
                userOrdersService.getOrdered()
        ).build();
    }

    @GET
    @Path("/ordered/{orderId}/detail")
    @RolesAllowed({Roles.MOBILE_USER_DELIVERY, Roles.ROLE_MOBILE_USER})
    public Response getOrderedDetail(
            @PathParam("orderId") Long orderId
    ) {
        return Response.ok(
                userOrdersService.getOrderedDetail(orderId)
        ).build();
    }


    @GET
    @RolesAllowed({Roles.ROLE_MOBILE_USER})
    @Path("/user")
    public Response userOrders(
            @QueryParam("type") @DefaultValue("IN_PROGRESS") UserOrderReqType reqType
    ) {
        return Response.ok(
                userOrdersService.loadOrders(reqType)
        ).build();
    }

    @GET
    @Path("/{orderId}/eta")
    @RolesAllowed({Roles.MOBILE_USER_DELIVERY, Roles.ROLE_MOBILE_USER})
    public Response etaOrder(
            @PathParam("orderId") Long orderId
    ) {
        return Response.ok(
                trackingOrderService.trackingEta(orderId)
        ).build();
    }

    @GET
    @Path("/tracking/{deliveryUserOrderRelId}/start")
    @RolesAllowed({Roles.MOBILE_USER_DELIVERY})
    public Response startTracking(
            @PathParam("deliveryUserOrderRelId") UUID id
    ) {

        return Response.ok(
                trackingOrderService.startToStore(id)
        ).build();
    }

    @GET
    @Path("/tracking/{deliveryUserOrderRelId}/destination")
    @RolesAllowed({Roles.MOBILE_USER_DELIVERY})
    public Response startDelivery(
            @PathParam("deliveryUserOrderRelId") UUID id
    ) {

        return Response.ok(
                trackingOrderService.startToDestination(id)
        ).build();
    }


    @GET
    @Path("/tracking/{deliveryUserOrderRelId}/continue")
    @RolesAllowed({Roles.MOBILE_USER_DELIVERY})
    public Response continueTRacking(
            @PathParam("deliveryUserOrderRelId") UUID id
    ) {

        return Response.ok(
                trackingOrderService.continueTracking(id)
        ).build();
    }

    @GET
    @Path("/tracking/{deliveryUserOrderRelId}/complete")
    @RolesAllowed({Roles.MOBILE_USER_DELIVERY})
    public Response completeOrder(
            @PathParam("deliveryUserOrderRelId") UUID id
    ) {

        return Response.ok(
                trackingOrderService.trackingComplete(id)
        ).build();
    }

    @GET
    @Path("/seq")
    public Response nextSeq() {

        return Response.ok(
                Map.of(
                        "code", orderService.getTestCode()
                )
        ).build();
    }


}


