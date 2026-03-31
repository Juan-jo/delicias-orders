package org.delicias.order.domain.model;


import jakarta.persistence.*;
import lombok.*;
import org.delicias.products.domain.model.PosProduct;
import org.hibernate.annotations.NotFoundAction;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "pos_order_line")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PosOrderLine {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "pos_order_line_id_seq")
    @SequenceGenerator(
            name = "pos_order_line_id_seq",
            allocationSize = 1
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_tmpl_id")
    @org.hibernate.annotations.NotFound(action = NotFoundAction.IGNORE)
    private PosProduct product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    private PosOrder order;

    @Column(columnDefinition = "varchar[]", name = "attributes")
    private List<String> attributes;

    @Column(name = "qty")
    private Short qty;

    @Column(name = "price_unit")
    private BigDecimal priceUnit;

    @Column(name = "price_total")
    private BigDecimal priceTotal;

}
