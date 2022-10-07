package com.mukk.tuum.model.response;

import com.mukk.tuum.model.enums.Currency;
import com.mukk.tuum.model.enums.TransactionDirection;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.UUID;

@Builder
@AllArgsConstructor
public class CreateTransactionResponse {

    private UUID accountId;

    private UUID transactionId;

    private Double amount;

    private Currency currency;

    private TransactionDirection direction;

    private String description;

    private Double balance;
}
