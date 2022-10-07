package com.mukk.tuum.controller;

import com.mukk.tuum.exception.AccountMissingException;
import com.mukk.tuum.model.request.CreateAccountRequest;
import com.mukk.tuum.model.response.AccountResponse;
import com.mukk.tuum.service.AccountService;
import com.mukk.tuum.service.RabbitSender;
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
import java.util.UUID;

@RestController
@RequestMapping("account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    private final RabbitSender rabbitSender;

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> get(@PathVariable final UUID accountId) throws AccountMissingException {
        rabbitSender.send();
        return ServiceResponseUtil.ok(accountService.getAccountWithBalances(accountId));
    }

    @PostMapping
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody final CreateAccountRequest request) {
        return ServiceResponseUtil.created(accountService.create(request));
    }
}
