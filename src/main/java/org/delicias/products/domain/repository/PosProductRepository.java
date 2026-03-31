package org.delicias.products.domain.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.delicias.products.domain.model.PosProduct;

@ApplicationScoped
public class PosProductRepository implements PanacheRepositoryBase<PosProduct, Integer> {

}
