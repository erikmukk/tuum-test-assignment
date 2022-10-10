package com.mukk.tuum.util;

import com.mukk.tuum.exception.ExceptionTexts;
import com.mukk.tuum.exception.InsufficientFundsException;
import com.mukk.tuum.exception.TransactionException;
import com.mukk.tuum.model.enums.TransactionDirection;
import com.mukk.tuum.model.request.CreateTransactionRequest;
import com.mukk.tuum.persistence.entity.gen.BalanceEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TransactionUtils {

    private TransactionUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static void verifyHasEnoughBalance(BalanceEntity balance, CreateTransactionRequest request) throws TransactionException {
        final var balanceAmount = BigDecimal.valueOf(balance.getAmount()).setScale(2, RoundingMode.UP);
        if (TransactionDirection.OUT.equals(request.getDirection())
                && balanceAmount.compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException(String.format(ExceptionTexts.INSUFFICIENT_FUNDS, balance.getAmount(), request.getAmount()));
        }
    }

    public static BigDecimal getNewAmount(Double initialAmount, BigDecimal diffAmount, TransactionDirection direction) {
        var balanceAmount = BigDecimal.valueOf(initialAmount).setScale(2, RoundingMode.UP);
        if (TransactionDirection.IN.equals(direction)) {
            return balanceAmount.add(diffAmount);
        }
        return balanceAmount.subtract(diffAmount);
    }
}
