package com.mukk.tuum.exception;

public class ExceptionTexts {

    private ExceptionTexts() {
        throw new IllegalStateException("Utility class");
    }

    public static final String ACCOUNT_NOT_FOUND = "Account with id '%s' does not exist.";
    public static final String INSUFFICIENT_FUNDS = "Insufficient funds. Got '%s', required '%s'";
    public static final String INVALID_CURRENCY = "No balance available for currency '%s'";
}
