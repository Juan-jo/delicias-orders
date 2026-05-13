package org.delicias.order.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.delicias.common.dto.order.OrderStatus;
import org.delicias.order.application.OrderStateFactory;
import org.delicias.order.domain.model.PosOrder;

import java.util.Map;

@ApplicationScoped
public class OrderChangeStatusService {


    @Inject
    OrderStateFactory stateFactory;

    public void changeStatus(PosOrder order, OrderStatus status) {
        stateFactory.processAction(order.getId(), status);
    }

    public void changeStatus(PosOrder order, OrderStatus status, Map<String, Object> additionalParams) {
        stateFactory.processAction(order.getId(), status, additionalParams);
    }

}
