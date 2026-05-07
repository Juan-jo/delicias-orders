package org.delicias.order.domain.repository;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import org.delicias.common.dto.PagedResult;
import org.delicias.common.dto.order.OrderStatus;
import org.delicias.order.domain.model.DeliveryUserPosOrderRel;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class DeliveryUserPosOrderRelRepository implements PanacheRepositoryBase<DeliveryUserPosOrderRel, UUID> {

    private static final String QUERY_ASSIGNED = """
        deliveryUser.deliveryUUID = :deliveryUUID
        and
        order.status in (:status)
        """;

    public PagedResult<DeliveryUserPosOrderRel> listAssigned(
            UUID deliveryUUID,
            List<OrderStatus> status,
            int page,
            int size
    ) {

        if (status == null || status.isEmpty()) {
            return new PagedResult<>(Collections.emptyList(), 0, page, size);
        }

        PanacheQuery<DeliveryUserPosOrderRel> query = find(
                QUERY_ASSIGNED,
                Parameters.with("deliveryUUID", deliveryUUID)
                        .and("status", status)
        );

        long total = query.count();

        List<DeliveryUserPosOrderRel> data = query
                .page(Page.of(page, size))
                .list();

        return new PagedResult<>(data, total, page, size);
    }

}
