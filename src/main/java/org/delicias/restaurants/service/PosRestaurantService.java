package org.delicias.restaurants.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.delicias.restaurants.domain.model.PosRestaurant;
import org.delicias.restaurants.domain.repository.PosRestaurantRepository;
import org.delicias.restaurants.dto.PosRestaurantDTO;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import java.util.Optional;

@ApplicationScoped
public class PosRestaurantService {

    @Inject
    PosRestaurantRepository repository;

    public void createOrUpdate(PosRestaurantDTO dto) {

        GeometryFactory geometryFactory = new GeometryFactory();


        PosRestaurant restaurant = Optional.ofNullable(repository.findById(dto.id()))
                .map(r -> {

                    r.setName(dto.name());
                    r.setAddress(dto.address());
                    r.setImageLogoUrl(dto.photo());
                    r.setPosition(geometryFactory.createPoint(new Coordinate(dto.longitude(), dto.latitude())));

                    return r;
                })
                .orElse(PosRestaurant.builder()
                        .id(dto.id())
                        .name(dto.name())
                        .address(dto.address())
                        .imageLogoUrl(dto.photo())
                        .position(geometryFactory.createPoint(new Coordinate(dto.longitude(), dto.latitude())))
                        .build());

        repository.persist(restaurant);
    }
}
