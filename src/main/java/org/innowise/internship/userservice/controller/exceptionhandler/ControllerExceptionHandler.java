package org.innowise.internship.userservice.controller.exceptionhandler;

import lombok.NonNull;
import org.innowise.internship.userservice.service.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ControllerExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            @NonNull NotFoundException e
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        e.getMessage(),
                        null
                ));
    }

    @ExceptionHandler(EmailAlreadyUsedException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyUsed(
            @NonNull EmailAlreadyUsedException e
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        ErrorCode.EMAIL_DUPLICATE_CONFLICT,
                        e.getMessage(),
                        null
                ));
    }

    @ExceptionHandler(UserHaveTooManyCardsException.class)
    public ResponseEntity<ErrorResponse> handleUserHaveTooManyCards(
            @NonNull UserHaveTooManyCardsException e
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        ErrorCode.CARDS_COUNT_LIMIT_OVERFLOW,
                        e.getMessage(),
                        null
                ));
    }

    @ExceptionHandler(PaymentCardAlreadyUsedException.class)
    public ResponseEntity<ErrorResponse> handleCardAlreadyUsed(
            @NonNull PaymentCardAlreadyUsedException e
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        ErrorCode.CARD_DUPLICATE_CONFLICT,
                        e.getMessage(),
                        null
                ));
    }

    @ExceptionHandler(PaymentCardAlreadyExpiredException.class)
    public ResponseEntity<ErrorResponse> handleCardAlreadyExpired(
            @NonNull PaymentCardAlreadyExpiredException e
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        ErrorCode.CARD_EXPIRED,
                        e.getMessage(),
                        null
                ));
    }

    @ExceptionHandler(InactiveUserException.class)
    public ResponseEntity<ErrorResponse> handleInactiveUser(
            @NonNull InactiveUserException e
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        ErrorCode.INACTIVE_USER_ACTION,
                        e.getMessage(),
                        null
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleNotValidArgumentException(
            @NonNull MethodArgumentNotValidException e
    ) {
        Map<String, String> errors = new HashMap<>();

        e.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage())
                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        ErrorCode.INVALID_ARGUMENT,
                        "Validation failed",
                        errors
                ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidJson(HttpMessageNotReadableException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        ErrorCode.INVALID_ARGUMENT,
                        "Malformed JSON request",
                        null
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            @NonNull Exception e
    ) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        ErrorCode.INTERNAL_ERROR,
                        "Unexpected error",
                        null
                ));
    }
}