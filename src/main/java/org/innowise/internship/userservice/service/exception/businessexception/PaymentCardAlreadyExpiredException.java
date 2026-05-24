package org.innowise.internship.userservice.service.exception.businessexception;

public class PaymentCardAlreadyExpiredException extends BusinessException {
    public PaymentCardAlreadyExpiredException(String message) {
        super(message);
    }

    public PaymentCardAlreadyExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
