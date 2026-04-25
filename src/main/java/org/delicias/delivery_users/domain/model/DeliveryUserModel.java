package org.delicias.delivery_users.domain.model;


import jakarta.persistence.*;
import lombok.*;
import org.delicias.common.dto.delivery.DeliveryUserStatus;

import java.util.UUID;

@Entity
@Table(name = "delivery_users")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryUserModel {

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

    @Column(name = "username")
    private String username;

    @Column(name = "email")
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private DeliveryUserStatus status;

    @Column(name = "name")
    private String name;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "picture_url")
    private String pictureURL;
}
