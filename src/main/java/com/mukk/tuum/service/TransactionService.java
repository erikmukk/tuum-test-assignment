package com.mukk.tuum.service;

import com.mukk.tuum.exception.AccountMissingException;
import com.mukk.tuum.exception.TransactionException;
import com.mukk.tuum.model.enums.Currency;
import com.mukk.tuum.model.rabbit.RabbitDatabaseAction;
import com.mukk.tuum.model.rabbit.RabbitDatabaseTable;
import com.mukk.tuum.model.request.TransactionRequest;
import com.mukk.tuum.model.response.CreateTransactionResponse;
import com.mukk.tuum.persistence.dao.TransactionDao;
import com.mukk.tuum.persistence.entity.gen.TransactionEntity;
import com.mukk.tuum.util.TransactionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {

    private final AccountService accountService;
    private final BalanceService balanceService;
    private final TransactionDao transactionDao;
    private final RabbitSender rabbitSender;

    @Transactional(readOnly = true)
    public List<TransactionEntity> get(UUID accountId) throws AccountMissingException {
        accountService.verifyAccountExists(accountId);
        return transactionDao.getByAccountId(accountId.toString());
    }

    public CreateTransactionResponse create(TransactionRequest request) throws AccountMissingException, TransactionException {
        accountService.verifyAccountExists(request.getAccountId());
        balanceService.verifyAccountHasCurrency(request.getCurrency(), request.getAccountId());

        var balanceBeingUpdated = balanceService.getBalanceForUpdating(request.getAccountId(), request.getCurrency());

        TransactionUtils.verifyHasEnoughBalance(balanceBeingUpdated, request);

        final var newBalance = TransactionUtils.getNewAmount(balanceBeingUpdated.getAmount(), request.getAmount(), request.getDirection());
        balanceBeingUpdated.setAmount(newBalance);
        balanceService.updateBalance(balanceBeingUpdated);

        final var transaction = createTransaction(request);

        insertTransaction(transaction);

        return CreateTransactionResponse.builder()
                .accountId(request.getAccountId())
                .transactionId(UUID.fromString(transaction.getTransactionId()))
                .amount(request.getAmount())
                .currency(Currency.valueOf(balanceBeingUpdated.getCurrency()))
                .direction(request.getDirection())
                .description(request.getDescription())
                .balance(balanceBeingUpdated.getAmount())
                .build();
    }

    private TransactionEntity createTransaction(TransactionRequest request) {
        return TransactionEntity.builder()
                .accountId(request.getAccountId().toString())
                .currency(request.getCurrency().getValue())
                .amount(request.getAmount())
                .description(request.getDescription())
                .direction(request.getDirection().getValue())
                .build();
    }

    private int insertTransaction(TransactionEntity entity) {
        final var insert = transactionDao.insert(entity);
        if (insert == 1) {
            rabbitSender.send(RabbitDatabaseAction.INSERT, RabbitDatabaseTable.TRANSACTION, entity);
        }
        return insert;
    }
}
