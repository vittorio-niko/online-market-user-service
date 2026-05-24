package org.innowise.internship.userservice.service;

import org.innowise.internship.userservice.model.entity.PaymentCard;
import org.innowise.internship.userservice.repository.PaymentCardRepository;
import org.innowise.internship.userservice.repository.UserRepository;
import org.innowise.internship.userservice.service.exception.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentCardQueryServiceTest {
    @Mock private UserRepository userRepository;
    @Mock private PaymentCardRepository paymentCardRepository;

    @InjectMocks
    private PaymentCardQueryService paymentCardQueryService;

    @Test
    @DisplayName("Should get payment cards by user ID successfully")
    void getPaymentCardsByUserId_shouldGetSuccessfully() {
        Long userId = 1L;
        List<PaymentCard> cards = List.of(new PaymentCard());

        when(userRepository.existsById(userId)).thenReturn(true);
        when(paymentCardRepository.findAllByUserId(userId))
                .thenReturn(cards);

        List<PaymentCard> result =
                paymentCardQueryService.getPaymentCardsByUserId(userId);

        assertEquals(cards, result);
        verify(userRepository).existsById(userId);
        verify(paymentCardRepository).findAllByUserId(userId);
    }

    @Test
    @DisplayName("Should throw NotFoundException when getting cards for non-existent user")
    void getPaymentCardsByUserId_shouldThrowNotFound() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> paymentCardQueryService.getPaymentCardsByUserId(1L));

        verify(userRepository).existsById(1L);
        verifyNoInteractions(paymentCardRepository);
    }

    @Test
    @DisplayName("Should get payment card by ID and user ID successfully")
    void getPaymentCardByIdAndUserId_shouldGetSuccessfully() {
        Long userId = 1L;
        Long cardId = 10L;

        PaymentCard card = new PaymentCard();

        when(paymentCardRepository.findByIdAndUserId(cardId, userId))
                .thenReturn(Optional.of(card));

        PaymentCard result =
                paymentCardQueryService.getPaymentCardByIdAndUserId(cardId, userId);

        assertEquals(card, result);
        verify(paymentCardRepository)
                .findByIdAndUserId(cardId, userId);
    }

    @Test
    @DisplayName("Should throw NotFoundException when getting non-existent payment card")
    void getPaymentCardByIdAndUserId_shouldThrowNotFound() {
        when(paymentCardRepository.findByIdAndUserId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> paymentCardQueryService.getPaymentCardByIdAndUserId(1L, 1L));

        verify(paymentCardRepository)
                .findByIdAndUserId(1L, 1L);
    }
}


