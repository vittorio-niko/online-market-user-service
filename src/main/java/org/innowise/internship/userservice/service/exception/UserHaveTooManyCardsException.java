package org.innowise.internship.userservice.service.exception;

public class UserHaveTooManyCardsException extends BusinessException {
    public UserHaveTooManyCardsException(String message) {
        super(message);
    }

    public UserHaveTooManyCardsException(String message, Throwable cause) {
        super(message, cause);
    }
}
