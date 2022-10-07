package com.mukk.tuum.model.response;

import com.mukk.tuum.model.enums.Currency;
import com.mukk.tuum.model.enums.TransactionDirection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
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
