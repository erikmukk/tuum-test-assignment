package com.mukk.tuum.service;

import com.mukk.tuum.exception.AccountMissingException;
import com.mukk.tuum.exception.ExceptionTexts;
import com.mukk.tuum.model.enums.Currency;
import com.mukk.tuum.model.request.CreateAccountRequest;
import com.mukk.tuum.model.response.AccountResponse;
import com.mukk.tuum.persistence.dao.AccountDao;
import com.mukk.tuum.persistence.entity.AccountBalance;
import com.mukk.tuum.persistence.entity.Balance;
import com.mukk.tuum.persistence.entity.gen.AccountEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional
public class AccountService {

    private final AccountDao accountDao;
    private final BalanceService balanceService;

    public AccountResponse create(final CreateAccountRequest request) {
        final var account = AccountEntity.builder()
                .customerId(request.getCustomerId())
                .build();

        accountDao.insert(account);

        final var createdBalances = balanceService.createBalances(request.getCurrencies(), UUID.fromString(account.getAccountId()));
        return AccountResponse.builder()
                .account(account)
                .balances(createdBalances.stream()
                        .map(b -> new Balance(Currency.valueOf(b.getCurrency()), b.getAmount()))
                        .collect(Collectors.toList()))
                .build();
    }

    public AccountResponse getAccountWithBalances(UUID accountId) throws AccountMissingException {
        final var accountWithBalances = accountDao.getAccountWithBalances(accountId.toString());
        verifyAccountExists(accountWithBalances, accountId);
        return AccountResponse.builder()
                .account(accountWithBalances.getAccount())
                .balances(accountWithBalances.getBalances())
                .build();
    }

    public void verifyAccountExists(UUID accountId) throws AccountMissingException {
        final var account = getAccount(accountId);
        verifyAccountExists(account, accountId);
    }

    private AccountEntity getAccount(UUID accountId) {
        return accountDao.selectByPrimaryKey(accountId.toString());
    }

    private void verifyAccountExists(AccountEntity account, UUID accountId) throws AccountMissingException {
        if (account == null) {
            throw new AccountMissingException(String.format(ExceptionTexts.ACCOUNT_NOT_FOUND, accountId));
        }
    }

    private void verifyAccountExists(AccountBalance account, UUID accountId) throws AccountMissingException {
        if (account == null || account.getAccount() == null) {
            throw new AccountMissingException(String.format(ExceptionTexts.ACCOUNT_NOT_FOUND, accountId));
        }
    }
}
