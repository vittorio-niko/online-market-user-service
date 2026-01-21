package org.innowise.internship.userservice.service.exception;

public class EmailAlreadyUsedException extends BusinessException {
    public EmailAlreadyUsedException(String message) {
        super(message);
    }

    public EmailAlreadyUsedException(String message, Throwable cause) {
        super(message, cause);
    }
}
