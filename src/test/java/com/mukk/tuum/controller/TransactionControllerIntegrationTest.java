package com.mukk.tuum.controller;

import com.mukk.tuum.model.enums.Currency;
import com.mukk.tuum.model.enums.TransactionDirection;
import com.mukk.tuum.model.request.TransactionRequest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

class TransactionControllerIntegrationTest extends IntegrationTestBase {

    private static final String TRANSACTION_BASE_PATH = "/transaction";
    private static final String TRANSACTION_BY_ACCOUNT_PATH = TRANSACTION_BASE_PATH + "/account/${accountId}";

    @Test
    void fails_to_create_transaction_account_missing() {
        final var accountId = UUID.randomUUID();
        final var body = TransactionRequest.builder()
                .accountId(accountId)
                .direction(TransactionDirection.IN)
                .amount(10.0)
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
            .body("message", equalTo(String.format("Account with id '%s' does not exist.", accountId)))
            .body("path", equalTo("/transaction"))
            .body("timeStamp", notNullValue());
    }
}
