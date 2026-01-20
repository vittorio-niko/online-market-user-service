package org.innowise.internship.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.innowise.internship.userservice.model.dto.request.user.FilterUserRequestDto;
import org.innowise.internship.userservice.model.dto.response.paymentcard.PaymentCardSummaryResponseDto;
import org.innowise.internship.userservice.model.dto.response.user.UserResponseDto;
import org.innowise.internship.userservice.model.mapper.response.PaymentCardResponseMapper;
import org.innowise.internship.userservice.model.mapper.response.UserResponseMapper;
import org.innowise.internship.userservice.service.PaymentCardQueryService;
import org.innowise.internship.userservice.service.UserQueryService;
import org.innowise.internship.userservice.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Validated
@RestController
public class AdminController {
    private final UserService userService;
    private final UserQueryService userQueryService;
    private final PaymentCardQueryService paymentCardQueryService;

    private final UserResponseMapper userResponseMapper;
    private final PaymentCardResponseMapper paymentCardResponseMapper;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(
            @PathVariable Long id
    ) {
        UserResponseDto responseDto = userResponseMapper.toUserResponseDto(
                userQueryService.getUserById(id)
        );
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(
            @Valid FilterUserRequestDto filter,
            Pageable pageable
    ) {
        Page<UserResponseDto> response =
                userQueryService.getAllUsers(filter, pageable)
                        .map(userResponseMapper::toUserResponseDto);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/cards")
    public ResponseEntity<List<PaymentCardSummaryResponseDto>> getUserCards(
            @PathVariable Long userId
    ) {
        List<PaymentCardSummaryResponseDto> responseDtoList =
                paymentCardResponseMapper.toPaymentCardSummaryResponseDtoList(
                        paymentCardQueryService.getPaymentCardsByUserId(userId)
                );
        return ResponseEntity.ok(responseDtoList);
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<Void> activateUser(@PathVariable Long id) {
        userService.activateUserById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        userService.deactivateUserById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{userId}/cards/{cardId}/activate")
    public ResponseEntity<Void> activatePaymentCard(
            @PathVariable Long userId,
            @PathVariable Long cardId
    ) {
        userService.activatePaymentCard(userId, cardId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{userId}/cards/{cardId}/deactivate")
    public ResponseEntity<Void> deactivatePaymentCard(
            @PathVariable Long userId,
            @PathVariable Long cardId
    ) {
        userService.deactivatePaymentCard(userId, cardId);
        return ResponseEntity.noContent().build();
    }
}
