package com.mukk.tuum.persistence.entity.gen;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountEntity {
    private String accountId;

    private String customerId;
}