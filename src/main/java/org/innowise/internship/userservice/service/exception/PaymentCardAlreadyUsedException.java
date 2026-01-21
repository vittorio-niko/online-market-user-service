package org.innowise.internship.userservice.service.exception;

public class PaymentCardAlreadyUsedException extends BusinessException {
    public PaymentCardAlreadyUsedException(String message) {
        super(message);
    }

    public PaymentCardAlreadyUsedException(String message, Throwable cause) {
        super(message, cause);
    }
}
