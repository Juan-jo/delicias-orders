package org.delicias.kanban.resource;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.delicias.kanban.dto.KanbanChangeStatusReqDTO;
import org.delicias.kanban.service.KanbanService;
import org.delicias.order.dto.CreateOrderReqDTO;

@Authenticated
@Path("/api/orders/kanban")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class KanbanResource {

    @Inject
    KanbanService kanbanService;

    @GET
    public Response loadKanban(
            @QueryParam("restaurant") Integer restaurantTmplId
    ) {
        var response = kanbanService.loadKanban(restaurantTmplId);

        return Response.ok(response).build();
    }


    @GET
    @Path("/{kanbanId}/detail")
    public Response getKanbanDetail(
            @PathParam("kanbanId") Long kanbanId
    ) {
        var response = kanbanService.getDetail(kanbanId);

        return Response.ok(response).build();
    }

    @PUT
    @Path("/{kanbanId}/status")
    public Response changeStatus(
            @PathParam("kanbanId") Long kanbanId,
            @Valid KanbanChangeStatusReqDTO req
    ) {
        kanbanService.changeStatus(kanbanId, req);

        return Response.ok().build();
    }
}
