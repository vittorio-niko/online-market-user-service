package org.innowise.internship.userservice.service.exception;

public class PaymentCardAlreadyExpiredException extends BusinessException {
    public PaymentCardAlreadyExpiredException(String message) {
        super(message);
    }

    public PaymentCardAlreadyExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
