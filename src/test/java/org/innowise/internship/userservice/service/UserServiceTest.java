package org.innowise.internship.userservice.service;

import org.innowise.internship.userservice.model.dto.request.paymentcard.CreatePaymentCardRequestDto;
import org.innowise.internship.userservice.model.dto.request.user.CreateUserRequestDto;
import org.innowise.internship.userservice.model.dto.request.user.UpdateUserRequestDto;
import org.innowise.internship.userservice.model.entity.PaymentCard;
import org.innowise.internship.userservice.model.entity.User;
import org.innowise.internship.userservice.model.mapper.request.PaymentCardRequestMapper;
import org.innowise.internship.userservice.model.mapper.request.UserRequestMapper;
import org.innowise.internship.userservice.repository.PaymentCardRepository;
import org.innowise.internship.userservice.repository.UserRepository;
import org.innowise.internship.userservice.service.exception.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock private UserRepository userRepository;
    @Mock private PaymentCardRepository paymentCardRepository;
    @Mock private UserRequestMapper userRequestMapper;
    @Mock private PaymentCardRequestMapper paymentCardRequestMapper;

    @InjectMocks
    private UserService userService;

    // createUser
    @Test
    @DisplayName("Should create user successfully")
    void createUser_shouldCreateSuccessfully() {
        CreateUserRequestDto dto = new CreateUserRequestDto();
        dto.setEmail("test@mail.com");

        User user = new User();

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(userRequestMapper.toUser(dto)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.createUser(dto);

        assertTrue(result.getActive());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should throw EmailAlreadyUsedException when creating user with existing email")
    void createUser_shouldThrowEmailAlreadyUsed() {
        CreateUserRequestDto dto = new CreateUserRequestDto();
        dto.setEmail("test@mail.com");

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThrows(EmailAlreadyUsedException.class,
                () -> userService.createUser(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw EmailAlreadyUsedException when data integrity violation occurs")
    void createUser_shouldThrowEmailAlreadyUsedOnDataIntegrityViolation() {
        CreateUserRequestDto dto = new CreateUserRequestDto();
        dto.setEmail("test@mail.com");

        User user = new User();
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(userRequestMapper.toUser(dto)).thenReturn(user);
        when(userRepository.save(user))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThrows(EmailAlreadyUsedException.class,
                () -> userService.createUser(dto));
    }

    // updateUserById
    @Test
    @DisplayName("Should update user successfully")
    void updateUserById_shouldUpdateUserSuccessfully() {
        Long userId = 1L;
        UpdateUserRequestDto dto = new UpdateUserRequestDto();
        User user = new User();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User result = userService.updateUserById(userId, dto);

        verify(userRequestMapper).updateUserFromDto(dto, user);
        assertEquals(user, result);
    }

    @Test
    @DisplayName("Should throw NotFoundException when updating non-existent user")
    void updateUserById_shouldThrowNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> userService.updateUserById(1L, new UpdateUserRequestDto()));
    }

    // activateUserById / deactivateUserById
    @Test
    @DisplayName("Should activate user successfully")
    void activateUserById_shouldActivateSuccessfully() {
        when(userRepository.updateActiveStatus(1L, true)).thenReturn(1);

        assertDoesNotThrow(() -> userService.activateUserById(1L));
    }

    @Test
    @DisplayName("Should throw NotFoundException when activating non-existent user")
    void activateUserById_shouldThrowNotFound() {
        when(userRepository.updateActiveStatus(1L, true)).thenReturn(0);

        assertThrows(NotFoundException.class, () -> userService.activateUserById(1L));
    }

    @Test
    @DisplayName("Should deactivate user successfully")
    void deactivateUserById_shouldDeactivateSuccessfully() {
        when(userRepository.updateActiveStatus(1L, false)).thenReturn(1);

        assertDoesNotThrow(() -> userService.deactivateUserById(1L));
    }

    @Test
    @DisplayName("Should throw NotFoundException when deactivating non-existent user")
    void deactivateUserById_shouldThrowNotFound() {
        when(userRepository.updateActiveStatus(1L, false)).thenReturn(0);

        assertThrows(NotFoundException.class, () -> userService.deactivateUserById(1L));
    }

    // deleteUserById
    @Test
    @DisplayName("Should delete user successfully")
    void deleteUserById_shouldDeleteSuccessfully() {
        User user = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUserById(1L);

        verify(userRepository).delete(user);
    }

    @Test
    @DisplayName("Should throw NotFoundException when deleting non-existent user")
    void deleteUserById_shouldThrowNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.deleteUserById(1L));
    }

    // createPaymentCard
    @Test
    @DisplayName("Should create payment card successfully")
    void createPaymentCard_shouldCreateSuccessfully() {
        CreatePaymentCardRequestDto dto = new CreatePaymentCardRequestDto();
        dto.setUserId(1L);
        dto.setNumber("1111222233334444");
        dto.setExpirationDate(LocalDate.now().plusDays(1));

        User user = new User();
        user.setActive(true);
        user.setPaymentCards(new ArrayList<>());

        PaymentCard card = new PaymentCard();

        when(paymentCardRepository.existsByNumber(dto.getNumber())).thenReturn(false);
        when(userRepository.findById(dto.getUserId())).thenReturn(Optional.of(user));
        when(paymentCardRequestMapper.toPaymentCard(dto)).thenReturn(card);
        when(paymentCardRepository.countPaymentCardsById(dto.getUserId())).thenReturn(0);

        card.setId(1L); // id generated
        when(userRepository.saveAndFlush(user)).thenReturn(user);
        card.setActive(true); // active status is attached
        when(paymentCardRepository.findByNumber(any())).thenReturn(Optional.of(card));

        PaymentCard result = userService.createPaymentCard(dto);

        assertTrue(result.getActive());
        assertEquals(user, result.getUser());
        assertTrue(user.getPaymentCards().contains(result));
    }

    @Test
    @DisplayName("Should throw PaymentCardAlreadyUsedException when creating card with existing number")
    void createPaymentCard_shouldThrowPaymentCardAlreadyUsed() {
        CreatePaymentCardRequestDto dto = new CreatePaymentCardRequestDto();
        dto.setNumber("1111");

        when(paymentCardRepository.existsByNumber(dto.getNumber())).thenReturn(true);

        assertThrows(PaymentCardAlreadyUsedException.class,
                () -> userService.createPaymentCard(dto));
    }

    @Test
    @DisplayName("Should throw PaymentCardAlreadyExpiredException when creating expired card")
    void createPaymentCard_shouldThrowPaymentCardAlreadyExpired() {
        CreatePaymentCardRequestDto dto = new CreatePaymentCardRequestDto();
        dto.setExpirationDate(LocalDate.now().minusDays(1));

        assertThrows(PaymentCardAlreadyExpiredException.class,
                () -> userService.createPaymentCard(dto));
    }

    @Test
    @DisplayName("Should throw NotFoundException when creating card for non-existent user")
    void createPaymentCard_shouldThrowNotFound() {
        CreatePaymentCardRequestDto dto = new CreatePaymentCardRequestDto();
        dto.setUserId(1L);
        dto.setExpirationDate(LocalDate.now().plusDays(1));
        when(paymentCardRepository.existsByNumber(dto.getNumber())).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> userService.createPaymentCard(dto));
    }

    @Test
    @DisplayName("Should throw InactiveUserException when creating card for inactive user")
    void createPaymentCard_shouldThrowInactiveUser() {
        CreatePaymentCardRequestDto dto = new CreatePaymentCardRequestDto();
        dto.setUserId(1L);
        dto.setExpirationDate(LocalDate.now().plusDays(1));

        User user = new User();
        user.setActive(false);

        when(paymentCardRepository.existsByNumber(any())).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(InactiveUserException.class,
                () -> userService.createPaymentCard(dto));
    }

    @Test
    @DisplayName("Should throw UserHaveTooManyCardsException when user has maximum cards")
    void createPaymentCard_shouldThrowUserHaveTooManyCards() {
        CreatePaymentCardRequestDto dto = new CreatePaymentCardRequestDto();
        dto.setUserId(1L);
        dto.setExpirationDate(LocalDate.now().plusDays(1));

        User user = new User();
        user.setActive(true);

        when(paymentCardRepository.existsByNumber(any())).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(paymentCardRepository.countPaymentCardsById(1L)).thenReturn(5);

        assertThrows(UserHaveTooManyCardsException.class,
                () -> userService.createPaymentCard(dto));
    }

    @Test
    @DisplayName("Should throw PaymentCardAlreadyExpiredException when card expiration date is in past")
    void createPaymentCard_shouldThrowPaymentCardAlreadyExpiredForPastDate() {
        CreatePaymentCardRequestDto dto = new CreatePaymentCardRequestDto();
        dto.setUserId(1L);
        dto.setNumber("1234567812345678");
        dto.setExpirationDate(LocalDate.of(1980, 1, 1));

        when(paymentCardRepository.existsByNumber(anyString()))
                .thenReturn(false);

        assertThrows(PaymentCardAlreadyExpiredException.class,
                () -> userService.createPaymentCard(dto));
    }

    // activatePaymentCard / deactivatePaymentCard
    @Test
    @DisplayName("Should activate payment card successfully")
    void activatePaymentCard_shouldActivateSuccessfully() {
        User user = new User();
        user.setActive(true);
        PaymentCard card = new PaymentCard();
        card.setId(1L);
        user.getPaymentCards().add(card);

        when(userRepository.findByIdWithPaymentCards(1L)).thenReturn(Optional.of(user));

        userService.activatePaymentCard(1L, 1L);

        assertTrue(card.getActive());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should throw NotFoundException when activating card for non-existent user")
    void activatePaymentCard_shouldThrowNotFoundForUser() {
        when(userRepository.findByIdWithPaymentCards(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> userService.activatePaymentCard(1L, 1L));
    }

    @Test
    @DisplayName("Should throw InactiveUserException when activating card for inactive user")
    void activatePaymentCard_shouldThrowInactiveUser() {
        User user = new User();
        user.setActive(false);
        when(userRepository.findByIdWithPaymentCards(1L)).thenReturn(Optional.of(user));

        assertThrows(InactiveUserException.class,
                () -> userService.activatePaymentCard(1L, 1L));
    }

    @Test
    @DisplayName("Should throw NotFoundException when activating non-existent card")
    void activatePaymentCard_shouldThrowNotFoundForCard() {
        User user = new User();
        user.setActive(true);
        when(userRepository.findByIdWithPaymentCards(1L)).thenReturn(Optional.of(user));

        assertThrows(NotFoundException.class,
                () -> userService.activatePaymentCard(1L, 1L));
    }

    @Test
    @DisplayName("Should deactivate payment card successfully")
    void deactivatePaymentCard_shouldDeactivateSuccessfully() {
        User user = new User();
        PaymentCard card = new PaymentCard();
        card.setId(1L);
        card.setActive(true);
        user.getPaymentCards().add(card);

        when(userRepository.findByIdWithPaymentCards(1L)).thenReturn(Optional.of(user));

        userService.deactivatePaymentCard(1L, 1L);

        assertFalse(card.getActive());
        verify(userRepository).save(user);
    }

    // deletePaymentCard
    @Test
    @DisplayName("Should delete payment card successfully")
    void deletePaymentCard_shouldDeleteSuccessfully() {
        User user = new User();
        user.setActive(true);
        PaymentCard card = new PaymentCard();
        card.setId(1L);
        user.getPaymentCards().add(card);

        when(userRepository.findByIdWithPaymentCards(1L)).thenReturn(Optional.of(user));

        userService.deletePaymentCard(1L, 1L);

        assertFalse(user.getPaymentCards().contains(card));
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should throw NotFoundException when deleting card for non-existent user")
    void deletePaymentCard_shouldThrowNotFoundForUser() {
        when(userRepository.findByIdWithPaymentCards(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> userService.deletePaymentCard(1L, 1L));
    }

    @Test
    @DisplayName("Should throw InactiveUserException when deleting card for inactive user")
    void deletePaymentCard_shouldThrowInactiveUser() {
        User user = new User();
        user.setActive(false);
        when(userRepository.findByIdWithPaymentCards(1L)).thenReturn(Optional.of(user));

        assertThrows(InactiveUserException.class,
                () -> userService.deletePaymentCard(1L, 1L));
    }

    @Test
    @DisplayName("Should throw NotFoundException when deleting non-existent card")
    void deletePaymentCard_shouldThrowNotFoundForCard() {
        User user = new User();
        user.setActive(true);
        when(userRepository.findByIdWithPaymentCards(1L)).thenReturn(Optional.of(user));

        assertThrows(NotFoundException.class,
                () -> userService.deletePaymentCard(1L, 10L));
    }
}