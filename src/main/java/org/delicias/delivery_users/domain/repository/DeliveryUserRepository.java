package org.delicias.delivery_users.domain.repository;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import org.delicias.delivery_users.domain.model.DeliveryUserModel;

import java.util.List;

@ApplicationScoped
public class DeliveryUserRepository implements PanacheRepositoryBase<DeliveryUserModel, Integer> {

    private final String queryFilterByZone = "zoneId = ?1 AND LOWER(name) LIKE LOWER(?2)";

    public List<DeliveryUserModel> searchByFilter(
        Integer zoneId,
        String name,
        int page,
        int size,
        String sortBy,
        Sort.Direction direction
    ) {

        PanacheQuery<DeliveryUserModel> query;
        Sort sort = Sort.by(sortBy).direction(direction);

        if (name == null || name.isBlank()) {
            query = find("zoneId = ?1", sort, zoneId);
        } else {
            query = find(queryFilterByZone, sort, zoneId, "%" + name.toLowerCase() + "%");
        }

        return query.page(Page.of(page, size)).list();
    }

    public long countByFilter(
            Integer zoneId,
            String name
    ) {

        if (name == null || name.isBlank()) {
            return count("zoneId = ?1", zoneId);
        }

        return count(queryFilterByZone,zoneId, "%" + name + "%"
        );
    }

}
