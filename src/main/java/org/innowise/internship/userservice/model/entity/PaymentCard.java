package org.innowise.internship.userservice.model.entity;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ForeignKey;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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
    @JoinColumn(name = "user_id", nullable = false, updatable = false, foreignKey =
        @ForeignKey(name = "fk_payment_cards_user_id"))
    private User user;

    @Column(name = "number", nullable = false, unique = true, updatable = false, length = 16)
    private String number;

    @Column(name = "holder", nullable = false, updatable = false, length = 100)
    private String holder;

    @Column(name = "expiration_date", nullable = false, updatable = false)
    private LocalDate expirationDate;

    @Column(name = "active", nullable = false)
    private Boolean active;
}
