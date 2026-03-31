package org.delicias.restaurants.domain.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.delicias.restaurants.domain.model.PosRestaurant;

@ApplicationScoped
public class PosRestaurantRepository implements PanacheRepositoryBase<PosRestaurant, Integer> {
}
