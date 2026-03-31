package org.delicias.order.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.delicias.common.dto.order.OrderStatus;
import org.delicias.order.domain.model.PosOrder;
import org.delicias.order.state.machine.OrderStateMachine;

@ApplicationScoped
public class OrderChangeStatusService {


    @Inject
    OrderStateMachine stateMachine;

    public void changeStatus(PosOrder order, OrderStatus status) {
        stateMachine.handleAction(order, status);
    }

}
