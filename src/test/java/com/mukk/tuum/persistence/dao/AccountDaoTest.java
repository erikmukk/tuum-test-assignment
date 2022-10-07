package com.mukk.tuum.persistence.dao;

import com.mukk.tuum.model.enums.Currency;
import com.mukk.tuum.persistence.entity.gen.AccountEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AccountDaoTest extends DaoTestBase {

    private static final UUID KNOWN_ACCOUNT_ID = UUID.fromString("909c39bd-e911-4030-a69c-e4b1a7f6054f");
    private static final UUID CUSTOMER_ID = UUID.fromString("116a84ba-3629-46b3-9fa1-a3667268ce56");
    private static final UUID MISSING_ACCOUNT_ID = UUID.randomUUID();

    @Autowired
    private AccountDao accountDao;

    @Test
    void inserts_account_without_explicit_id() {
        final var accountEntity = AccountEntity.builder().customerId(CUSTOMER_ID.toString()).build();
        final var insert = accountDao.insert(accountEntity);

        assertThat(insert).isEqualTo(1);
        assertThat(accountEntity.getAccountId()).isNotNull();
    }

    @Test
    void fails_to_get_account_due_to_account_missing() {
        final AccountEntity accountEntity = accountDao.selectByPrimaryKey(MISSING_ACCOUNT_ID.toString());

        assertThat(accountEntity).isNull();
    }

    @Sql(statements = "INSERT INTO tuum.account(account_id, customer_id) VALUES('909c39bd-e911-4030-a69c-e4b1a7f6054f', '116a84ba-3629-46b3-9fa1-a3667268ce56')")
    @Test
    void test_get_account() {
        final var accountEntity = accountDao.selectByPrimaryKey(KNOWN_ACCOUNT_ID.toString());

        assertThat(accountEntity).isNotNull();
        assertThat(accountEntity.getAccountId()).isEqualTo(KNOWN_ACCOUNT_ID.toString());
        assertThat(accountEntity.getCustomerId()).isEqualTo(CUSTOMER_ID.toString());
    }

    @Sql(statements = "INSERT INTO tuum.account(account_id, customer_id) VALUES('909c39bd-e911-4030-a69c-e4b1a7f6054f', '116a84ba-3629-46b3-9fa1-a3667268ce56')")
    @Test
    void gets_account_with_no_balances() {
        final var accountWithCurrencies = accountDao.getAccountWithBalances(KNOWN_ACCOUNT_ID.toString());

        assertThat(accountWithCurrencies).isNotNull();
        assertThat(accountWithCurrencies.getAccount()).isNotNull();
        assertThat(accountWithCurrencies.getAccount().getAccountId()).isEqualTo(KNOWN_ACCOUNT_ID.toString());
        assertThat(accountWithCurrencies.getBalances()).isEmpty();
    }

    @Sql(statements = {
            "INSERT INTO tuum.account(account_id, customer_id) VALUES('909c39bd-e911-4030-a69c-e4b1a7f6054f', '116a84ba-3629-46b3-9fa1-a3667268ce56')",
            "INSERT INTO tuum.balance(account_id, amount, currency) VALUES('909c39bd-e911-4030-a69c-e4b1a7f6054f', 10.0, 'EUR')",
            "INSERT INTO tuum.balance(account_id, amount, currency) VALUES('909c39bd-e911-4030-a69c-e4b1a7f6054f', 20.0, 'GBP')"
    })
    @Test
    void gets_account_with_balances() {
        final var accountWithCurrencies = accountDao.getAccountWithBalances(KNOWN_ACCOUNT_ID.toString());

        assertThat(accountWithCurrencies.getBalances()).hasSize(2);
        assertThat(accountWithCurrencies.getBalances().get(0).getCurrency()).isEqualTo(Currency.EUR);
        assertThat(accountWithCurrencies.getBalances().get(0).getAmount()).isEqualTo(10.0);
        assertThat(accountWithCurrencies.getBalances().get(1).getCurrency()).isEqualTo(Currency.GBP);
        assertThat(accountWithCurrencies.getBalances().get(1).getAmount()).isEqualTo(20.0);
    }

    @Test
    void fails_to_get_account_with_balances_due_to_missing_account() {
        final var missingAccount = accountDao.getAccountWithBalances(MISSING_ACCOUNT_ID.toString());

        assertThat(missingAccount).isNull();
    }
}
