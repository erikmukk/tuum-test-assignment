package com.mukk.tuum.exception;

public class ExceptionTexts {

    private ExceptionTexts() {
        throw new IllegalStateException("Utility class");
    }

    public static final String ACCOUNT_NOT_FOUND = "Account with id '%s' does not exist.";
    public static final String INSUFFICIENT_FUNDS = "Insufficient funds. Got '%s', required '%s'";
    public static final String INVALID_CURRENCY = "No balance available for currency '%s'";
    public static final String INVALID_ENUM_FIELD_VALUE = "Invalid value for field '%s'.";
    public static final String INVALID_ENUM_EXPLANATION = "Provided value '%s', expected one of '%s'";
}
