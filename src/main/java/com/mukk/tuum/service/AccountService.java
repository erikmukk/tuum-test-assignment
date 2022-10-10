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
@Transactional(readOnly = true)
public class AccountService {

    private final AccountDao accountDao;
    private final BalanceService balanceService;

    @Transactional
    public AccountResponse create(final CreateAccountRequest request) {
        final var account = AccountEntity.builder()
                .customerId(request.getCustomerId().toString())
                .country(request.getCountry().toUpperCase())
                .build();

        insertAccount(account);

        final var createdBalances = balanceService.createBalances(request.getCurrencies(), UUID.fromString(account.getAccountId()));

        account.setCountry(null);

        return AccountResponse.builder()
                .accountId(UUID.fromString(account.getAccountId()))
                .customerId(account.getCustomerId())
                .balances(createdBalances.stream()
                        .map(b -> {
                            var balance = new Balance();
                            balance.setCurrency(Currency.valueOf(b.getCurrency()));
                            balance.setAmount(b.getAmount());
                            return balance;
                        })
                        .collect(Collectors.toList()))
                .build();
    }

    public AccountResponse getAccountWithBalances(UUID accountId) throws AccountMissingException {
        final var accountWithBalances = accountDao.getAccountWithBalances(accountId.toString());
        verifyAccountExists(accountWithBalances, accountId);
        return AccountResponse.builder()
                .accountId(UUID.fromString(accountWithBalances.getAccount().getAccountId()))
                .customerId(accountWithBalances.getAccount().getCustomerId())
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

    public int insertAccount(AccountEntity entity) {
        return accountDao.insert(entity);
    }
}
