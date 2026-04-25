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


    private static final String CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generate() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }

        return sb.substring(0, 4) + "-" + sb.substring(4, 8);
        //return sb.substring(0, 3) + "-" + sb.substring(3, 6);
    }




    public static String generate2() {
        // 1. Tiempo en milisegundos
        long now = System.currentTimeMillis();

        // 2. Convertir a base36 (compacto)
        String timePart = Long.toString(now, 36).toUpperCase();

        // 3. Random (4 chars base36)
        String randomPart = randomBase36();

        return timePart + "-" + randomPart;
    }

    private static String randomBase36() {
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 4; i++) {
            sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }

        return sb.toString();
    }

    @GET
    @Path("/seq")
    public Response nextSeq() {

        DateTimeFormatter FORMATTER =
                DateTimeFormatter.ofPattern("yyyyMd").withZone(UTC);

        String date = FORMATTER.format(Instant.now());
        String seq = UUID.randomUUID().toString().substring(0, 6).toUpperCase();


        LocalDate dateLocal = LocalDate.now().plusDays(1);


        long epochDay = dateLocal.toEpochDay(); // días desde 1970
        String encoded = Long.toString(epochDay, 36).toUpperCase();


        long millis = System.currentTimeMillis();
        String timePart = Long.toString(millis, 36).toUpperCase();

        return Response.ok(
                Map.of(
                        //"code_mil", String.format("%s-%s", timePart, generate()),
                        //"code", String.format("%s-%s", encoded, generate()),
                        "code", generate2()


                )
        ).build();
    }


}


