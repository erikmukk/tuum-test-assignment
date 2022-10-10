package com.mukk.tuum.controller;

import com.mukk.tuum.model.enums.Currency;
import com.mukk.tuum.model.enums.TransactionDirection;
import com.mukk.tuum.model.request.CreateTransactionRequest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.util.UUID;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

class TransactionControllerIntegrationTest extends IntegrationTestBase {

    private static final UUID KNOWN_ACCOUNT_ID = UUID.fromString("909c39bd-e911-4030-a69c-e4b1a7f6054f");
    private static final UUID MISSING_ACCOUNT_ID = UUID.randomUUID();
    private static final String TRANSACTION_BASE_PATH = "/transaction";
    private static final String TRANSACTION_BY_ACCOUNT_PATH = TRANSACTION_BASE_PATH + "/account/${accountId}";

    @Test
    void fails_to_create_transaction_account_missing() {
        final var body = CreateTransactionRequest.builder()
                .accountId(MISSING_ACCOUNT_ID)
                .direction(TransactionDirection.IN)
                .amount(BigDecimal.valueOf(10.0))
                .currency(Currency.EUR)
                .description("desc")
                .build();

        given()
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post(TRANSACTION_BASE_PATH)
        .then().log().all()
            .statusCode(HttpStatus.NOT_FOUND.value())
            .body("statusCode", equalTo(HttpStatus.NOT_FOUND.value()))
            .body("message", equalTo(String.format("Account with id '%s' does not exist.", MISSING_ACCOUNT_ID)))
            .body("path", equalTo("/transaction"))
            .body("timeStamp", notNullValue());
    }

    @Sql(statements = {
            "INSERT INTO tuum.account(account_id, customer_id, country) VALUES('909c39bd-e911-4030-a69c-e4b1a7f6054f', '116a84ba-3629-46b3-9fa1-a3667268ce56', 'EST')",
            "INSERT INTO tuum.balance(account_id, amount, currency) VALUES ('909c39bd-e911-4030-a69c-e4b1a7f6054f', 10.75, 'EUR')",
            "INSERT INTO tuum.balance(account_id, amount, currency) VALUES ('909c39bd-e911-4030-a69c-e4b1a7f6054f', 75.10, 'SEK')"
    })
    @Test
    void fails_to_create_transaction_insufficient_funds() {
        final var body = CreateTransactionRequest.builder()
                .accountId(KNOWN_ACCOUNT_ID)
                .direction(TransactionDirection.OUT)
                .amount(BigDecimal.valueOf(10.76))
                .currency(Currency.EUR)
                .description("desc")
                .build();

        given()
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post(TRANSACTION_BASE_PATH)
        .then()
            .log().all()
            .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
            .body("statusCode", equalTo(HttpStatus.UNPROCESSABLE_ENTITY.value()))
            .body("message", equalTo("Insufficient funds. Got '10.75', required '10.76'"))
            .body("path", equalTo("/transaction"))
            .body("timeStamp", notNullValue());
    }

    @Sql(statements = {
            "INSERT INTO tuum.account(account_id, customer_id, country) VALUES('909c39bd-e911-4030-a69c-e4b1a7f6054f', '116a84ba-3629-46b3-9fa1-a3667268ce56', 'EST')",
            "INSERT INTO tuum.balance(account_id, amount, currency) VALUES ('909c39bd-e911-4030-a69c-e4b1a7f6054f', 10.75, 'EUR')",
            "INSERT INTO tuum.balance(account_id, amount, currency) VALUES ('909c39bd-e911-4030-a69c-e4b1a7f6054f', 75.10, 'SEK')"
    })
    @Test
    void creates_transaction() {
        final var body = CreateTransactionRequest.builder()
                .accountId(KNOWN_ACCOUNT_ID)
                .direction(TransactionDirection.OUT)
                .amount(BigDecimal.valueOf(10.0))
                .currency(Currency.EUR)
                .description("desc")
                .build();

        given()
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post(TRANSACTION_BASE_PATH)
        .then()
            .log().all()
            .statusCode(HttpStatus.CREATED.value())
            .body("accountId", equalTo(KNOWN_ACCOUNT_ID.toString()))
            .body("transactionId", notNullValue())
            .body("amount", is(10.00f))
            .body("balance", is(0.75f))
            .body("currency", equalTo(Currency.EUR.getValue()))
            .body("direction", equalTo(TransactionDirection.OUT.getValue()))
            .body("description", equalTo("desc"));
    }
}
