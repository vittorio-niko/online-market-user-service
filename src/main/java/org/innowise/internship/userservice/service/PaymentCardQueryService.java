package org.innowise.internship.userservice.service;

import lombok.RequiredArgsConstructor;
import org.innowise.internship.userservice.model.entity.PaymentCard;
import org.innowise.internship.userservice.repository.PaymentCardRepository;
import org.innowise.internship.userservice.repository.UserRepository;
import org.innowise.internship.userservice.service.exception.businessexception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PaymentCardQueryService {
    private final UserRepository userRepository;
    private final PaymentCardRepository paymentCardRepository;

    @Transactional(readOnly = true)
    public List<PaymentCard> getPaymentCardsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found by id");
        }

        return paymentCardRepository.findAllByUserId(userId);
    }

    @Transactional(readOnly = true)
    public PaymentCard getPaymentCardByIdAndUserId(Long cardId, Long userId) {
        return paymentCardRepository.findByIdAndUserId(cardId, userId)
                .orElseThrow(() -> new NotFoundException("Payment card not found"));
    }
}
