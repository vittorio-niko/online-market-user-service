package org.innowise.internship.userservice.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.innowise.internship.userservice.model.dto.request.paymentcard.CreatePaymentCardRequestDto;
import org.innowise.internship.userservice.model.dto.request.user.CreateUserRequestDto;
import org.innowise.internship.userservice.model.dto.request.user.UpdateUserRequestDto;
import org.innowise.internship.userservice.model.entity.PaymentCard;
import org.innowise.internship.userservice.model.entity.User;
import org.innowise.internship.userservice.model.mapper.request.PaymentCardRequestMapper;
import org.innowise.internship.userservice.model.mapper.request.UserRequestMapper;
import org.innowise.internship.userservice.repository.PaymentCardRepository;
import org.innowise.internship.userservice.repository.UserRepository;
import org.innowise.internship.userservice.service.exception.businessexception.*;
import org.innowise.internship.userservice.service.exception.securityexception.KeycloakIdAlreadyExistsException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@RequiredArgsConstructor
@Service
public class UserService {
    private static final int MAX_CARDS_PER_USER = 5;

    private final UserRepository userRepository;
    private final PaymentCardRepository paymentCardRepository;
    private final UserRequestMapper userRequestMapper;
    private final PaymentCardRequestMapper paymentCardRequestMapper;

    @Transactional
    public User createUser(@NonNull CreateUserRequestDto dto) {
        String keycloakId = dto.getKeycloakId();

        if (userRepository.existsByKeycloakId(keycloakId)) {
            throw new KeycloakIdAlreadyExistsException("User with such keycloak id already exists");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyUsedException("User with such email already exists");
        }

        User user = userRequestMapper.toUser(dto);
        user.setActive(true);
        user.setKeycloakId(keycloakId);

        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new UserAlreadyExistsException("User with such email or keycloak id already exists", e);
        }
    }

    @Transactional
    @Caching(
            put = {
                   @CachePut(value = "UserQueryService::getUserById", key = "#id")
           }
    )
    public User updateUserById(Long id, @NonNull UpdateUserRequestDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found by id"));

        // email is immutable identity
        userRequestMapper.updateUserFromDto(dto, user);

        return user;
    }

    @Transactional
    @CacheEvict(value = "UserQueryService::getUserById", key = "#id")
    public void activateUserById(Long id) {
        int updatedRows = userRepository.updateActiveStatus(id, true);
        if (updatedRows == 0) {
            throw new NotFoundException("User not found by id");
        }
    }

    @Transactional
    @CacheEvict(value = "UserQueryService::getUserById", key = "#id")
    public void deactivateUserById(Long id) {
        int updatedRows = userRepository.updateActiveStatus(id, false);
        if (updatedRows == 0) {
            throw new NotFoundException("User not found by id");
        }
    }

    @Transactional
    @CacheEvict(value = "UserQueryService::getUserById", key = "#id")
    public void deleteUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found by id"));
        userRepository.delete(user);
    }

    @Transactional
    public PaymentCard createPaymentCard(@NonNull CreatePaymentCardRequestDto dto) {
        if (paymentCardRepository.existsByNumber(dto.getNumber())) {
            throw new PaymentCardAlreadyUsedException("Card number already exists");
        }

        if (dto.getExpirationDate().isBefore(LocalDate.now())) {
            throw new PaymentCardAlreadyExpiredException("Card is already expired");
        }

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!user.getActive()) {
            throw new InactiveUserException("Inactive user cannot create cards");
        }

        int cardCount = paymentCardRepository.countPaymentCardsById(dto.getUserId());
        if (cardCount >= MAX_CARDS_PER_USER) {
            throw new UserHaveTooManyCardsException(String.format(
                    "User cannot have more than %d cards", MAX_CARDS_PER_USER
            ));
        }

        PaymentCard card = paymentCardRequestMapper.toPaymentCard(dto);
        card.setActive(true);
        card.setUser(user);

        user.getPaymentCards().add(card);

        try {
            userRepository.saveAndFlush(user); // cascade insert
            return paymentCardRepository.findByNumber(card.getNumber())
                    .orElseThrow(() -> new NotFoundException("Card not found (not saved properly)"));
        } catch (DataIntegrityViolationException e) {
            throw new PaymentCardAlreadyUsedException("Card number already exists", e);
        }
    }

    @Transactional
    public void activatePaymentCard(Long userId, Long cardId) {
        User user = userRepository.findByIdWithPaymentCards(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!user.getActive()) {
            throw new InactiveUserException("Cannot activate card for inactive user");
        }

        PaymentCard card = user.getPaymentCards().stream()
                .filter(c -> c.getId().equals(cardId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Card not found for this user"));

        card.setActive(true);
        userRepository.save(user);
    }

    @Transactional
    public void deactivatePaymentCard(Long userId, Long cardId) {
        User user = userRepository.findByIdWithPaymentCards(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        PaymentCard card = user.getPaymentCards().stream()
                .filter(c -> c.getId().equals(cardId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Card not found for this user"));

        card.setActive(false);
        userRepository.save(user);
    }

    @Transactional
    public void deletePaymentCard(Long userId, Long cardId) {
        User user = userRepository.findByIdWithPaymentCards(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!user.getActive()) {
            throw new InactiveUserException("Inactive user cannot delete card");
        }

        boolean removed = user.getPaymentCards().removeIf(card ->
                card.getId().equals(cardId));

        if (!removed) {
            throw new NotFoundException("Card not found for this user");
        }

        userRepository.save(user);
    }
}
