package org.delicias.order.domain.model;

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
import java.util.*;

@Entity
@Table(name = "pos_order")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PosOrder {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "pos_order_id_seq")
    @SequenceGenerator(
            name = "pos_order_id_seq",
            allocationSize = 1
    )
    private Long id;

    @Column(name = "user_uuid")
    private UUID userUUID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_tmpl_id")
    private PosRestaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_address_id")
    private PosUserAddress userAddress;

    @Column(name = "shoppingcart_id")
    private UUID shoppingCartId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", columnDefinition = "pos_order_status")
    private OrderStatus status;

    @Column(name = "delivery_location", columnDefinition = "GEOGRAPHY(Point, 4326)")
    private Point deliveryLocation;

    private String notes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", name = "adjustments")
    private List<OrderAdjustment> adjustments;

    @Column(name = "total_amount_restaurant", precision = 10, scale = 2)
    private BigDecimal totalAmountRestaurant;

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "zone_id")
    private Integer zoneId;

    @Column(name = "code")
    private String code;

    @OrderBy("id asc")
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<PosOrderLine> lines;

    @Column(name = "delivery_assignment_attempts")
    private Short deliveryAssignmentAttempts;

    @Column(name = "ordered_at")
    private Instant orderedAt;

    @Column(name = "ready_for_delivery_at")
    private Instant readyForDeliveryAt;


    @Column(name = "delivery_assigned_at")
    private Instant deliveryAssignedAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_user_order_rel_id")
    private DeliveryUserPosOrderRel deliveryUserOrderRel;

    public void addLine(PosOrderLine line) {

        if(this.lines == null) {
            this.lines = new HashSet<>();
        }

        line.setOrder(this);
        this.lines.add(line);
    }

}
