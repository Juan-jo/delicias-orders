package org.delicias.delivery_users.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.delicias.common.dto.PagedResult;
import org.delicias.common.dto.order.OrderStatus;
import org.delicias.delivery_users.dto.OrderAssignedItemDTO;
import org.delicias.delivery_users.dto.OrderAssignedType;
import org.delicias.order.domain.repository.DeliveryUserPosOrderRelRepository;
import org.delicias.rest.security.SecurityContextService;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class DeliveryAssignedOrdersService {

    @Inject
    SecurityContextService security;

    @Inject
    DeliveryUserPosOrderRelRepository relRepository;

    @ConfigProperty(name = "delicias.defaultPicture")
    String defaultPicture;

    private static final List<OrderStatus> statusAssigned = List.of(
            OrderStatus.READY_FOR_DELIVERY,
            OrderStatus.DELIVERY_ASSIGNED_ORDER,
            OrderStatus.DELIVERY_ROAD_TO_STORE,
            OrderStatus.DELIVERY_ROAD_TO_DESTINATION
    );

    private static final List<OrderStatus> statusCompleted = List.of(
            OrderStatus.DELIVERED,
            OrderStatus.CANCELLED,
            OrderStatus.REJECTED
    );


    public PagedResult<OrderAssignedItemDTO> loadAssignedOrHistory(
            int page,
            int size,
            OrderAssignedType req
    ) {

        var data = relRepository.listAssigned(
                UUID.fromString(security.userId()),
                req.equals(OrderAssignedType.ASSIGNED) ? statusAssigned : statusCompleted,
                page,
                size
        );


        return new PagedResult<>(
                data.data().stream().map(it -> {

                            OrderAssignedItemDTO.Destination destination = switch (it.getOrder().getStatus()) {
                                case DELIVERY_ASSIGNED_ORDER,
                                     DELIVERY_ROAD_TO_STORE -> OrderAssignedItemDTO.Destination.builder()
                                        .name(it.getOrder().getRestaurant().getName())
                                        .address(it.getOrder().getRestaurant().getAddress())
                                        .pictureUrl(it.getOrder().getRestaurant().getImageLogoUrl())
                                        .build();
                                default -> OrderAssignedItemDTO.Destination.builder()
                                        .name("María J. Martinez")
                                        .pictureUrl(defaultPicture) // TODO add picture User
                                        .address(
                                                String.format("%s. %s", it.getOrder().getUserAddress().getAddress(), it.getOrder().getUserAddress().getStreet())
                                        )
                                        .build();
                            };

                            return OrderAssignedItemDTO.builder()
                                    .id(it.getId())
                                    .status(it.getStatus())
                                    .order(OrderAssignedItemDTO.Order.builder()
                                            .orderId(it.getOrder().getId())
                                            .deliveryAssignedAt(it.getOrder().getDeliveryAssignedAt())
                                            .status(it.getOrder().getStatus())
                                            .destination(destination)
                                            .build()
                                    )
                                    .build();
                        }
                ).toList(),
                data.total(),
                data.page(),
                data.size()
        );
    }
}
