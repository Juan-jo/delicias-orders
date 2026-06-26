package org.delicias.order.domain.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.*;
import org.delicias.order.payment.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@Entity
@Table(name = "payment_transaction")
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentTransaction extends PanacheEntityBase {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "pos_payment_transaction_id_seq")
    @SequenceGenerator(
            name = "pos_payment_transaction_id_seq",
            allocationSize = 1
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private PosOrder order;

    @Column(name = "provider")
    private String provider;

    @Column(name = "transaction_id")
    private String transactionId;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(name = "amount", precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency")
    private String currency;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public static Optional<PaymentTransaction> findByTransactionId(String transactionId) {
        return find("transactionId", transactionId).firstResultOptional();
    }

}
