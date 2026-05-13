package org.delicias.order.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.delicias.common.dto.order.OrderStatus;
import org.delicias.order.domain.model.PosOrderLine;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class OrderMoveToHistoryService {

    @Inject
    EntityManager entityManager;

    final String moveOrder = """
            INSERT INTO pos_order_history (
            id, zone_id, code, user_uuid, restaurant_tmpl_id, status, notes, adjustments, total_amount_restaurant, total_amount,
            user_address_id, delivery_location, message_canceled, message_rejected,
            ordered_at, ready_for_delivery_at, delivery_assigned_at, delivered_at, canceled_at, rejected_at, delivery_user_id)
            SELECT
            id, zone_id, code, user_uuid, restaurant_tmpl_id, status, notes, adjustments, total_amount_restaurant, total_amount,
            user_address_id, delivery_location, :canceledMessage, :rejectedMessage,
            ordered_at, ready_for_delivery_at, delivery_assigned_at, delivered_at, :canceledAt, :rejectedAt, :deliveryUserId
            FROM pos_order WHERE id = :orderId;
            """;

    final String moveLines = """
            INSERT INTO pos_order_line_history (
            id, order_history_id, product_tmpl_id, qty, price_unit, price_total, "attributes", created_at)
            SELECT id, order_id, product_tmpl_id, qty, price_unit, price_total, "attributes", created_at FROM pos_order_line WHERE order_id = :orderId;
            """;



    @Transactional
    public void moveToHistory(Long orderId, OrderStatus status, String reasonMessage, Integer deliveryUserId) {

        String rejectedMessage = null;
        Instant rejectedAt = null;

        String canceledMessage = null;
        Instant canceledAt = null;

        if (status.equals(OrderStatus.CANCELLED)) {
            canceledMessage = reasonMessage;
            canceledAt = Instant.now();
        } else if (status.equals(OrderStatus.REJECTED)) {
            rejectedMessage = reasonMessage;
            rejectedAt = Instant.now();
        }


        int affectedRows = entityManager.createNativeQuery(moveOrder)
                .setParameter("orderId", orderId)
                .setParameter("rejectedMessage", rejectedMessage)
                .setParameter("rejectedAt", rejectedAt)
                .setParameter("canceledMessage", canceledMessage)
                .setParameter("canceledAt", canceledAt)
                .setParameter("deliveryUserId",deliveryUserId)
                .executeUpdate();

        if (affectedRows == 0) {
            throw new IllegalStateException(
                    "No se encontró la orden con id: " + orderId);
        }

        int affectedRowsLines = entityManager.createNativeQuery(moveLines)
                .setParameter("orderId", orderId)
                .executeUpdate();

        if (affectedRowsLines == 0) {
            throw new IllegalStateException(
                    "No se encontró lines con order_id: " + orderId);
        }

        entityManager.createNativeQuery("DELETE FROM pos_order WHERE id = :id")
                .setParameter("id", orderId)
                .executeUpdate();
    }
}
