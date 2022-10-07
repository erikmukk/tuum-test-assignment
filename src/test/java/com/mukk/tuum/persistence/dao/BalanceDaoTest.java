package com.mukk.tuum.persistence.dao;

import com.mukk.tuum.model.enums.Currency;
import com.mukk.tuum.persistence.entity.gen.BalanceEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BalanceDaoTest extends DaoTestBase {

    private static final UUID KNOWN_ACCOUNT_ID = UUID.fromString("909c39bd-e911-4030-a69c-e4b1a7f6054f");
    private static final UUID MISSING_ACCOUNT_ID = UUID.randomUUID();

    @Autowired
    private BalanceDao balanceDao;

    @Sql(statements = "INSERT INTO tuum.account(account_id, customer_id) VALUES('909c39bd-e911-4030-a69c-e4b1a7f6054f', '116a84ba-3629-46b3-9fa1-a3667268ce56')")
    @Test
    void gets_balances_by_account_id_missing_account() {
        final var balances = balanceDao.getBalancesByAccountId(MISSING_ACCOUNT_ID.toString());

        assertThat(balances).isNotNull().isEmpty();
    }

    @Sql(statements = "INSERT INTO tuum.account(account_id, customer_id) VALUES('909c39bd-e911-4030-a69c-e4b1a7f6054f', '116a84ba-3629-46b3-9fa1-a3667268ce56')")
    @Test
    void gets_balances_by_account_id_no_balances() {
        final var balances = balanceDao.getBalancesByAccountId(KNOWN_ACCOUNT_ID.toString());

        assertThat(balances).isNotNull().isEmpty();
    }

    @Sql(statements = {
            "INSERT INTO tuum.account(account_id, customer_id) VALUES('909c39bd-e911-4030-a69c-e4b1a7f6054f', '116a84ba-3629-46b3-9fa1-a3667268ce56')",
            "INSERT INTO tuum.balance(account_id, amount, currency) VALUES ('909c39bd-e911-4030-a69c-e4b1a7f6054f', 10.75, 'EUR')",
            "INSERT INTO tuum.balance(account_id, amount, currency) VALUES ('909c39bd-e911-4030-a69c-e4b1a7f6054f', 75.10, 'SEK')"
    })
    @Test
    void gets_two_balances_by_account_id() {
        final var balances = balanceDao.getBalancesByAccountId(KNOWN_ACCOUNT_ID.toString());

        assertThat(balances).isNotNull().hasSize(2);
        assertBalance(balances.get(0), 10.75, Currency.EUR);
        assertBalance(balances.get(1), 75.10, Currency.SEK);
    }

    @Test
    void gets_currencies_by_account_id_missing_account() {
        final var currencies = balanceDao.getAccountCurrencies(MISSING_ACCOUNT_ID.toString());

        assertThat(currencies).isNotNull().isEmpty();
    }

    @Sql(statements = "INSERT INTO tuum.account(account_id, customer_id) VALUES('909c39bd-e911-4030-a69c-e4b1a7f6054f', '116a84ba-3629-46b3-9fa1-a3667268ce56')")
    @Test
    void gets_currencies_by_account_id_no_balances() {
        final var currencies = balanceDao.getAccountCurrencies(KNOWN_ACCOUNT_ID.toString());

        assertThat(currencies).isNotNull().isEmpty();
    }

    @Sql(statements = {
            "INSERT INTO tuum.account(account_id, customer_id) VALUES('909c39bd-e911-4030-a69c-e4b1a7f6054f', '116a84ba-3629-46b3-9fa1-a3667268ce56')",
            "INSERT INTO tuum.balance(account_id, amount, currency) VALUES ('909c39bd-e911-4030-a69c-e4b1a7f6054f', 10.75, 'EUR')",
            "INSERT INTO tuum.balance(account_id, amount, currency) VALUES ('909c39bd-e911-4030-a69c-e4b1a7f6054f', 75.10, 'SEK')"
    })
    @Test
    void gets_two_currencies_by_account_id() {
        final var currencies = balanceDao.getAccountCurrencies(KNOWN_ACCOUNT_ID.toString());

        assertThat(currencies).isNotNull().hasSize(2);
        assertThat(currencies.get(0).getValue()).isEqualTo(Currency.EUR.getValue());
        assertThat(currencies.get(1).getValue()).isEqualTo(Currency.SEK.getValue());
    }

    private void assertBalance(BalanceEntity balance, double expectedAmount, Currency expectedCurrency) {
        assertThat(balance.getAmount()).isEqualTo(expectedAmount);
        assertThat(balance.getCurrency()).isEqualTo(expectedCurrency.getValue());
        assertThat(balance.getAccountId()).isEqualTo(KNOWN_ACCOUNT_ID.toString());
    }

}