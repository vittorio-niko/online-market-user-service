package org.innowise.internship.userservice.controller.exceptionhandler;

public enum ErrorCode {
    RESOURCE_NOT_FOUND,
    EMAIL_DUPLICATE_CONFLICT,
    CARD_DUPLICATE_CONFLICT,
    CARDS_COUNT_LIMIT_OVERFLOW,
    CARD_NUMBER_CONFLICT,
    CARD_EXPIRED,
    INACTIVE_USER_ACTION,
    INVALID_ARGUMENT,
    INTERNAL_ERROR
}
