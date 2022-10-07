package com.mukk.tuum.util;

import com.mukk.tuum.exception.ExceptionTexts;
import com.mukk.tuum.exception.InsufficientFundsException;
import com.mukk.tuum.exception.TransactionException;
import com.mukk.tuum.model.enums.TransactionDirection;
import com.mukk.tuum.model.request.TransactionRequest;
import com.mukk.tuum.persistence.entity.gen.BalanceEntity;

public class TransactionUtils {

    private TransactionUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static void verifyHasEnoughBalance(BalanceEntity balance, TransactionRequest request) throws TransactionException {
        if (TransactionDirection.OUT.equals(request.getDirection())
                && Double.compare(balance.getAmount(), request.getAmount()) < 0) {
            throw new InsufficientFundsException(String.format(ExceptionTexts.INSUFFICIENT_FUNDS, balance.getAmount(), request.getAmount()));
        }
    }

    public static Double getNewAmount(Double initialAmount, Double diffAmount, TransactionDirection direction) {
        if (TransactionDirection.IN.equals(direction)) {
            return initialAmount + diffAmount;
        }
        return initialAmount - diffAmount;
    }
}
