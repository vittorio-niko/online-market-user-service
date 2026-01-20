package org.innowise.internship.userservice.model.entity;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payment_cards")
@EqualsAndHashCode(callSuper = true)
public class PaymentCard extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey =
        @ForeignKey(name = "fk_payment_cards_user_id"))
    private User user;

    @Column(name = "number", nullable = false, unique = true, length = 16)
    private String number;

    @Column(name = "holder", nullable = false, length = 100)
    private String holder;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @Column(name = "active", nullable = false)
    private Boolean active;
}
