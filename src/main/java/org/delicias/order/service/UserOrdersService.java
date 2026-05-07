package org.delicias.order.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.delicias.common.dto.order.OrderStatus;
import org.delicias.order.domain.model.DeliveryUser;
import org.delicias.order.domain.repository.PosOrderRepository;
import org.delicias.order.dto.UserOrderDTO;
import org.delicias.order.dto.UserOrderReqType;
import org.delicias.rest.security.SecurityContextService;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UserOrdersService {

    @ConfigProperty(name = "delicias.defaultPicture")
    String defaultPicture;

    @Inject
    PosOrderRepository posOrderRepository;

    @Inject
    SecurityContextService security;

    private static final List<OrderStatus> statusInProgress = List.of(
            OrderStatus.ORDERED,
            OrderStatus.ACCEPTED,
            OrderStatus.COOKING,
            OrderStatus.READY_FOR_DELIVERY,
            OrderStatus.DELIVERY_ASSIGNED_ORDER,
            OrderStatus.DELIVERY_ROAD_TO_STORE,
            OrderStatus.DELIVERY_ROAD_TO_DESTINATION,
            OrderStatus.READY_FOR_PICKUP
    );

    public Map<String, Object> loadOrders(UserOrderReqType reqType) {

        long countInProgress = posOrderRepository.countInProgress(
                UUID.fromString(security.userId()),
                statusInProgress
        );


        if(reqType.equals(UserOrderReqType.IN_PROGRESS)) {
            return Map.of(
                    "status", reqType,
                    "data", ordersInProgress(),
                    "totalInProgress", countInProgress
            );
        }

        return Map.of(
                "status", reqType,
                "data", List.of(),
                "totalInProgress", countInProgress
        );

    }

    private List<UserOrderDTO> ordersInProgress() {

        return posOrderRepository.findInProgress(
                UUID.fromString(security.userId()),
                statusInProgress
        ).stream().map(it -> UserOrderDTO.builder()
                .orderId(it.getId())
                .status(it.getStatus())
                .lines(it.getLines().stream().map(li -> {

                    UserOrderDTO.Line.LineBuilder r = UserOrderDTO.Line.builder()
                            .qty(li.getQty())
                            .attributes(li.getAttributes());

                    Optional.ofNullable(li.getProduct()).ifPresentOrElse(p -> {
                        r.name(p.getName());
                        r.pictureUrl(p.getPictureUrl());
                    }, () -> {
                        r.name("Product Unknow");
                        r.pictureUrl(defaultPicture);
                    });

                    return r.build();

                }).toList())
                .restaurant(Optional.ofNullable(it.getRestaurant()).map(res -> UserOrderDTO.Restaurant.builder()
                                .name(res.getName())
                                .pictureUrl(res.getImageLogoUrl())
                                .build())
                        .orElse(
                                UserOrderDTO.Restaurant.builder()
                                        .name("Restaurant Unknow")
                                        .pictureUrl(defaultPicture)
                                        .build()
                        ))
                .deliveryAddress(Optional.ofNullable(it.getUserAddress()).map(ua -> UserOrderDTO.DeliveryAddress.builder()
                                .address(ua.getAddress())
                                .details(ua.getDetails())
                                .street(ua.getStreet())
                                .indications(ua.getIndications())
                                .build())
                        .orElse(
                                UserOrderDTO.DeliveryAddress.builder()
                                        .address("")
                                        .details("")
                                        .street("")
                                        .indications("")
                                        .build()
                        ))
                .deliveryUser(Optional.ofNullable(it.getDeliveryUserOrderRel()).map(u -> UserOrderDTO.DeliveryUser
                        .builder()
                                .name(Optional.ofNullable(u.getDeliveryUser()).map(DeliveryUser::getName).orElse("Desconocido"))
                                .lastName(Optional.ofNullable(u.getDeliveryUser()).map(DeliveryUser::getLastName).orElse("--"))
                                .pictureUrl(Optional.ofNullable(u.getDeliveryUser()).map(DeliveryUser::getPictureUrl).orElse(defaultPicture))
                                .build())
                        .orElse(null))
                .build()).toList();
    }

    private List<UserOrderDTO> ordersHistory() {

        return List.of();
    }


}
