package com.mukk.tuum.model.response;

import com.mukk.tuum.model.enums.Currency;
import com.mukk.tuum.model.enums.TransactionDirection;
import com.mukk.tuum.persistence.entity.gen.TransactionEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class TransactionResponse {

    private UUID transactionId;

    private UUID accountId;

    private Double amount;

    private Currency currency;

    private String description;

    private TransactionDirection direction;

    public static TransactionResponse fromTransactionEntity(TransactionEntity t) {
        return TransactionResponse.builder()
                .transactionId(UUID.fromString(t.getTransactionId()))
                .accountId(UUID.fromString(t.getAccountId()))
                .amount(t.getAmount())
                .currency(Currency.valueOf(t.getCurrency()))
                .description(t.getDescription())
                .direction(TransactionDirection.valueOf(t.getDirection()))
                .build();
    }

}
