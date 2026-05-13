package org.delicias.order.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.delicias.common.dto.order.OrderStatus;
import org.delicias.order.domain.model.DeliveryUser;
import org.delicias.order.domain.model.PosOrderLine;
import org.delicias.order.domain.repository.PosOrderRepository;
import org.delicias.order.dto.*;
import org.delicias.rest.security.SecurityContextService;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.math.BigDecimal;
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

    @Inject
    OrderChangeStatusService orderChangeStatusService;

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

    public List<OrderedDTO> getOrdered() {

        return posOrderRepository.findInProgress(
                        UUID.fromString(security.userId()),
                        statusInProgress
                ).stream().map(it -> OrderedDTO.builder()
                        .orderId(it.getId())
                        .code(it.getCode())
                        .status(it.getStatus())
                        .orderedAt(it.getOrderedAt())
                        .restaurant(Optional.ofNullable(it.getRestaurant()).map(res -> OrderedDTO.Restaurant.builder()
                                        .name(res.getName())
                                        .pictureUrl(res.getImageLogoUrl())
                                        .build())
                                .orElse(
                                        OrderedDTO.Restaurant.builder()
                                                .name("Restaurant Unknow")
                                                .pictureUrl(defaultPicture)
                                                .build()
                                ))

                        .build())
                .toList();
    }

    public OrderedDetailDTO getOrderedDetail(Long orderId) {

        var order = posOrderRepository.findById(orderId);

        if (order == null) {
            throw new NotFoundException("Order Not Found");
        }

        var deliveryUser = Optional.ofNullable(order.getDeliveryUserOrderRel()).map(rel ->
                OrderedDetailDTO.DeliveryUser.builder()
                        .name(rel.getDeliveryUser().getName())
                        .lastName(rel.getDeliveryUser().getLastName())
                        .pictureUrl(rel.getDeliveryUser().getPictureUrl())
                        .build()
        ).orElse(null);

        BigDecimal subtotal = order.getLines().stream()
                .map(PosOrderLine::getPriceTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return OrderedDetailDTO.builder()
                .orderId(order.getId())
                .code(order.getCode())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .subtotalAmount(subtotal)
                .adjustments(order.getAdjustments().stream().map(ad -> OrderedDetailDTO.Adjustment.builder()
                        .name(ad.getName())
                        .amount(BigDecimal.valueOf(ad.getAmount()))
                        .build()).toList())
                .lines(order.getLines().stream().map(line -> OrderedDetailDTO.Line.builder()
                        .name(line.getProduct().getName())
                        .pictureUrl(line.getProduct().getPictureUrl())
                        .qty(line.getQty())
                        .attributes(line.getAttributes())
                        .total(line.getPriceTotal())
                        .build()).toList())
                .deliveryAddress(OrderedDetailDTO.DeliveryAddress.builder()
                        .name(order.getUserAddress().getDetails())
                        .address(List.of(order.getUserAddress().getAddress(), order.getUserAddress().getStreet()))
                        .build())
                .deliveryUser(deliveryUser)
                .build();
    }

    public void cancelOrder(Long orderId, CancelOrderReqDTO reqDTO) {

        var order = posOrderRepository.findById(orderId);

        if(order == null) {
            throw new NotFoundException("Order Not Found");
        }

        orderChangeStatusService.changeStatus(order, OrderStatus.CANCELLED, Map.of("message", reqDTO.message()));
    }

}
