package org.innowise.internship.userservice.controller.exceptionhandler;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.innowise.internship.userservice.service.exception.securityexception.KeycloakIdAlreadyExistsException;
import org.innowise.internship.userservice.service.exception.securityexception.NoJwtAuthenticationFoundException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityExceptionHandler {
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationDenied(
            @NonNull AuthorizationDeniedException e
    ) {
        log.warn("Authorization denied: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(
                        new ErrorResponse(
                                ErrorCode.UNAUTHORIZED,
                                e.getMessage(),
                                null
                        )
                );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            @NonNull AccessDeniedException e
    ) {
        log.warn("Access denied: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(
                        new ErrorResponse(
                                ErrorCode.FORBIDDEN,
                                e.getMessage(),
                                null
                        )
                );
    }

    @ExceptionHandler(KeycloakIdAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleKeycloakIdAlreadyExists(
            @NonNull KeycloakIdAlreadyExistsException e
    ) {
        log.warn("Keycloak ID conflict: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(
                        new ErrorResponse(
                                ErrorCode.KEYCLOAK_ID_DUPLICATE_CONFLICT,
                                e.getMessage(),
                                null
                        )
                );
    }

    @ExceptionHandler(NoJwtAuthenticationFoundException.class)
    ResponseEntity<ErrorResponse> handleNoJwtAuthenticationFound(
            @NonNull NoJwtAuthenticationFoundException e
    ) {
        log.warn("JWT Authentication missing: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(
                        new ErrorResponse(
                                ErrorCode.UNAUTHORIZED,
                                e.getMessage(),
                                null
                        )
                );
    }
}
