package org.delicias.order.application;

import org.delicias.common.dto.order.OrderStatus;
import org.delicias.order.domain.model.PosOrder;

import java.util.Map;

public interface OrderStateFactory {

    void processAction(Long orderId, OrderStatus status);
    void processAction(Long orderId, OrderStatus status, Map<String, Object> additionalParams);

    void handlePostTransition(PosOrder order, Map<String, Object> additionalParams);

}
