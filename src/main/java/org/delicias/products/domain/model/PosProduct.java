package org.delicias.products.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pos_product_info")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PosProduct {

    @Id
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "picture_logo_url")
    private String pictureUrl;
}
