package com.mukk.tuum.persistence.dao;

import com.mukk.tuum.model.enums.Currency;
import com.mukk.tuum.model.enums.TransactionDirection;
import com.mukk.tuum.persistence.entity.gen.TransactionEntity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.jdbc.Sql;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionDaoTest extends DaoTestBase {

    private static final UUID KNOWN_ACCOUNT_ID = UUID.fromString("909c39bd-e911-4030-a69c-e4b1a7f6054f");
    private static final UUID MISSING_ACCOUNT_ID = UUID.randomUUID();

    @Autowired
    private TransactionDao transactionDao;

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class Get_transaction_succeeds {

        @Test
        void when_account_is_missing() {
            final var result = transactionDao.getByAccountId(MISSING_ACCOUNT_ID.toString());

            assertThat(result).isNotNull().isEmpty();
        }

        @Sql(statements = "INSERT INTO tuum.account(account_id, customer_id) VALUES('909c39bd-e911-4030-a69c-e4b1a7f6054f', '116a84ba-3629-46b3-9fa1-a3667268ce56')")
        @Test
        void when_none_are_present() {
            final var result = transactionDao.getByAccountId(KNOWN_ACCOUNT_ID.toString());

            assertThat(result).isNotNull().isEmpty();
        }

        @Sql(statements = {
                "INSERT INTO tuum.account(account_id, customer_id) VALUES('909c39bd-e911-4030-a69c-e4b1a7f6054f', '116a84ba-3629-46b3-9fa1-a3667268ce56')",
                "INSERT INTO tuum.transaction(account_id, amount, currency, description, direction) VALUES ('909c39bd-e911-4030-a69c-e4b1a7f6054f', 10.0, 'EUR', 'in 10', 'IN')",
                "INSERT INTO tuum.transaction(account_id, amount, currency, description, direction) VALUES ('909c39bd-e911-4030-a69c-e4b1a7f6054f', 5.0, 'EUR', 'out 5', 'OUT')"
        })
        @Test
        void when_two_transactions_are_present() {
            final var result = transactionDao.getByAccountId(KNOWN_ACCOUNT_ID.toString());

            assertThat(result).isNotNull().hasSize(2);
            assertTransaction(result.get(0), 10.0, Currency.EUR, "in 10", TransactionDirection.IN);
            assertTransaction(result.get(1), 5.0, Currency.EUR, "out 5", TransactionDirection.OUT);
        }

        private void assertTransaction(
                TransactionEntity transaction,
                Double expectedAmount,
                Currency expectedCurrency,
                String expectedDescription,
                TransactionDirection expectedDirection
        ) {
            assertThat(transaction.getAmount()).isEqualTo(expectedAmount);
            assertThat(transaction.getCurrency()).isEqualTo(expectedCurrency.getValue());
            assertThat(transaction.getDescription()).isEqualTo(expectedDescription);
            assertThat(transaction.getDirection()).isEqualTo(expectedDirection.getValue());
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class Insert_transaction {

        @Test
        @Sql(statements = "INSERT INTO tuum.account(account_id, customer_id) VALUES('909c39bd-e911-4030-a69c-e4b1a7f6054f', '116a84ba-3629-46b3-9fa1-a3667268ce56')")
        void succeeds() {
            final var transaction = TransactionEntity.builder()
                    .accountId(KNOWN_ACCOUNT_ID.toString())
                    .amount(10.0)
                    .currency(Currency.GBP.getValue())
                    .description("desc")
                    .direction(TransactionDirection.IN.getValue())
                    .build();

            final var result = transactionDao.insert(transaction);

            assertThat(result).isEqualTo(1);
            assertThat(transaction.getTransactionId()).isNotNull();
        }

        @Test
        void fails_due_to_missing_account() {
            final var transaction = TransactionEntity.builder()
                    .accountId(MISSING_ACCOUNT_ID.toString())
                    .amount(10.0)
                    .currency(Currency.GBP.getValue())
                    .description("desc")
                    .direction(TransactionDirection.IN.getValue())
                    .build();

            assertThrows(DataIntegrityViolationException.class, () -> transactionDao.insert(transaction));
        }
    }
}
