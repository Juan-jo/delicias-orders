package org.delicias.users.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "pos_user_addresss")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PosUserAddress {

    @Id
    private Integer id;

    @Column(name = "type_address")
    private String typeAddress;

    @Column(name = "details")
    private String details;

    @Column(name = "street")
    private String street;

    @Column(name = "address")
    private String address;

    @Column(name = "indications")
    private String indications;
}
