package org.innowise.internship.userservice.controller.exceptionhandler;

import lombok.NonNull;
import org.innowise.internship.userservice.service.exception.securityexception.KeycloakIdAlreadyExistsException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityExceptionHandler {
    @ExceptionHandler(AuthorizationDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleAuthorizationDenied(
            @NonNull AuthorizationDeniedException e
    ) {
        return new ErrorResponse(
                ErrorCode.FORBIDDEN,
                e.getMessage(),
                null
                );
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleAccessDenied(
            @NonNull AccessDeniedException e
    ) {
        return new ErrorResponse(
                ErrorCode.FORBIDDEN,
                e.getMessage(),
                null
        );
    }

    @ExceptionHandler(KeycloakIdAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleKeycloakIdAlreadyExists(
            @NonNull KeycloakIdAlreadyExistsException e
    ) {
        return new ErrorResponse(
                        ErrorCode.KEYCLOAK_ID_DUPLICATE_CONFLICT,
                        e.getMessage(),
                        null
                );
    }
}
