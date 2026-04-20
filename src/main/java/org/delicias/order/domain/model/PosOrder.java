package org.delicias.order.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.delicias.common.adjusment.OrderAdjustment;
import org.delicias.common.dto.order.OrderStatus;
import org.delicias.restaurants.domain.model.PosRestaurant;
import org.delicias.users.domain.model.PosUserAddress;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
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
    @NotFound(action = NotFoundAction.IGNORE)
    private PosRestaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_address_id")
    @NotFound(action = NotFoundAction.IGNORE)
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
    private List<OrderAdjustment> adjustments = new ArrayList<>();

    @Column(name = "total_amount_restaurant", precision = 10, scale = 2)
    private BigDecimal totalAmountRestaurant;

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "zone_id")
    private Integer zoneId;

    @OrderBy("id asc")
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PosOrderLine> lines = new HashSet<>();

    @Column(name = "delivery_assignment_attempts")
    private Short deliveryAssignmentAttempts;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "ready_for_delivery_date")
    private Instant readyForDeliveryDate;

    @Column(name = "delivery_assigned_date")
    private Instant deliveryAssignedDate;

    @Column(name = "delivered_date")
    private Instant deliveredDate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_user_order_rel_id")
    @NotFound(action = NotFoundAction.IGNORE)
    private DeliveryUserPosOrderRel deliveryUserPosOrderRel;

    public void addLine(PosOrderLine line) {

        if(this.lines == null) {
            this.lines = new HashSet<>();
        }

        line.setOrder(this);
        this.lines.add(line);
    }

}
