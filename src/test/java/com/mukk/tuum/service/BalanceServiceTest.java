package com.mukk.tuum.service;

import com.mukk.tuum.exception.InvalidCurrencyException;
import com.mukk.tuum.model.enums.Currency;
import com.mukk.tuum.model.rabbit.RabbitDatabaseAction;
import com.mukk.tuum.model.rabbit.RabbitDatabaseTable;
import com.mukk.tuum.persistence.dao.BalanceDao;
import com.mukk.tuum.persistence.entity.gen.BalanceEntity;
import org.junit.jupiter.api.Nested;
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
import static org.mockito.ArgumentMatchers.eq;
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
    @Mock
    private RabbitSender rabbitSender;

    @InjectMocks
    private BalanceService balanceService;

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

    @Nested
    class Create_balance {

        @Test
        void succeeds() {
            mockBalanceDaoInsert();
            when(balanceDao.getBalancesByAccountId(ACCOUNT_ID.toString())).thenReturn(List.of());

            final var result = balanceService.createBalances(CURRENCIES, ACCOUNT_ID);

            verify(rabbitSender, times(2)).send(eq(RabbitDatabaseAction.INSERT), eq(RabbitDatabaseTable.BALANCE), any());
            verify(balanceDao, times(2)).insert(any(BalanceEntity.class));
            verify(balanceDao).getBalancesByAccountId(ACCOUNT_ID.toString());
            assertThat(result).isNotNull();
        }
    }

    @Nested
    class Balance_verification {

        @ParameterizedTest
        @EnumSource(value = Currency.class, names = {"EUR", "GBP"})
        void succeeds_when_account_has_balance_with_given_currency(Currency validCurrency) {
            when(balanceDao.getAccountCurrencies(ACCOUNT_ID.toString())).thenReturn(CURRENCIES);

            assertDoesNotThrow(() -> balanceService.verifyAccountHasCurrency(validCurrency, ACCOUNT_ID));
        }

        @ParameterizedTest
        @EnumSource(value = Currency.class, names = {"USD", "SEK"})
        void throws_an_exception_when_account_does_not_have_balance_with_given_currency(Currency invalidCurrency) {
            when(balanceDao.getAccountCurrencies(ACCOUNT_ID.toString())).thenReturn(CURRENCIES);

            final var exception = assertThrows(InvalidCurrencyException.class,
                    () -> balanceService.verifyAccountHasCurrency(invalidCurrency, ACCOUNT_ID));

            assertThat(exception.getMessage()).isEqualTo(String.format("No balance available for currency '%s'", invalidCurrency));
        }
    }

    @Nested
    class Update_balance {

        @Test
        void succeeds() {
            final var balanceEntity = new BalanceEntity();
            when(balanceDao.updateByPrimaryKey(balanceEntity)).thenReturn(1);

            balanceService.updateBalance(balanceEntity);

            verify(balanceDao).updateByPrimaryKey(balanceEntity);
            verify(rabbitSender).send(eq(RabbitDatabaseAction.UPDATE), eq(RabbitDatabaseTable.BALANCE), any());
        }
    }

    @Nested
    class Get_balance {

        @Test
        void for_updating_succeeds() {
            balanceService.getBalanceForUpdating(ACCOUNT_ID, Currency.EUR);

            verify(balanceDao).getBalanceByAccountIdForUpdate(ACCOUNT_ID.toString(), Currency.EUR.getValue());
        }
    }
}
