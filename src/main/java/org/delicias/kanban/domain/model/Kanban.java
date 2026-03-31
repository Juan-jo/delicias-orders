package org.delicias.kanban.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.delicias.order.domain.model.PosOrder;

@Entity
@Table(name = "pos_restaurant_kanban")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Kanban {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "pos_restaurant_kanban_id_seq")
    @SequenceGenerator(
            name = "pos_restaurant_kanban_id_seq",
            allocationSize = 1
    )
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    private PosOrder order;

    @Column(name = "restaurant_id")
    private Integer restaurantId;

}
