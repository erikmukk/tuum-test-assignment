package com.mukk.tuum.model.response;

import com.mukk.tuum.persistence.entity.Balance;
import com.mukk.tuum.persistence.entity.gen.AccountEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {

    private AccountEntity account;

    private List<Balance> balances;
}
