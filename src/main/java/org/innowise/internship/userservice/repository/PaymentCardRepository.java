package org.innowise.internship.userservice.repository;

import org.innowise.internship.userservice.model.entity.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentCardRepository extends
        JpaRepository<PaymentCard, Long>,
        JpaSpecificationExecutor<PaymentCard> {
    // Default methods:
    // - save(PaymentCard paymentCard) for Create/Update
    // - findById(PaymentCard paymentCard) for Get by id
    // - findAll(Pageable pageable) for pagination

    Optional<PaymentCard> findByNumber(String number);

    boolean existsByNumber(String number);

    @Query("""
        SELECT c
        FROM PaymentCard c
        WHERE c.id = :cardId
          AND c.user.id = :userId
    """)
    Optional<PaymentCard> findByIdAndUserId(@Param("cardId") Long cardId,
                                             @Param("userId") Long userId);

    @Query(
            value = "SELECT * FROM payment_cards WHERE user_id = :userId",
            nativeQuery = true
    )
    List<PaymentCard> findAllByUserId(@Param("userId") Long userId);

    @Query(
            value = "SELECT COUNT(*) FROM payment_cards WHERE user_id = :userId",
            nativeQuery = true
    )
    int countPaymentCardsById(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE PaymentCard c SET c.active = :active WHERE c.id = :id")
    int updateActiveStatus(@Param("id") Long id,
                           @Param("active") boolean active);
}

