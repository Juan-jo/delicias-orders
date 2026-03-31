package org.delicias.order.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.delicias.common.adjusment.OrderAdjustment;
import org.delicias.common.dto.order.OrderStatus;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @Column(name = "restaurant_tmpl_id")
    private Integer restaurantTmplId;

    @Column(name = "user_address_id")
    private Integer userAddressId;

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

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @OrderBy("id asc")
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PosOrderLine> lines = new HashSet<>();;

    public void addLine(PosOrderLine line) {

        if(this.lines == null) {
            this.lines = new HashSet<>();
        }

        line.setOrder(this);
        this.lines.add(line);
    }

}
