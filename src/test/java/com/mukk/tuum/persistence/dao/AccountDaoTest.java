package com.mukk.tuum.persistence.dao;

import com.mukk.tuum.model.enums.Currency;
import com.mukk.tuum.persistence.entity.gen.AccountEntity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AccountDaoTest extends DaoTestBase {

    private static final UUID KNOWN_ACCOUNT_ID = UUID.fromString("909c39bd-e911-4030-a69c-e4b1a7f6054f");
    private static final UUID CUSTOMER_ID = UUID.fromString("116a84ba-3629-46b3-9fa1-a3667268ce56");
    private static final UUID MISSING_ACCOUNT_ID = UUID.randomUUID();

    @Autowired
    private AccountDao accountDao;

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class Inserts_account {

        @Test
        void without_explicitly_defining_account_id() {
            final var accountEntity = AccountEntity.builder().customerId(CUSTOMER_ID.toString()).country("EST").build();
            final var insert = accountDao.insert(accountEntity);

            assertThat(insert).isEqualTo(1);
            assertThat(accountEntity.getAccountId()).isNotNull();
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class Get_account_succeeds {

        @Sql(statements = "INSERT INTO tuum.account(account_id, customer_id, country) VALUES('909c39bd-e911-4030-a69c-e4b1a7f6054f', '116a84ba-3629-46b3-9fa1-a3667268ce56', 'EST')")
        @Test
        void get() {
            final var accountEntity = accountDao.selectByPrimaryKey(KNOWN_ACCOUNT_ID.toString());

            assertThat(accountEntity).isNotNull();
            assertThat(accountEntity.getAccountId()).isEqualTo(KNOWN_ACCOUNT_ID.toString());
            assertThat(accountEntity.getCustomerId()).isEqualTo(CUSTOMER_ID.toString());
            assertThat(accountEntity.getCountry()).isNull();
        }

        @Sql(statements = "INSERT INTO tuum.account(account_id, customer_id, country) VALUES('909c39bd-e911-4030-a69c-e4b1a7f6054f', '116a84ba-3629-46b3-9fa1-a3667268ce56', 'EST')")
        @Test
        void with_no_balances() {
            final var accountWithCurrencies = accountDao.getAccountWithBalances(KNOWN_ACCOUNT_ID.toString());

            assertThat(accountWithCurrencies).isNotNull();
            assertThat(accountWithCurrencies.getAccount()).isNotNull();
            assertThat(accountWithCurrencies.getAccount().getAccountId()).isEqualTo(KNOWN_ACCOUNT_ID.toString());
            assertThat(accountWithCurrencies.getBalances()).isEmpty();
        }

        @Sql(statements = {
                "INSERT INTO tuum.account(account_id, customer_id, country) VALUES('909c39bd-e911-4030-a69c-e4b1a7f6054f', '116a84ba-3629-46b3-9fa1-a3667268ce56', 'EST')",
                "INSERT INTO tuum.balance(account_id, amount, currency) VALUES('909c39bd-e911-4030-a69c-e4b1a7f6054f', 10.00, 'EUR')",
                "INSERT INTO tuum.balance(account_id, amount, currency) VALUES('909c39bd-e911-4030-a69c-e4b1a7f6054f', 20.00, 'GBP')"
        })
        @Test
        void with_balances() {
            final var accountWithCurrencies = accountDao.getAccountWithBalances(KNOWN_ACCOUNT_ID.toString());

            assertThat(accountWithCurrencies.getBalances()).hasSize(2);
            assertThat(accountWithCurrencies.getBalances().get(0).getCurrency()).isEqualTo(Currency.EUR);
            assertThat(accountWithCurrencies.getBalances().get(0).getAmount()).isEqualTo(BigDecimal.valueOf(10.0).setScale(2, RoundingMode.UP));
            assertThat(accountWithCurrencies.getBalances().get(1).getCurrency()).isEqualTo(Currency.GBP);
            assertThat(accountWithCurrencies.getBalances().get(1).getAmount()).isEqualTo(BigDecimal.valueOf(20.0).setScale(2, RoundingMode.UP));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class Get_account_fails {

        @Test
        void due_to_missing_account() {
            final AccountEntity accountEntity = accountDao.selectByPrimaryKey(MISSING_ACCOUNT_ID.toString());

            assertThat(accountEntity).isNull();
        }

        @Test
        void with_balances_due_to_missing_account() {
            final var missingAccount = accountDao.getAccountWithBalances(MISSING_ACCOUNT_ID.toString());

            assertThat(missingAccount).isNull();
        }
    }
}
