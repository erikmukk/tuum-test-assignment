package com.mukk.tuum.exception;

public class InsufficientFundsException extends TransactionException {
    public InsufficientFundsException(String message) {
        super(message);
    }
}
