package org.innowise.internship.userservice.service.exception;

public class InactiveUserException extends BusinessException {
    public InactiveUserException(String message) { super(message); }

    public InactiveUserException(String message, Throwable cause) {
        super(message, cause);
    }
}
