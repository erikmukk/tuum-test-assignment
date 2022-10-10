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
public class TransactionResponse {

    private UUID transactionId;

    private UUID accountId;

    private BigDecimal amount;

    private Currency currency;

    private String description;

    private TransactionDirection direction;

    public static TransactionResponse fromTransactionEntity(TransactionEntity t) {
        return TransactionResponse.builder()
                .transactionId(UUID.fromString(t.getTransactionId()))
                .accountId(UUID.fromString(t.getAccountId()))
                .amount(BigDecimal.valueOf(t.getAmount()).setScale(2, RoundingMode.UP))
                .currency(Currency.valueOf(t.getCurrency()))
                .description(t.getDescription())
                .direction(TransactionDirection.valueOf(t.getDirection()))
                .build();
    }

}
