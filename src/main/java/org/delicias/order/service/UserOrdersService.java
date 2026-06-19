package org.delicias.order.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.delicias.common.dto.order.OrderStatus;
import org.delicias.minio.MinioStorageService;
import org.delicias.minio.utils.MinioRS;
import org.delicias.minio.utils.MinioSize;
import org.delicias.order.domain.model.*;
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

    @Inject
    MinioStorageService minioStorageService;

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
                                        .pictureUrl(minioStorageService.pictureUrl(res.getImageLogoUrl(), MinioSize.SMALL, MinioRS.FIT, (short) 70))
                                        .build())
                                .orElse(
                                        OrderedDTO.Restaurant.builder()
                                                .name("Restaurant Unknow")
                                                .pictureUrl(minioStorageService.pictureUrl(defaultPicture, MinioSize.SMALL, MinioRS.FIT, (short) 70))
                                                .build()
                                ))

                        .build())
                .toList();
    }

    public OrderedDetailDTO getOrderedDetail(Long orderId) {

        PosOrder order = posOrderRepository.findById(orderId);

        if (order == null) {

            return PosOrderHistory.<PosOrderHistory>findByIdOptional(orderId)
                    .map(this::orderHistoryToOrderedDetailDTO)
                    .orElseThrow(() -> new NotFoundException("Order Not Found"));
        }

        return orderToOrderedDetailDTO(order);
    }

    private OrderedDetailDTO orderToOrderedDetailDTO(PosOrder order) {

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
                        .addressType(order.getUserAddress().getTypeAddress())
                        .name(order.getUserAddress().getDetails())
                        .address(List.of(order.getUserAddress().getAddress(), order.getUserAddress().getStreet()))
                        .build())
                .deliveryUser(deliveryUser)
                .build();
    }

    private OrderedDetailDTO orderHistoryToOrderedDetailDTO(PosOrderHistory order) {
        var deliveryUser = Optional.ofNullable(order.getDeliveryUser()).map(rel ->
                OrderedDetailDTO.DeliveryUser.builder()
                        .name(rel.getName())
                        .lastName(rel.getLastName())
                        .pictureUrl(rel.getPictureUrl())
                        .build()
        ).orElse(null);

        BigDecimal subtotal = order.getLines().stream()
                .map(PosOrderLineHistory::getPriceTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String rejectOrCancelMessage = switch (order.getStatus()) {
            case REJECTED -> order.getMessageRejected();
            case CANCELLED -> order.getMessageCanceled();
            default -> null;
        };


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
                        .addressType(order.getUserAddress().getTypeAddress())
                        .address(List.of(order.getUserAddress().getAddress(), order.getUserAddress().getStreet()))
                        .build())
                .deliveryUser(deliveryUser)
                .rejectOrCancelMessage(rejectOrCancelMessage)
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
