package org.delicias.order.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.delicias.products.domain.model.PosProduct;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "pos_order_line_history")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PosOrderLineHistory {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_tmpl_id")
    private PosProduct product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_history_id", referencedColumnName = "id")
    private PosOrderHistory order;

    @Column(columnDefinition = "varchar[]", name = "attributes")
    private List<String> attributes;

    @Column(name = "qty")
    private Short qty;

    @Column(name = "price_unit")
    private BigDecimal priceUnit;

    @Column(name = "price_total")
    private BigDecimal priceTotal;

}