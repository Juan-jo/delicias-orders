package org.delicias.order.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import lombok.Builder;
import org.delicias.common.dto.order.OrderStatus;
import org.delicias.order.domain.model.PosOrder;
import org.delicias.order.domain.repository.PosOrderRepository;
import org.delicias.order.state.machine.OrderStateMachine;
import org.delicias.outbox.domain.OutboxEvent;
import org.delicias.outbox.domain.OutboxEventType;

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

    @Transactional
    @Override
    public void handlePostTransition(PosOrder order, Map<String, Object> additionalParams) {

        // TODO Add actions when change status

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


        if(order.getStatus().equals(OrderStatus.READY_FOR_DELIVERY)) {
            order.setReadyForDeliveryAt(Instant.now());
        }

        orderRepository.persist(order);
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
