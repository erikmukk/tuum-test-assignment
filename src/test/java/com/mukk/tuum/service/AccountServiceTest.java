package com.mukk.tuum.service;

import com.mukk.tuum.exception.AccountMissingException;
import com.mukk.tuum.model.enums.Currency;
import com.mukk.tuum.model.request.CreateAccountRequest;
import com.mukk.tuum.persistence.dao.AccountDao;
import com.mukk.tuum.persistence.entity.AccountBalance;
import com.mukk.tuum.persistence.entity.gen.AccountEntity;
import com.mukk.tuum.persistence.entity.gen.BalanceEntity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    private static final UUID ACCOUNT_ID = UUID.randomUUID();
    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final String COUNTRY = "EST";
    private static final List<Currency> CURRENCIES = List.of(Currency.EUR, Currency.GBP);

    @Mock
    private AccountDao accountDao;
    @Mock
    private BalanceService balanceService;

    @InjectMocks
    private AccountService accountService;

    private void mockCreateBalances() {
        final var balanceEur = new BalanceEntity(UUID.randomUUID().toString(), UUID.randomUUID().toString(), 0.0, Currency.EUR.getValue());
        final var balanceGbp = new BalanceEntity(UUID.randomUUID().toString(), UUID.randomUUID().toString(), 0.0, Currency.GBP.getValue());
        when(balanceService.createBalances(CURRENCIES, ACCOUNT_ID)).thenReturn(List.of(balanceEur, balanceGbp));
    }

    /*
    Since my myBatis implementation creates and sets accountId in AccountEntity while/after insert
    automatically (I do not provide a value prior to inserting),
    then I need this more complex mocking logic.
     */
    private void mockAccountDaoInsert() {
        when(accountDao.insert(any(AccountEntity.class))).thenAnswer((Answer<Integer>) invocation -> {
            AccountEntity accountEntity = invocation.getArgument(0);
            accountEntity.setAccountId(ACCOUNT_ID.toString());
            return 1;
        });
    }

    @Nested
    class Create_account {

        @Test
        void succeeds() {
            final var createAccountRequest = CreateAccountRequest.builder()
                    .customerId(CUSTOMER_ID)
                    .country(COUNTRY)
                    .currencies(CURRENCIES)
                    .build();

            mockAccountDaoInsert();
            mockCreateBalances();

            final var accountResponse = accountService.create(createAccountRequest);

            verify(accountDao).insert(any(AccountEntity.class));
            verify(balanceService).createBalances(CURRENCIES, ACCOUNT_ID);
            assertThat(accountResponse).isNotNull();
        }
    }

    @Nested
    class Get_account_with_balances {

        @Test
        void succeeds() {
            when(accountDao.getAccountWithBalances(ACCOUNT_ID.toString()))
                    .thenReturn(new AccountBalance(new AccountEntity(UUID.randomUUID().toString(), "customerId", "EST"), List.of()));

            final var result = assertDoesNotThrow(() -> accountService.getAccountWithBalances(ACCOUNT_ID));

            assertThat(result).isNotNull();
        }

        @Test
        void fails_due_to_missing_account() {
            when(accountDao.getAccountWithBalances(ACCOUNT_ID.toString())).thenReturn(new AccountBalance());

            final var exception = assertThrows(AccountMissingException.class, () -> accountService.getAccountWithBalances(ACCOUNT_ID));

            assertThat(exception.getMessage()).isEqualTo(String.format("Account with id '%s' does not exist.", ACCOUNT_ID));
        }
    }

    @Nested
    class Account_verification {
        @Test
        void succeeds_with_existing_account() {
            when(accountDao.selectByPrimaryKey(ACCOUNT_ID.toString())).thenReturn(new AccountEntity());

            assertDoesNotThrow(() -> accountService.verifyAccountExists(ACCOUNT_ID));
        }

        @Test
        void throws_an_exception_when_account_missing() {
            when(accountDao.selectByPrimaryKey(ACCOUNT_ID.toString())).thenReturn(null);

            final var exception = assertThrows(AccountMissingException.class, () -> accountService.verifyAccountExists(ACCOUNT_ID));

            assertThat(exception.getMessage()).isEqualTo(String.format("Account with id '%s' does not exist.", ACCOUNT_ID));
        }
    }

}
