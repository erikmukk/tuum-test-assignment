package com.mukk.tuum.exception;

public class InvalidCurrencyException extends TransactionException {
    public InvalidCurrencyException(String message) {
        super(message);
    }
}
