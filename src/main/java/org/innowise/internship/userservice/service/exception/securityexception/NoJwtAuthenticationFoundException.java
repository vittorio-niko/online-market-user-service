package org.innowise.internship.userservice.service.exception.securityexception;

public class NoJwtAuthenticationFoundException extends SecurityException {
    public NoJwtAuthenticationFoundException(String message) {
        super(message);
    }
    public NoJwtAuthenticationFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
