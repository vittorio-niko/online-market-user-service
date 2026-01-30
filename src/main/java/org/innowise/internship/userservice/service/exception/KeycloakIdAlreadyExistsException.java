package org.innowise.internship.userservice.service.exception;

public class KeycloakIdAlreadyExistsException extends SecurityException {
    public KeycloakIdAlreadyExistsException(String message) {
        super(message);
    }
    public KeycloakIdAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
