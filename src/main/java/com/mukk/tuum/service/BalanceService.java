package com.mukk.tuum.service;

import com.mukk.tuum.exception.ExceptionTexts;
import com.mukk.tuum.exception.InvalidCurrencyException;
import com.mukk.tuum.model.enums.Currency;
import com.mukk.tuum.model.rabbit.RabbitDatabaseAction;
import com.mukk.tuum.model.rabbit.RabbitDatabaseTable;
import com.mukk.tuum.persistence.dao.BalanceDao;
import com.mukk.tuum.persistence.entity.gen.BalanceEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BalanceService {

    private final BalanceDao balanceDao;
    private final RabbitSender rabbitSender;

    @Transactional
    public List<BalanceEntity> createBalances(List<Currency> currencies, UUID accountId) {
        for (final var currency : currencies) {
            var balance = BalanceEntity.builder()
                    .accountId(accountId.toString())
                    .currency(currency.getValue())
                    .amount(0.0)
                    .build();
            insertBalance(balance);
        }
        return balanceDao.getBalancesByAccountId(accountId.toString());
    }

    public void verifyAccountHasCurrency(Currency currency, UUID accountId) throws InvalidCurrencyException {
        final var accountCurrencies = balanceDao.getAccountCurrencies(accountId.toString());
        final var hasCurrency = accountCurrencies.stream().filter(currency::equals).findAny();
        if (hasCurrency.isEmpty()) {
            throw new InvalidCurrencyException(String.format(ExceptionTexts.INVALID_CURRENCY, currency));
        }
    }

    public void updateBalance(BalanceEntity balance) {
        final int update = balanceDao.updateByPrimaryKey(balance);
        if (update == 1) {
            rabbitSender.send(RabbitDatabaseAction.UPDATE, RabbitDatabaseTable.BALANCE, balance);
        }
    }

    public BalanceEntity getBalanceForUpdating(UUID accountId, Currency currency) {
        return balanceDao.getBalanceByAccountIdForUpdate(accountId.toString(), currency.getValue());
    }

    private int insertBalance(BalanceEntity entity) {
        final int insert = balanceDao.insert(entity);
        if (insert == 1) {
            rabbitSender.send(RabbitDatabaseAction.INSERT, RabbitDatabaseTable.BALANCE, entity);
        }
        return insert;
    }
}
