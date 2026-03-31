package org.delicias.order.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.delicias.common.dto.order.CandidateOrderDTO;
import org.delicias.common.dto.order.OrderStatus;
import org.delicias.common.dto.user.UserZoneDTO;
import org.delicias.order.domain.model.PosOrder;
import org.delicias.order.domain.model.PosOrderLine;
import org.delicias.order.domain.repository.PosOrderRepository;
import org.delicias.order.dto.CreateOrderReqDTO;
import org.delicias.order.exception.CandidateOrderBusinessException;
import org.delicias.order.exception.CandidateOrderErrorCode;
import org.delicias.products.domain.model.PosProduct;
import org.delicias.products.service.PosProductService;
import org.delicias.rest.clients.RestaurantClient;
import org.delicias.rest.clients.ShoppingCartClient;
import org.delicias.rest.clients.UserClient;
import org.delicias.rest.security.SecurityContextService;
import org.delicias.restaurants.dto.PosRestaurantDTO;
import org.delicias.restaurants.service.PosRestaurantService;
import org.delicias.users.dto.UserAddressDTO;
import org.delicias.users.service.PosUserAddressService;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import java.time.LocalDateTime;
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
    SecurityContextService security;

    @Transactional
    public void createOrder(CreateOrderReqDTO reqDTO) {

        GeometryFactory geometryFactory = new GeometryFactory();
        UUID userUUID = UUID.fromString(security.userId());

        UserZoneDTO userZoneDTO = getUserZone(userUUID);
        CandidateOrderDTO candidateOrder = getCandidateOrder(reqDTO.shoppingCartId());
        PosRestaurantDTO restaurant = getRestaurant(candidateOrder.restaurantTmplId());
        UserAddressDTO userAddress = getUserAddress(candidateOrder.deliveryAddressId());


        restaurantService.createOrUpdate(restaurant);
        addressService.createOrUpdate(userAddress);
        productService.addProducts(candidateOrder.lines());

        PosOrder order = PosOrder.builder()
                .userUUID(userUUID)
                .restaurantTmplId(candidateOrder.restaurantTmplId())
                .status(OrderStatus.ORDERED)
                .notes(reqDTO.notes())
                .adjustments(candidateOrder.adjustments())
                .totalAmountRestaurant(candidateOrder.subtotal())
                .totalAmount(candidateOrder.total())
                .createdDate(LocalDateTime.now())
                .zoneId(userZoneDTO.zoneId())
                .userAddressId(candidateOrder.deliveryAddressId())
                .deliveryLocation(
                        geometryFactory.createPoint(new Coordinate(userAddress.longitude(), userAddress.latitude()))
                )
                .shoppingCartId(reqDTO.shoppingCartId())
                .build();

        candidateOrder.lines().forEach(it -> {
            PosOrderLine line = PosOrderLine.builder()
                    .product(PosProduct.builder()
                            .id(it.productId())
                            .build())
                    .attributes(it.attributes())
                    .qty(it.qty())
                    .priceUnit(it.priceUnit())
                    .priceTotal(it.priceTotal())
                    .build();

            order.addLine(line);

        });

        posOrderRepository.persist(order);
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

}
