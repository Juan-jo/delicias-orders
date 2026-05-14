package org.delicias.order.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.delicias.common.dto.order.CandidateOrderDTO;
import org.delicias.common.dto.order.OrderStatus;
import org.delicias.common.dto.user.UserZoneDTO;
import org.delicias.kanban.domain.model.Kanban;
import org.delicias.kanban.domain.repository.KanbanRepository;
import org.delicias.kanban.dto.KanbanDTO;
import org.delicias.order.domain.model.PosOrder;
import org.delicias.order.domain.model.PosOrderLine;
import org.delicias.order.domain.repository.PosOrderRepository;
import org.delicias.order.dto.CreateOrderReqDTO;
import org.delicias.order.exception.CandidateOrderBusinessException;
import org.delicias.order.exception.CandidateOrderErrorCode;
import org.delicias.outbox.domain.OutboxEvent;
import org.delicias.outbox.domain.OutboxEventType;
import org.delicias.products.domain.model.PosProduct;
import org.delicias.products.service.PosProductService;
import org.delicias.rest.clients.RestaurantClient;
import org.delicias.rest.clients.ShoppingCartClient;
import org.delicias.rest.clients.UserClient;
import org.delicias.rest.security.SecurityContextService;
import org.delicias.restaurants.domain.model.PosRestaurant;
import org.delicias.restaurants.dto.PosRestaurantDTO;
import org.delicias.restaurants.service.PosRestaurantService;
import org.delicias.users.domain.model.PosUserAddress;
import org.delicias.users.dto.UserAddressDTO;
import org.delicias.users.service.PosUserAddressService;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PosOrderService {


    @Inject
    @RestClient
    ShoppingCartClient shoppingCartClient;

    @Inject
    @RestClient
    RestaurantClient restaurantClient;

    @Inject
    @RestClient
    UserClient userClient;

    @Inject
    PosRestaurantService restaurantService;

    @Inject
    PosUserAddressService addressService;

    @Inject
    PosProductService productService;

    @Inject
    PosOrderRepository posOrderRepository;

    @Inject
    KanbanRepository kanbanRepository;

    @Inject
    SecurityContextService security;

    @Inject
    ObjectMapper mapper;

    private static final long CUSTOM_EPOCH = 1624665600000L; // Fecha de referencia: 26 de Jun de 2021 00:00:00 UTC
    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom RANDOM = new SecureRandom();



    @Transactional
    public void createOrder(CreateOrderReqDTO reqDTO) {

        GeometryFactory geometryFactory = new GeometryFactory();
        UUID userUUID = UUID.fromString(security.userId());

        UserZoneDTO userZoneDTO = getUserZone(userUUID);
        CandidateOrderDTO candidateOrder = getCandidateOrder(reqDTO.shoppingCartId());
        PosRestaurantDTO restaurant = getRestaurant(candidateOrder.restaurantTmplId());
        UserAddressDTO userAddress = getUserAddress(candidateOrder.deliveryAddressId());


        PosRestaurant posRestaurant = restaurantService.createOrUpdate(restaurant);
        PosUserAddress posUserAddress = addressService.createOrUpdate(userAddress);

        Map<Integer, PosProduct> productsMap = productService.addProducts(candidateOrder.lines());

        PosOrder order = PosOrder.builder()
                .userUUID(userUUID)
                .restaurant(posRestaurant)
                .status(OrderStatus.ORDERED)
                .notes(reqDTO.notes())
                .adjustments(candidateOrder.adjustments())
                .totalAmountRestaurant(candidateOrder.subtotal())
                .totalAmount(candidateOrder.total())
                .orderedAt(Instant.now())
                .zoneId(userZoneDTO.zoneId())
                .code(generateCode())
                .userAddress(posUserAddress)
                .deliveryLocation(
                        geometryFactory.createPoint(new Coordinate(userAddress.longitude(), userAddress.latitude()))
                )
                .shoppingCartId(reqDTO.shoppingCartId())
                .build();

        candidateOrder.lines().forEach(it -> {
            PosOrderLine line = PosOrderLine.builder()
                    .product(productsMap.get(it.productId()))
                    .attributes(it.attributes())
                    .qty(it.qty())
                    .priceUnit(it.priceUnit())
                    .priceTotal(it.priceTotal())
                    .build();

            order.addLine(line);

        });

        deleteShoppingCart(reqDTO.shoppingCartId());

        posOrderRepository.persist(order);

        Kanban kanban = Kanban.builder()
                .order(order)
                .restaurantId(candidateOrder.restaurantTmplId())
                .build();

        kanbanRepository.persist(kanban);

        sendOutboxEvent(kanban.getId(), candidateOrder.restaurantTmplId(), order);
    }

    private void sendOutboxEvent(Long kanbanId, Integer restaurantTmplId ,PosOrder order) {

        KanbanDTO.BoardItem item = KanbanDTO.BoardItem.builder()
                .restaurantTmplId(restaurantTmplId)
                .kanbanId(kanbanId)
                .orderId(order.getId())
                .code(order.getCode())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmountRestaurant())
                .createdAt(order.getOrderedAt())
                .readyForDeliveryDate(order.getReadyForDeliveryAt())
                .products(order.getLines().stream().map(line -> KanbanDTO.ProductItem.builder()
                        .name(Optional.ofNullable(line.getProduct()).map(PosProduct::getName).orElse("Product Unknow"))
                        .qty(line.getQty())
                        .attrValuesDesc(line.getAttributes())
                        .build()).toList())
                .build();

        OutboxEvent outboxEvent = OutboxEvent.builder()
                .aggregateId(order.getId())
                .type(OutboxEventType.CREATE_ORDER)
                .payload(mapper.valueToTree(item))
                .createdAt(LocalDateTime.now())
                .build();

        outboxEvent.persist();
    }

    private void deleteShoppingCart(UUID shoppingCartId) {
        try (Response response = shoppingCartClient.deleteShippingCart(shoppingCartId)) {

            if (response.getStatus() != Response.Status.NO_CONTENT.getStatusCode()) {
                throw new CandidateOrderBusinessException(
                        "",
                        CandidateOrderErrorCode.CAN_NOT_COMPLETE_SHOPPING_CART,
                        Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()
                );
            }

        } catch (Exception e) {
            throw new CandidateOrderBusinessException(
                    e.getMessage(),
                    CandidateOrderErrorCode.CAN_NOT_COMPLETE_SHOPPING_CART,
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()
            );
        }
    }



    private CandidateOrderDTO getCandidateOrder(UUID shoppingCartId) {
        try (Response response = shoppingCartClient.getOrderCandidate(shoppingCartId)) {

            return response.readEntity(CandidateOrderDTO.class);

        } catch (Exception e) {
            throw new CandidateOrderBusinessException(
                    e.getMessage(),
                    CandidateOrderErrorCode.CAN_NOT_FETCH_SHOPPING_CART,
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()
            );
        }
    }

    private PosRestaurantDTO getRestaurant(Integer restaurantTmplId) {
        try (Response response = restaurantClient.getByFields(
                restaurantTmplId, "id,name,photo,cover,latitude,longitude,address")
        ) {

            return response.readEntity(PosRestaurantDTO.class);

        } catch (Exception e) {
            throw new CandidateOrderBusinessException(
                    e.getMessage(),
                    CandidateOrderErrorCode.CAN_NOT_FETCH_RESTAURANT,
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()
            );
        }
    }

    private UserAddressDTO getUserAddress(Integer addressId) {

        try (Response response = userClient.getByFields(
                addressId, "id,type_address,details,street,latitude,longitude,address,indications,zone_id")
        ) {

            return response.readEntity(UserAddressDTO.class);

        } catch (Exception e) {
            throw new CandidateOrderBusinessException(
                    e.getMessage(),
                    CandidateOrderErrorCode.CAN_NOT_FETCH_USER_ADDRESS,
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()
            );
        }

    }

    private UserZoneDTO getUserZone(UUID userUUID) {

        UserZoneDTO userZoneDTO = userClient.getUserZone(userUUID);

        if (userZoneDTO.zoneId().equals(0)) {
            throw new CandidateOrderBusinessException(
                    "",
                    CandidateOrderErrorCode.CAN_NOT_FETCH_USER_ZONE,
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()
            );
        }

        return userZoneDTO;
    }

    public String getTestCode() {
        return generateCode();
    }

    public static String generateCode() {
        long now = System.currentTimeMillis() - CUSTOM_EPOCH;

        String timePart = Long.toString(now, 36).toUpperCase();
        if (timePart.length() > 3) {
            timePart = timePart.substring(timePart.length() - 3);
        } else {
            timePart = String.format("%3s", timePart).replace(' ', '0');
        }

        String randomPart = generateRandomBase36();

        return timePart + "-" + randomPart;
    }

    private static String generateRandomBase36() {
        StringBuilder sb = new StringBuilder(3);
        for (int i = 0; i < 3; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

}
