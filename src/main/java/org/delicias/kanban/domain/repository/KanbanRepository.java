package org.delicias.kanban.domain.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import org.delicias.common.dto.order.OrderStatus;
import org.delicias.kanban.domain.model.Kanban;

import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class KanbanRepository implements PanacheRepositoryBase<Kanban, Long> {

    public List<Kanban> findKanbanByRestaurant(Integer restaurantId, List<OrderStatus> status) {
        if (status == null || status.isEmpty()) {
            return Collections.emptyList();
        }

        String queryByRestaurant = """
                restaurantId = :restaurantId
                and order.status in (:status)
                """;

        return find(queryByRestaurant,
                Parameters.with("restaurantId", restaurantId)
                        .and("status", status))
                .list();
    }
}
