package com.mukk.tuum.model.response;

import com.mukk.tuum.persistence.entity.Balance;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {

    private UUID accountId;

    private String customerId;

    private List<Balance> balances;
}
