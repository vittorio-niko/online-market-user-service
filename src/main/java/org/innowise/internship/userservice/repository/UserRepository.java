package org.innowise.internship.userservice.repository;

import org.innowise.internship.userservice.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserRepository extends
        JpaRepository<User, Long>,
        JpaSpecificationExecutor<User> {
    // Default methods:
    // - save(User user) for Create/Update
    // - findById(User user) for Get by id
    // - findAll(Pageable pageable) for pagination

    boolean existsByEmail(String email);

    @Query("""
           SELECT u
           FROM User u
           LEFT JOIN FETCH u.paymentCards
           WHERE u.id = :id
           """)
    Optional<User> findByIdWithPaymentCards(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.active = :active WHERE u.id = :id")
    int updateActiveStatus(@Param("id") Long id,
                           @Param("active") boolean active);

    Optional<User> findByKeycloakId(String keycloakId);

    boolean existsByKeycloakId(String keycloakId);

    @Query("SELECT u.id FROM User u WHERE u.keycloakId = :keycloakId")
    Optional<Long> findIdByKeycloakId(@Param("keycloakId") String keycloakId);
}
