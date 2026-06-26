package org.delicias.order.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import lombok.Builder;
import org.delicias.common.dto.order.OrderStatus;
import org.delicias.kanban.domain.model.Kanban;
import org.delicias.kanban.domain.repository.KanbanRepository;
import org.delicias.kanban.dto.KanbanDTO;
import org.delicias.order.domain.model.PosOrder;
import org.delicias.order.domain.repository.PosOrderRepository;
import org.delicias.order.payment.PaymentMethod;
import org.delicias.order.payment.PaymentStatus;
import org.delicias.order.service.OrderMoveToHistoryService;
import org.delicias.order.state.machine.OrderStateMachine;
import org.delicias.outbox.domain.OutboxEvent;
import org.delicias.outbox.domain.OutboxEventType;
import org.delicias.products.domain.model.PosProduct;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class OrderStateFactoryImpl implements OrderStateFactory {

    @Inject
    private PosOrderRepository orderRepository;

    @Inject
    private OrderStateMachine stateMachine;

    @Inject
    private OrderMoveToHistoryService orderMoveToHistoryService;

    @Inject
    KanbanRepository kanbanRepository;

    @Inject
    ObjectMapper mapper;

    @Transactional
    @Override
    public void processAction(Long orderId, OrderStatus status) {

        processAction(orderId, status, Map.of());
    }

    @Transactional
    @Override
    public void processAction(Long orderId, OrderStatus status, Map<String, Object> additionalParams) {

        Optional.ofNullable(
                        orderRepository.findById(orderId)
                )
                .flatMap(order -> stateMachine.handleAction(order, status))
                .map(orderSaved -> {

                    handlePostTransition(orderSaved, additionalParams);
                    return orderSaved;

                })
                .orElseThrow(() -> new NotFoundException("Order Not Found"));
    }

    @Override
    public void handlePostTransition(PosOrder order, Map<String, Object> additionalParams) {

        switch (order.getStatus()) {
            case ORDERED -> handleOrdered(order);
            case PENDING_PAYMENT -> handlePendingPayment(order);
            case ACCEPTED, COOKING, READY_FOR_DELIVERY, DELIVERY_ASSIGNED_ORDER,
                 DELIVERY_ROAD_TO_STORE, DELIVERY_ROAD_TO_DESTINATION, READY_FOR_PICKUP -> {

                if (order.getStatus().equals(OrderStatus.READY_FOR_DELIVERY)) {
                    order.setReadyForDeliveryAt(Instant.now());
                }

                sendStatusChangedOutboxEvent(order);
                //orderRepository.persist(order);
            }
            case DELIVERED, CANCELLED, REJECTED -> {
                Integer deliveryUserId = null;

                if(order.getStatus().equals(OrderStatus.DELIVERED)) {
                    deliveryUserId = order.getDeliveryUserOrderRel().getDeliveryUser().getId();
                }
                sendStatusChangedOutboxEvent(order);

                // Get reason for cancel or reject
                String message = additionalParams.getOrDefault("message", "").toString();
                orderMoveToHistoryService.moveToHistory(order.getId(), order.getStatus(), message, deliveryUserId);

            }
        }
    }


    private void handleOrdered(PosOrder order) {

        Integer restaurantTmplId = order.getRestaurant().getId();

        Kanban kanban = Kanban.builder()
                .order(order)
                .restaurantId(restaurantTmplId)
                .build();

        kanbanRepository.persist(kanban);

        if(order.getPaymentMethod().equals(PaymentMethod.CARD)) {
            order.setPaymentStatus(PaymentStatus.SUCCEEDED);
        }

        sendOrderedOutboxEvent(order, kanban.getId(), restaurantTmplId);
    }

    private void handlePendingPayment(PosOrder order) {

    }

    private void sendOrderedOutboxEvent(PosOrder order, Long kanbanId, Integer restaurantTmplId) {

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


    private void sendStatusChangedOutboxEvent(PosOrder order) {
        ObjectMapper mapper = new ObjectMapper();

        StatusChanged changed = StatusChanged.builder()
                .orderId(order.getId())
                .status(order.getStatus())
                .restaurantTmplId(order.getRestaurant().getId())
                .userUUID(order.getUserUUID())
                .build();

        OutboxEvent outboxEvent = OutboxEvent.builder()
                .aggregateId(order.getId())
                .type(OutboxEventType.ORDER_STATUS_CHANGED)
                .payload(mapper.valueToTree(changed))
                .createdAt(LocalDateTime.now())
                .build();

        outboxEvent.persist();
    }

    @Builder
    private record StatusChanged(
            Long orderId,
            OrderStatus status,
            Integer restaurantTmplId,
            UUID userUUID
    ) {}
}
