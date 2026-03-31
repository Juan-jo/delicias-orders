package org.delicias.order.domain.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.delicias.order.domain.model.PosOrder;

@ApplicationScoped
public class PosOrderRepository implements PanacheRepositoryBase<PosOrder, Long> {


}
