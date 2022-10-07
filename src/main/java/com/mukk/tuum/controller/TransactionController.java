package com.mukk.tuum.controller;

import com.mukk.tuum.exception.AccountMissingException;
import com.mukk.tuum.exception.TransactionException;
import com.mukk.tuum.model.request.TransactionRequest;
import com.mukk.tuum.model.response.CreateTransactionResponse;
import com.mukk.tuum.persistence.entity.gen.TransactionEntity;
import com.mukk.tuum.service.TransactionService;
import com.mukk.tuum.util.ServiceResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("transaction")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<CreateTransactionResponse> create(@Valid @RequestBody TransactionRequest request) throws AccountMissingException, TransactionException {
        return ServiceResponseUtil.created(transactionService.create(request));
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<TransactionEntity>> getByAccountId(@PathVariable final UUID accountId) throws AccountMissingException {
        return ServiceResponseUtil.ok(transactionService.get(accountId));
    }
}
