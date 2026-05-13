package org.delicias.order.domain.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.*;
import org.delicias.common.adjusment.OrderAdjustment;
import org.delicias.common.dto.order.OrderStatus;
import org.delicias.restaurants.domain.model.PosRestaurant;
import org.delicias.users.domain.model.PosUserAddress;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "pos_order_history")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PosOrderHistory  extends PanacheEntityBase {

    @Id
    private Long id;

    @Column(name = "zone_id")
    private Integer zoneId;

    @Column(name = "code")
    private String code;

    @Column(name = "user_uuid")
    private UUID userUUID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_tmpl_id")
    private PosRestaurant restaurant;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OrderStatus status;

    private String notes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", name = "adjustments")
    private List<OrderAdjustment> adjustments;

    @Column(name = "total_amount_restaurant", precision = 10, scale = 2)
    private BigDecimal totalAmountRestaurant;

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_address_id")
    private PosUserAddress userAddress;

    @Column(name = "delivery_location", columnDefinition = "GEOGRAPHY(Point, 4326)")
    private Point deliveryLocation;

    @Column(name = "message_canceled", length = 250)
    private String messageCanceled;

    @Column(name = "message_rejected", length = 250)
    private String messageRejected;

    @Column(name = "ordered_at")
    private Instant orderedAt;

    @Column(name = "ready_for_delivery_at")
    private Instant readyForDeliveryAt;

    @Column(name = "delivery_assigned_at")
    private Instant deliveryAssignedAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "canceled_at")
    private Instant canceledAt;

    @Column(name = "rejected_at")
    private Instant rejectedAt;

}
