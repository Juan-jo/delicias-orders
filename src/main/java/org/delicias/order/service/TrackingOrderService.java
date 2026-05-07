package org.delicias.order.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.delicias.common.dto.order.OrderStatus;
import org.delicias.common.dto.order.OrderTrackingStatus;
import org.delicias.order.domain.model.DeliveryUserPosOrderRel;
import org.delicias.order.domain.model.PosOrder;
import org.delicias.order.domain.repository.DeliveryUserPosOrderRelRepository;
import org.delicias.order.domain.repository.PosOrderRepository;
import org.delicias.order.dto.TrackingDTO;
import org.delicias.order.dto.TrackingEtaDTO;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class TrackingOrderService {

    @Inject
    OrderChangeStatusService orderChangeStatusService;

    @Inject
    DeliveryUserPosOrderRelRepository relRepository;

    @Inject
    PosOrderRepository orderRepository;

    @ConfigProperty(name = "delicias.defaultPicture")
    String defaultPicture;

    @Transactional
    public TrackingDTO startToStore(UUID id) {

        DeliveryUserPosOrderRel rel = relRepository.findById(id);

        if(rel == null) {
            throw new NotFoundException("DeliveryUserPosOrderRel Not Found");
        }

        PosOrder order = rel.getOrder();

        orderChangeStatusService.changeStatus(order, OrderStatus.DELIVERY_ROAD_TO_STORE);

        rel.setStatus(OrderTrackingStatus.ROAD_TO_STORE);

        // TODO -> Add Outbox

        return mapToTrackingDTO(rel);
    }

    @Transactional
    public TrackingDTO startToDestination(UUID id) {

        DeliveryUserPosOrderRel rel = relRepository.findById(id);

        if(rel == null) {
            throw new NotFoundException("DeliveryUserPosOrderRel Not Found");
        }

        PosOrder order = rel.getOrder();

        orderChangeStatusService.changeStatus(order, OrderStatus.DELIVERY_ROAD_TO_DESTINATION);

        rel.setStatus(OrderTrackingStatus.ROAD_TO_DELIVERY);

        // TODO -> Add Outbox

        return mapToTrackingDTO(rel);
    }

    public TrackingDTO continueTracking(UUID id) {

        DeliveryUserPosOrderRel rel = relRepository.findById(id);

        if (rel == null) {
            throw new NotFoundException("DeliveryUserPosOrderRel Not Found");
        }

        return mapToTrackingDTO(rel);
    }


    @Transactional
    public Map<String, Object> trackingComplete(UUID id) {

        DeliveryUserPosOrderRel rel = relRepository.findById(id);

        if(rel == null) {
            throw new NotFoundException("DeliveryUserPosOrderRel Not Found");
        }
        PosOrder order = rel.getOrder();
        orderChangeStatusService.changeStatus(order, OrderStatus.DELIVERED);
        rel.setStatus(OrderTrackingStatus.DELIVERED);

        return Map.of("success", true);
    }

    public TrackingEtaDTO trackingEta(Long orderId) {

        PosOrder order = orderRepository.findById(orderId);

        if(order == null) {
            throw new NotFoundException("Order Not Found");
        }

        if(order.getStatus().equals(OrderStatus.DELIVERY_ROAD_TO_STORE) ||
                order.getStatus().equals(OrderStatus.DELIVERY_ROAD_TO_DESTINATION)) {

            return TrackingEtaDTO.builder()
                    .orderId(order.getId())
                    .lat(order.getDeliveryUserOrderRel().getLastLat())
                    .lng(order.getDeliveryUserOrderRel().getLastLng())
                    .distance(order.getDeliveryUserOrderRel().getDistance())
                    .duration(order.getDeliveryUserOrderRel().getDuration())
                    .route(order.getDeliveryUserOrderRel().getRoute())
                    .build();
        }

        throw new NotFoundException("Tracking Not Found");
    }


    private TrackingDTO mapToTrackingDTO(DeliveryUserPosOrderRel rel) {

        PosOrder order = rel.getOrder();

        TrackingDTO.Destination destination = switch (order.getStatus()) {
            case DELIVERY_ROAD_TO_STORE -> TrackingDTO.Destination.builder()
                    .name(order.getRestaurant().getName())
                    .address(order.getRestaurant().getAddress())
                    .pictureUrl(order.getRestaurant().getImageLogoUrl())
                    .latitude(order.getRestaurant().getPosition().getY())
                    .longitude(order.getRestaurant().getPosition().getX())
                    .build();
            case DELIVERY_ROAD_TO_DESTINATION -> TrackingDTO.Destination.builder()
                    .name("María J. Martinez")
                    .pictureUrl(defaultPicture) // TODO add picture User
                    .street(order.getUserAddress().getStreet())
                    .address(
                            String.format("%s. %s", order.getUserAddress().getAddress(), order.getUserAddress().getStreet())
                    )
                    .indications(order.getUserAddress().getIndications())
                    .latitude(order.getDeliveryLocation().getY())
                    .longitude(order.getDeliveryLocation().getX())
                    .build();
            default -> null;
        };


        return TrackingDTO.builder()
                .deliveryUserOrderRelId(rel.getId())
                .status(rel.getStatus())
                .order(TrackingDTO.Order.builder()
                        .orderId(order.getId())
                        .status(order.getStatus())
                        .code("MX-20172") // TODO Add codes
                        .destination(destination)
                        .build())
                .build();
    }

}
