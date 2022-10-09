package com.mukk.tuum.service;

import com.mukk.tuum.exception.AccountMissingException;
import com.mukk.tuum.exception.TransactionException;
import com.mukk.tuum.model.request.CreateTransactionRequest;
import com.mukk.tuum.model.response.CreateTransactionResponse;
import com.mukk.tuum.model.response.TransactionResponse;
import com.mukk.tuum.persistence.dao.TransactionDao;
import com.mukk.tuum.persistence.entity.gen.TransactionEntity;
import com.mukk.tuum.util.TransactionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {

    private final AccountService accountService;
    private final BalanceService balanceService;
    private final TransactionDao transactionDao;

    @Transactional(readOnly = true)
    public List<TransactionResponse> get(UUID accountId) throws AccountMissingException {
        accountService.verifyAccountExists(accountId);
        final var transactions = transactionDao.getByAccountId(accountId.toString());

        return transactions.stream()
                .map(TransactionResponse::fromTransactionEntity)
                .collect(Collectors.toList());
    }

    public CreateTransactionResponse create(CreateTransactionRequest request) throws AccountMissingException, TransactionException {
        accountService.verifyAccountExists(request.getAccountId());
        balanceService.verifyAccountHasCurrency(request.getCurrency(), request.getAccountId());

        var balanceBeingUpdated = balanceService.getBalanceForUpdating(request.getAccountId(), request.getCurrency());

        TransactionUtils.verifyHasEnoughBalance(balanceBeingUpdated, request);

        final var newBalance = TransactionUtils.getNewAmount(balanceBeingUpdated.getAmount(), request.getAmount(), request.getDirection());
        balanceBeingUpdated.setAmount(newBalance);
        balanceService.updateBalance(balanceBeingUpdated);

        final var transaction = createTransaction(request);

        insertTransaction(transaction);

        return CreateTransactionResponse.fromTransactionEntityAndBalance(transaction, balanceBeingUpdated.getAmount());
    }

    private TransactionEntity createTransaction(CreateTransactionRequest request) {
        return TransactionEntity.builder()
                .accountId(request.getAccountId().toString())
                .currency(request.getCurrency().getValue())
                .amount(request.getAmount())
                .description(request.getDescription())
                .direction(request.getDirection().getValue())
                .build();
    }

    public int insertTransaction(TransactionEntity entity) {
        return transactionDao.insert(entity);
    }
}
