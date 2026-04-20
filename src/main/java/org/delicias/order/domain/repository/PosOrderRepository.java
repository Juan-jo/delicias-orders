package org.delicias.order.domain.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import org.delicias.common.dto.order.OrderStatus;
import org.delicias.kanban.domain.model.Kanban;
import org.delicias.order.domain.model.PosOrder;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class PosOrderRepository implements PanacheRepositoryBase<PosOrder, Long> {

    private static final String QUERY_IN_PROGRESS = """
        userUUID = :userUUID
        and
        status in (:status)
        """;

    public List<PosOrder> findInProgress(UUID userUUID, List<OrderStatus> status) {
        if (status == null || status.isEmpty()) {
            return Collections.emptyList();
        }

        return find(QUERY_IN_PROGRESS,
                Parameters.with("userUUID", userUUID)
                        .and("status", status))
                .list();
    }

    public long countInProgress(UUID userUUID, List<OrderStatus> status) {
        if (status == null || status.isEmpty()) {
            return 0;
        }

        return count(QUERY_IN_PROGRESS,
                Parameters.with("userUUID", userUUID)
                        .and("status", status));
    }

}
