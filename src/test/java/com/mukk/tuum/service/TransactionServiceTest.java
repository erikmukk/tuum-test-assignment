package com.mukk.tuum.service;

import com.mukk.tuum.exception.AccountMissingException;
import com.mukk.tuum.exception.InsufficientFundsException;
import com.mukk.tuum.exception.InvalidCurrencyException;
import com.mukk.tuum.model.enums.Currency;
import com.mukk.tuum.model.enums.TransactionDirection;
import com.mukk.tuum.model.request.TransactionRequest;
import com.mukk.tuum.persistence.dao.TransactionDao;
import com.mukk.tuum.persistence.entity.gen.BalanceEntity;
import com.mukk.tuum.persistence.entity.gen.TransactionEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    private static final UUID ACCOUNT_ID = UUID.randomUUID();
    private static final UUID TRANSACTION_ID = UUID.randomUUID();

    @Mock
    private AccountService accountService;
    @Mock
    private BalanceService balanceService;
    @Mock
    private TransactionDao transactionDao;

    @InjectMocks
    private TransactionService transactionService;

    static Stream<Arguments> transactionArgumentProvider() {
        return Stream.of(
                Arguments.arguments(
                        TransactionDirection.IN, 15.0
                ),
                Arguments.arguments(
                        TransactionDirection.OUT, 35.0
                )
        );
    }

    @Test
    void gets_account_transactions() {
        when(transactionDao.getByAccountId(ACCOUNT_ID.toString())).thenReturn(List.of());

        final var result = assertDoesNotThrow(() -> transactionService.get(ACCOUNT_ID));

        verify(transactionDao).getByAccountId(ACCOUNT_ID.toString());
        assertThat(result).isNotNull();
    }

    @Test
    void does_not_get_account_transactions_due_to_missing_account() throws AccountMissingException {
        doThrow(new AccountMissingException("")).when(accountService).verifyAccountExists(ACCOUNT_ID);

        assertThrows(AccountMissingException.class, () -> transactionService.get(ACCOUNT_ID));
    }

    @ParameterizedTest
    @MethodSource("transactionArgumentProvider")
    void creates_transaction(TransactionDirection transactionDirection, Double initialBalance) {
        final var transactionRequest = TransactionRequest.builder()
                .accountId(ACCOUNT_ID)
                .description("DESC")
                .direction(transactionDirection)
                .currency(Currency.EUR)
                .amount(10.0)
                .build();
        var balance = BalanceEntity.builder()
                .accountId(ACCOUNT_ID.toString())
                .currency(Currency.EUR.getValue())
                .amount(initialBalance)
                .build();

        when(balanceService.getBalanceForUpdating(ACCOUNT_ID, transactionRequest.getCurrency()))
                .thenReturn(balance);
        mockTransactionDaoInsert();

        final var result = assertDoesNotThrow(() -> transactionService.create(transactionRequest));

        verify(balanceService).updateBalance(balance);
        verify(transactionDao).insert(any(TransactionEntity.class));
        assertThat(balance.getAmount()).isEqualTo(25.0);
        assertThat(result).isNotNull();
    }

    @Test
    void fails_to_create_transaction_due_to_missing_account() throws AccountMissingException {
        final var transactionRequest = TransactionRequest.builder().accountId(ACCOUNT_ID).build();

        doThrow(new AccountMissingException("")).when(accountService).verifyAccountExists(ACCOUNT_ID);

        assertThrows(AccountMissingException.class, () -> transactionService.create(transactionRequest));
    }

    @Test
    void fails_to_create_transaction_due_to_unavailable_currency() throws InvalidCurrencyException {
        final var transactionRequest = TransactionRequest.builder()
                .accountId(ACCOUNT_ID)
                .currency(Currency.GBP)
                .build();

        doThrow(new InvalidCurrencyException("")).when(balanceService)
                .verifyAccountHasCurrency(Currency.GBP, ACCOUNT_ID);

        assertThrows(InvalidCurrencyException.class, () -> transactionService.create(transactionRequest));
    }

    @Test
    void fails_to_create_transaction_due_to_invalid_funds() {
        final var transactionRequest = TransactionRequest.builder()
                .accountId(ACCOUNT_ID)
                .description("DESC")
                .direction(TransactionDirection.OUT)
                .currency(Currency.EUR)
                .amount(10.0)
                .build();
        final var balance = BalanceEntity.builder()
                .accountId(ACCOUNT_ID.toString())
                .currency(Currency.EUR.getValue())
                .amount(5.0)
                .build();

        when(balanceService.getBalanceForUpdating(ACCOUNT_ID, transactionRequest.getCurrency()))
                .thenReturn(balance);

        final var exception = assertThrows(InsufficientFundsException.class,
                () -> transactionService.create(transactionRequest));

        assertThat(exception.getMessage()).isEqualTo(String.format("Insufficient funds. Got '%s', required '%s'", 5.0, 10.0));
    }

    /*
    Since my myBatis implementation creates and sets accountId in TransactionEntity while/after insert
    automatically (I do not provide a value prior to inserting),
    then I need this more complex mocking logic.
     */
    private void mockTransactionDaoInsert() {
        when(transactionDao.insert(any(TransactionEntity.class))).thenAnswer((Answer<Integer>) invocation -> {
            TransactionEntity transactionEntity = invocation.getArgument(0);
            transactionEntity.setTransactionId(TRANSACTION_ID.toString());
            return 1;
        });
    }
}