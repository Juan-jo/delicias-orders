package org.delicias.order.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.delicias.common.dto.order.OrderTrackingStatus;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "delivery_user_order_rel")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryUserPosOrderRel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_user_id", referencedColumnName = "id")
    private DeliveryUser deliveryUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private PosOrder order;

    @Enumerated(EnumType.STRING)
    private OrderTrackingStatus status;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "route_geometry", columnDefinition = "text")
    private String route;

    @Column(name = "distance")
    private Double distance;

    @Column(name = "duration")
    private Double duration;

    @Column(name = "last_lat")
    private Double lastLat;

    @Column(name = "last_lng")
    private Double lastLng;
}
