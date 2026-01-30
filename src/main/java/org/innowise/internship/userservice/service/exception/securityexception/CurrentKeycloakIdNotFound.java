package org.innowise.internship.userservice.service.exception.securityexception;

public class CurrentKeycloakIdNotFound extends SecurityException {
    public CurrentKeycloakIdNotFound(String message) {
        super(message);
    }
    public CurrentKeycloakIdNotFound(String message, Throwable cause) {
        super(message, cause);
    }
}
