package org.delicias.order.resource;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.delicias.order.dto.CreateOrderReqDTO;
import org.delicias.order.service.PosOrderService;

@Authenticated
@Path("/api/orders")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class OrderResource {

    @Inject
    PosOrderService orderService;

    @POST
    public Response create(
            @Valid CreateOrderReqDTO reqDTO
    ) {

        orderService.createOrder(reqDTO);
        return Response.status(Response.Status.CREATED).build();
    }

}
