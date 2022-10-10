package com.mukk.tuum.model.response;

import com.mukk.tuum.model.enums.Currency;
import com.mukk.tuum.model.enums.TransactionDirection;
import com.mukk.tuum.persistence.entity.gen.TransactionEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class CreateTransactionResponse {

    private UUID accountId;

    private UUID transactionId;

    private Double amount;

    private Currency currency;

    private TransactionDirection direction;

    private String description;

    private BigDecimal balance;

    public static CreateTransactionResponse fromTransactionEntityAndBalance(TransactionEntity t, Double balance) {
        return CreateTransactionResponse.builder()
                .transactionId(UUID.fromString(t.getTransactionId()))
                .accountId(UUID.fromString(t.getAccountId()))
                .amount(t.getAmount())
                .currency(Currency.valueOf(t.getCurrency()))
                .description(t.getDescription())
                .direction(TransactionDirection.valueOf(t.getDirection()))
                .balance(BigDecimal.valueOf(balance).setScale(2, RoundingMode.UP))
                .build();
    }
}
