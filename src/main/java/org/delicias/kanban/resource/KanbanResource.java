package org.delicias.kanban.resource;

import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.delicias.common.roles.Roles;
import org.delicias.kanban.dto.KanbanChangeStatusReqDTO;
import org.delicias.kanban.dto.OrderRejectReqDTO;
import org.delicias.kanban.service.KanbanService;

@Authenticated
@Path("/api/orders/kanban")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class KanbanResource {

    @Inject
    KanbanService kanbanService;

    @GET
    @RolesAllowed({Roles.ROLE_WEB_RESTAURANT, Roles.ROLE_ROOT})
    public Response loadKanban(
            @QueryParam("restaurant") Integer restaurantTmplId
    ) {
        var response = kanbanService.loadKanban(restaurantTmplId);

        return Response.ok(response).build();
    }


    @GET
    @Path("/{kanbanId}/detail")
    @RolesAllowed({Roles.ROLE_WEB_RESTAURANT, Roles.ROLE_ROOT})
    public Response getKanbanDetail(
            @PathParam("kanbanId") Long kanbanId
    ) {
        var response = kanbanService.getDetail(kanbanId);

        return Response.ok(response).build();
    }

    @PUT
    @Path("/{kanbanId}/status")
    @RolesAllowed({Roles.ROLE_WEB_RESTAURANT, Roles.ROLE_ROOT})
    public Response changeStatus(
            @PathParam("kanbanId") Long kanbanId,
            @Valid KanbanChangeStatusReqDTO req
    ) {
        kanbanService.changeStatus(kanbanId, req);

        return Response.ok().build();
    }

    @PUT
    @Path("/{kanbanId}/reject")
    @RolesAllowed({Roles.ROLE_WEB_RESTAURANT, Roles.ROLE_ROOT})
    public Response reject(
            @PathParam("kanbanId") Long kanbanId,
            @Valid OrderRejectReqDTO req
    ) {
        kanbanService.rejectOrder(kanbanId, req);
        return Response.ok().build();
    }

}
