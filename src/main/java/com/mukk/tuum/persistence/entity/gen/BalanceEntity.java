package com.mukk.tuum.persistence.entity.gen;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BalanceEntity {
    private String balanceId;

    private String accountId;

    private Double amount;

    private String currency;
}