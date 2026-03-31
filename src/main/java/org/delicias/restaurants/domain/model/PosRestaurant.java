package org.delicias.restaurants.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
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

}
