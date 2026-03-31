package org.delicias.users.domain.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.delicias.users.domain.model.PosUserAddress;

@ApplicationScoped
public class PosUserAddressRepository implements PanacheRepositoryBase<PosUserAddress, Integer> {
}
