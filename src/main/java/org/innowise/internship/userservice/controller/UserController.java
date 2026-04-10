package org.innowise.internship.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.innowise.internship.userservice.controller.securitycontext.CurrentUserProvider;
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

@Slf4j
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
@RestController
public class UserController {
    private final UserService userService;
    private final CurrentUserProvider currentUserProvider;
    private final UserQueryService userQueryService;
    private final PaymentCardQueryService paymentCardQueryService;

    private final UserResponseMapper userResponseMapper;
    private final PaymentCardResponseMapper paymentCardResponseMapper;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponseDto> getCurrentUser() {
        log.info("Fetching profile for user id: {}", currentUserProvider.getCurrentInternalId());
        UserProfileResponseDto responseDto = userResponseMapper.toUserProfileResponseDto(
                userQueryService.getUserById(currentUserProvider.getCurrentInternalId())
        );
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping
    public ResponseEntity<UserProfileResponseDto> createUser(
            @RequestBody @Valid CreateUserRequestDto dto
    ) {
        log.info("Request to create a new user");
        UserProfileResponseDto response = userResponseMapper.toUserProfileResponseDto(
                userService.createUser(dto)
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponseDto> updateCurrentUserById(
            @RequestBody @Valid UpdateUserRequestDto dto
    ) {
        log.info("Request to update profile for user id: {}", currentUserProvider.getCurrentInternalId());
        UserProfileResponseDto responseDto =  userResponseMapper.toUserProfileResponseDto(
                userService.updateUserById(currentUserProvider.getCurrentInternalId(), dto)
        );

        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteCurrentUser() {
        log.info("Request to delete user account for id: {}", currentUserProvider.getCurrentInternalId());
        userService.deleteUserById(currentUserProvider.getCurrentInternalId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-cards")
    public ResponseEntity<List<PaymentCardSummaryResponseDto>> getUserCards() {
        log.info("Fetching all payment cards for user id: {}", currentUserProvider.getCurrentInternalId());
        List<PaymentCardSummaryResponseDto> responseDtoList =
                paymentCardResponseMapper.toPaymentCardSummaryResponseDtoList(
                        paymentCardQueryService.getPaymentCardsByUserId(
                                currentUserProvider.getCurrentInternalId()
                        )
                );
        return ResponseEntity.ok(responseDtoList);
    }

    @GetMapping("/my-cards/{cardId}")
    public ResponseEntity<PaymentCardSummaryResponseDto> getUserCardById(
            @PathVariable Long cardId
    ) {
        log.info("Fetching card id: {} for user id: {}", cardId, currentUserProvider.getCurrentInternalId());
        PaymentCardSummaryResponseDto responseDto = paymentCardResponseMapper.toPaymentCardSummaryResponseDto(
                paymentCardQueryService.getPaymentCardByIdAndUserId(
                        cardId, currentUserProvider.getCurrentInternalId()
                )
        );
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/my-cards")
    public ResponseEntity<PaymentCardSummaryResponseDto> createPaymentCard(
            @RequestBody @Valid CreatePaymentCardRequestDto dto
    ) {
        log.info("Request to add a new payment card for user id: {}", currentUserProvider.getCurrentInternalId());
        PaymentCardSummaryResponseDto responseDto = paymentCardResponseMapper
                .toPaymentCardSummaryResponseDto(
                        userService.createPaymentCard(currentUserProvider.getCurrentInternalId(), dto)
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseDto);
    }

    @PutMapping("/my-cards/{cardId}/activate")
    public ResponseEntity<Void> activatePaymentCard(
            @PathVariable Long cardId
    ) {
        log.info("Activating card id: {} for user id: {}", cardId, currentUserProvider.getCurrentInternalId());
        userService.activatePaymentCard(currentUserProvider.getCurrentInternalId(), cardId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/my-cards/{cardId}/deactivate")
    public ResponseEntity<Void> deactivatePaymentCard(
            @PathVariable Long cardId
    ) {
        log.info("Deactivating card id: {} for user id: {}", cardId, currentUserProvider.getCurrentInternalId());
        userService.deactivatePaymentCard(currentUserProvider.getCurrentInternalId(), cardId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/my-cards/{cardId}")
    public ResponseEntity<Void> deletePaymentCard(
            @PathVariable Long cardId
    ) {
        log.info("Deleting card id: {} for user id: {}", cardId, currentUserProvider.getCurrentInternalId());
        userService.deletePaymentCard(currentUserProvider.getCurrentInternalId(), cardId);
        return ResponseEntity.noContent().build();
    }
}