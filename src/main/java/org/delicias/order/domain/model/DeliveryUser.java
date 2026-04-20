package org.delicias.order.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

import java.util.UUID;

@Entity
@Table(name = "delivery_users")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryUser {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "delivery_user_id_seq")
    @SequenceGenerator(
            name = "delivery_user_id_seq",
            allocationSize = 1
    )
    private Integer id;

    @Column(name = "delivery_uuid")
    private UUID deliveryUUID;

    @Column(name = "zone_id")
    private Integer zoneId;

    @Column(name = "status")
    private String status;

    @Column(name = "last_position", columnDefinition = "GEOGRAPHY(Point, 4326)")
    private Point lastPosition;

    @Column(name = "name")
    private String name;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "picture_url")
    private String pictureUrl;
}
