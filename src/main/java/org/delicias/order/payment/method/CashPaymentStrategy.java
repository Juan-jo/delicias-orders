package org.delicias.order.payment.method;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.delicias.common.dto.order.OrderStatus;
import org.delicias.order.domain.model.PosOrder;
import org.delicias.order.payment.PaymentMethod;
import org.delicias.order.payment.PaymentStatus;
import org.delicias.order.payment.PaymentStrategy;
import org.delicias.order.payment.dto.PaymentResultDTO;
import org.delicias.order.service.OrderChangeStatusService;

@ApplicationScoped
public class CashPaymentStrategy implements PaymentStrategy {

    private static final PaymentMethod CASH = PaymentMethod.CASH;

    @Inject
    OrderChangeStatusService changeStatusService;

    @Override
    public PaymentResultDTO processPayment(PosOrder order) {

        order.setPaymentMethod(CASH);
        order.setPaymentStatus(PaymentStatus.PENDING);

        order.persist();

        changeStatusService.changeStatus(order, OrderStatus.ORDERED);


        return new PaymentResultDTO(
                CASH,
                "--"
        );
    }

}
