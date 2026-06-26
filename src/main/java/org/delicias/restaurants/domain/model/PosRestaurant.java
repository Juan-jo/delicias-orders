package org.delicias.restaurants.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.delicias.common.dto.restaurant.StoreType;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "pos_restaurant_info")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PosRestaurant {

    @Id
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "address")
    private String address;

    @Column(name = "image_logo_url")
    private String imageLogoUrl;

    @Column(name = "position", columnDefinition = "GEOGRAPHY(Point, 4326)")
    private Point position;

    @Enumerated(EnumType.STRING)
    @Column(name = "store_type")
    private StoreType storeType;

    public PosRestaurant(Integer id) {
        this.id = id;
    }
}
