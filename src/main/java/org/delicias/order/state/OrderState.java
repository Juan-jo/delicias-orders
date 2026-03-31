package org.delicias.order.state;

import org.delicias.common.dto.order.OrderStatus;
import org.delicias.order.domain.model.PosOrder;

import java.util.Optional;

public interface OrderState {

    OrderStatus getStatus();
    Optional<PosOrder> next(PosOrder order, OrderStatus nextStatus);
    boolean canTransitionTo(OrderStatus nextState);

}
