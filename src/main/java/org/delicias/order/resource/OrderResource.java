package org.delicias.order.resource;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.delicias.order.dto.CreateOrderReqDTO;
import org.delicias.order.dto.UserOrderReqType;
import org.delicias.order.service.PosOrderService;
import org.delicias.order.service.UserOrdersService;

@Authenticated
@Path("/api/orders")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class OrderResource {

    @Inject
    PosOrderService orderService;

    @Inject
    UserOrdersService userOrdersService;

    @POST
    public Response create(
            @Valid CreateOrderReqDTO reqDTO
    ) {

        orderService.createOrder(reqDTO);
        return Response.status(Response.Status.CREATED).build();
    }


    @GET
    @Path("/user")
    public Response userOrders(
            @QueryParam("type") @DefaultValue("IN_PROGRESS") UserOrderReqType reqType
    ) {
        return Response.ok(
                userOrdersService.loadOrders(reqType)
        ).build();
    }

}
