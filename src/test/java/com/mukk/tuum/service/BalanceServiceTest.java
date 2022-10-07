package com.mukk.tuum.service;

import com.mukk.tuum.exception.InvalidCurrencyException;
import com.mukk.tuum.model.enums.Currency;
import com.mukk.tuum.persistence.dao.BalanceDao;
import com.mukk.tuum.persistence.entity.gen.BalanceEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BalanceServiceTest {

    private static final UUID ACCOUNT_ID = UUID.randomUUID();
    private static final UUID BALANCE_ID = UUID.randomUUID();
    private static final List<Currency> CURRENCIES = List.of(Currency.EUR, Currency.GBP);

    @Mock
    private BalanceDao balanceDao;

    @InjectMocks
    private BalanceService balanceService;

    @Test
    void creates_balances() {
        mockBalanceDaoInsert();
        when(balanceDao.getBalancesByAccountId(ACCOUNT_ID.toString())).thenReturn(List.of());

        final var result = balanceService.createBalances(CURRENCIES, ACCOUNT_ID);

        verify(balanceDao, times(2)).insert(any(BalanceEntity.class));
        verify(balanceDao).getBalancesByAccountId(ACCOUNT_ID.toString());
        assertThat(result).isNotNull();
    }

    @ParameterizedTest
    @EnumSource(value = Currency.class, names = { "EUR", "GBP" })
    void verifies_that_account_has_balance_with_given_currency(Currency validCurrency) {
        when(balanceDao.getAccountCurrencies(ACCOUNT_ID.toString())).thenReturn(CURRENCIES);

        assertDoesNotThrow(() -> balanceService.verifyAccountHasCurrency(validCurrency, ACCOUNT_ID));
    }

    @ParameterizedTest
    @EnumSource(value = Currency.class, names = { "USD", "SEK" })
    void verifies_that_account_does_not_have_balance_with_given_currency(Currency invalidCurrency) {
        when(balanceDao.getAccountCurrencies(ACCOUNT_ID.toString())).thenReturn(CURRENCIES);

        final var exception = assertThrows(InvalidCurrencyException.class,
                () -> balanceService.verifyAccountHasCurrency(invalidCurrency, ACCOUNT_ID));

        assertThat(exception.getMessage()).isEqualTo(String.format("No balance available for currency '%s'", invalidCurrency));
    }

    @Test
    void updates_balance() {
        final var balanceEntity = new BalanceEntity();
        balanceService.updateBalance(balanceEntity);

        verify(balanceDao).updateByPrimaryKey(balanceEntity);
    }

    @Test
    void gets_balance_for_updating() {
        balanceService.getBalanceForUpdating(ACCOUNT_ID, Currency.EUR);

        verify(balanceDao).getBalanceByAccountIdForUpdate(ACCOUNT_ID.toString(), Currency.EUR.getValue());
    }

    /*
    Since my myBatis implementation creates and sets accountId in BalanceEntity while/after insert
    automatically (I do not provide a value prior to inserting),
    then I need this more complex mocking logic.
     */
    private void mockBalanceDaoInsert() {
        when(balanceDao.insert(any(BalanceEntity.class))).thenAnswer((Answer<Integer>) invocation -> {
            BalanceEntity balanceEntity = invocation.getArgument(0);
            balanceEntity.setBalanceId(BALANCE_ID.toString());
            return 1;
        });
    }
}