package com.mukk.tuum.persistence.entity;

import com.mukk.tuum.persistence.entity.gen.AccountEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountBalance {

    private AccountEntity account;

    private List<Balance> balances;
}
