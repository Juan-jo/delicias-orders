package org.delicias.delivery_users.resource;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.ConvertGroup;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.delicias.common.validation.OnCreate;
import org.delicias.common.validation.OnUpdate;
import org.delicias.delivery_users.dto.ChangePasswordReqDTO;
import org.delicias.delivery_users.dto.CreateDeliverUserReqDTO;
import org.delicias.delivery_users.dto.UpdateDeliveryUserReqDTO;
import org.delicias.delivery_users.service.DeliveryUserService;

@Path("/api/user-delivery")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class DeliveryUserResource {


    @Inject
    DeliveryUserService deliveryUserService;


    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response create(
            @Valid @ConvertGroup(to = OnCreate.class)
            @BeanParam CreateDeliverUserReqDTO req
    ) {


        deliveryUserService.createUserDelivery(req);

        return Response.status(Response.Status.CREATED).build();
    }

    @GET
    @Path("/{deliveryUserId}")
    public Response findById(
            @PathParam("deliveryUserId") Integer deliveryUserId) {

        return Response.ok(
                deliveryUserService.findById(deliveryUserId)
        ).build();
    }

    @PUT
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response update(
            @Valid @ConvertGroup(to = OnUpdate.class)
            @BeanParam UpdateDeliveryUserReqDTO req
    ) {


        deliveryUserService.update(req);

        return Response.status(Response.Status.OK).build();
    }

    @DELETE
    @Path("/{deliveryUserId}")
    public Response deleteById(
            @PathParam("deliveryUserId") Integer deliveryUserId) {

        deliveryUserService.delete(deliveryUserId);
        return Response.noContent().build();
    }

    @PUT
    @Path("/pwd")
    public Response changePwd(
            @Valid ChangePasswordReqDTO req
    ) {
        deliveryUserService.changePassword(req);
        return Response.status(Response.Status.OK).build();
    }

    @GET
    public Response search(
            @QueryParam("zoneId") @NotNull Integer zoneId,
            @QueryParam("name") @DefaultValue("") String name,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size,
            @QueryParam("orderColumn") @DefaultValue("name") String orderColumn,
            @QueryParam("orderDir") @DefaultValue("asc") String orderDir

    ) {
        var filtered = deliveryUserService.search(
                zoneId,
                name,
                page,
                size,
                orderColumn,
                orderDir
        );

        return Response.ok(filtered).build();
    }
}
