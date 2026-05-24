package org.innowise.internship.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.innowise.internship.userservice.model.dto.request.paymentcard.CreatePaymentCardRequestDto;
import org.innowise.internship.userservice.model.dto.request.user.CreateUserRequestDto;
import org.innowise.internship.userservice.model.dto.request.user.UpdateUserRequestDto;
import org.innowise.internship.userservice.model.dto.response.paymentcard.PaymentCardSummaryResponseDto;
import org.innowise.internship.userservice.model.dto.response.user.UserProfileResponseDto;
import org.innowise.internship.userservice.model.mapper.response.PaymentCardResponseMapper;
import org.innowise.internship.userservice.model.mapper.response.UserResponseMapper;
import org.innowise.internship.userservice.service.PaymentCardQueryService;
import org.innowise.internship.userservice.service.UserQueryService;
import org.innowise.internship.userservice.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
@RestController
public class UserController {
    private final UserService userService;
    private final UserQueryService userQueryService;
    private final PaymentCardQueryService paymentCardQueryService;

    private final UserResponseMapper userResponseMapper;
    private final PaymentCardResponseMapper paymentCardResponseMapper;

    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponseDto> getUserById(
            @PathVariable Long id
    ) {
        UserProfileResponseDto responseDto = userResponseMapper.toUserProfileResponseDto(
                userQueryService.getUserById(id)
        );
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping
    public ResponseEntity<UserProfileResponseDto> createUser(
            @RequestBody @Valid CreateUserRequestDto dto
    ) {
        UserProfileResponseDto response = userResponseMapper.toUserProfileResponseDto(
                userService.createUser(dto)
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserProfileResponseDto> updateUserById(
            @PathVariable Long id,
            @RequestBody @Valid UpdateUserRequestDto dto
    ) {
        UserProfileResponseDto responseDto =  userResponseMapper.toUserProfileResponseDto(
                userService.updateUserById(id, dto)
        );

        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
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

    @GetMapping("/{userId}/cards/{cardId}")
    public ResponseEntity<PaymentCardSummaryResponseDto> getUserCardById(
            @PathVariable Long userId,
            @PathVariable Long cardId
    ) {
        PaymentCardSummaryResponseDto responseDto = paymentCardResponseMapper.toPaymentCardSummaryResponseDto(
                paymentCardQueryService.getPaymentCardByIdAndUserId(cardId, userId)
        );
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/{userId}/cards")
    public ResponseEntity<PaymentCardSummaryResponseDto> createPaymentCard(
            @PathVariable Long userId,
            @RequestBody @Valid CreatePaymentCardRequestDto dto
    ) {
        dto.setUserId(userId);

        PaymentCardSummaryResponseDto responseDto = paymentCardResponseMapper
                .toPaymentCardSummaryResponseDto(
                userService.createPaymentCard(dto)
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseDto);
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

    @DeleteMapping("/{userId}/cards/{cardId}")
    public ResponseEntity<Void> deletePaymentCard(
            @PathVariable Long userId,
            @PathVariable Long cardId
    ) {
        userService.deletePaymentCard(userId, cardId);
        return ResponseEntity.noContent().build();
    }
}
