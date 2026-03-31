package org.delicias.order.state;

import org.delicias.common.dto.order.OrderStatus;
import org.delicias.order.domain.model.PosOrder;
import org.delicias.order.exception.InvalidTransitionStatusOrderException;

import java.util.Optional;

public class OrderedState implements OrderState {

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.ORDERED;
    }


    @Override
    public Optional<PosOrder> next(PosOrder order, OrderStatus nextStatus) {

        if(canTransitionTo(nextStatus)) {
            order.setStatus(nextStatus);
            return Optional.of(order);
        }

        throw new InvalidTransitionStatusOrderException(nextStatus.name());

    }

    @Override
    public boolean canTransitionTo(OrderStatus nextState) {
        return nextState.equals(OrderStatus.ACCEPTED) ||
                nextState.equals(OrderStatus.COOKING) ||
                nextState.equals(OrderStatus.REJECTED) ||
                nextState.equals(OrderStatus.CANCELLED) ||
                nextState.equals(OrderStatus.READY_FOR_DELIVERY);
    }
}
