package org.delicias.order.state.machine;

import jakarta.enterprise.context.ApplicationScoped;
import org.delicias.common.dto.order.OrderStatus;
import org.delicias.order.domain.model.PosOrder;
import org.delicias.order.state.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class OrderStateMachine {

    private final Map<OrderStatus, OrderState> states = new HashMap<>();

    public OrderStateMachine() {
        registerState(new OrderedState());
        registerState(new AcceptedState());
        registerState(new CookingState());
        registerState(new ReadyForDeliveryState());
        registerState(new AssignedDeliveryOrderState());
        registerState(new DeliveryOnTheWayToStoreState());
        registerState(new DeliveryOnTheWayToDestinationState());
    }


    private void registerState(OrderState state) {
        states.put(state.getStatus(), state);
    }

    public Optional<PosOrder> handleAction(PosOrder order, OrderStatus nextStatus) {

        OrderState currentState = states.get(order.getStatus());

        if (currentState == null) {
            throw new UnsupportedOperationException();
        }

        return currentState.next(order, nextStatus);
    }
}
