package org.delicias.order.domain.model;

import jakarta.persistence.*;
import lombok.*;
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_user_id", referencedColumnName = "id")
    @NotFound(action = NotFoundAction.IGNORE)
    private DeliveryUser deliveryUser;

    @Column(name = "order_id")
    private Long orderId;

    private String status;

    @Column(name = "created_at")
    private Instant createdAt;
}
