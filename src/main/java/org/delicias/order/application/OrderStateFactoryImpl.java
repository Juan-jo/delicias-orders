package org.delicias.order.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.delicias.common.dto.order.OrderStatus;
import org.delicias.order.domain.model.PosOrder;
import org.delicias.order.domain.repository.PosOrderRepository;
import org.delicias.order.state.machine.OrderStateMachine;

import java.util.Map;
import java.util.Optional;

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
                .map(order -> {
                    orderRepository.persist(order);
                    return order;
                })
                .map(orderSaved -> {

                    handlePostTransition(orderSaved, additionalParams);
                    return orderSaved;

                })
                .orElseThrow(() -> new NotFoundException("Order Not Found"));
    }

    @Override
    public void handlePostTransition(PosOrder order, Map<String, Object> additionalParams) {

        // TODO Add actions when change status
        switch (order.getStatus()) {


        }
    }
}
