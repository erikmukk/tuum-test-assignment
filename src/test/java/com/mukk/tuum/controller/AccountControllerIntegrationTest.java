package com.mukk.tuum.controller;

import com.mukk.tuum.model.enums.Currency;
import com.mukk.tuum.model.rabbit.RabbitDatabaseAction;
import com.mukk.tuum.model.rabbit.RabbitDatabaseTable;
import com.mukk.tuum.model.request.CreateAccountRequest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.UUID;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class AccountControllerIntegrationTest extends IntegrationTestBase {

    private static final String ACCOUNT_BASE_PATH = "/account";
    private static final String ACCOUNT_BY_ID_PATH = ACCOUNT_BASE_PATH + "/{accountId}";
    private static final UUID MISSING_ACCOUNT_ID = UUID.randomUUID();
    private static final UUID KNOWN_ACCOUNT_ID = UUID.fromString("909c39bd-e911-4030-a69c-e4b1a7f6054f");
    private static final UUID KNOWN_CUSTOMER_ID = UUID.fromString("116a84ba-3629-46b3-9fa1-a3667268ce56");

    @Test
    void gets_account_by_id_account_missing() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get(ACCOUNT_BY_ID_PATH, MISSING_ACCOUNT_ID)
        .then()
            .log().all()
            .statusCode(HttpStatus.NOT_FOUND.value())
            .body("statusCode", equalTo(HttpStatus.NOT_FOUND.value()))
            .body("message", equalTo(String.format("Account with id '%s' does not exist.", MISSING_ACCOUNT_ID)))
            .body("path", equalTo(String.format("/account/%s", MISSING_ACCOUNT_ID)))
            .body("timeStamp", notNullValue());
    }

    @Sql(statements = {
            "INSERT INTO tuum.account(account_id, customer_id, country) VALUES('909c39bd-e911-4030-a69c-e4b1a7f6054f', '116a84ba-3629-46b3-9fa1-a3667268ce56', 'EST')",
            "INSERT INTO tuum.balance(account_id, amount, currency) VALUES ('909c39bd-e911-4030-a69c-e4b1a7f6054f', 10.75, 'EUR')",
            "INSERT INTO tuum.balance(account_id, amount, currency) VALUES ('909c39bd-e911-4030-a69c-e4b1a7f6054f', 75.10, 'SEK')"
    })
    @Test
    void gets_account_by_id() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get(ACCOUNT_BY_ID_PATH, KNOWN_ACCOUNT_ID)
        .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .body("accountId", equalTo(KNOWN_ACCOUNT_ID.toString()))
            .body("customerId", equalTo(KNOWN_CUSTOMER_ID.toString()))
            .body("balances", hasSize(2))
            .body("balances[0].currency", equalTo(Currency.EUR.getValue()))
            .body("balances[0].amount", is(10.75f))
            .body("balances[1].currency", equalTo(Currency.SEK.getValue()))
            .body("balances[1].amount", is(75.10f));
    }

    @Test
    void creates_account() {
        final var request = CreateAccountRequest.builder()
                .customerId(KNOWN_CUSTOMER_ID.toString())
                .currencies(List.of(Currency.EUR, Currency.GBP))
                .country("EST")
                .build();

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post(ACCOUNT_BASE_PATH)
        .then()
            .log().all()
            .statusCode(HttpStatus.CREATED.value())
            .body("accountId", notNullValue())
            .body("customerId", equalTo(KNOWN_CUSTOMER_ID.toString()))
            .body("balances", hasSize(2))
            .body("balances[0].currency", equalTo(Currency.EUR.getValue()))
            .body("balances[0].amount", is(0.00f))
            .body("balances[1].currency", equalTo(Currency.GBP.getValue()))
            .body("balances[1].amount", is(0.00f));
        verify(rabbitSender).send(eq(RabbitDatabaseAction.INSERT), eq(RabbitDatabaseTable.ACCOUNT), ArgumentMatchers.any());
        verify(rabbitSender, times(2)).send(eq(RabbitDatabaseAction.INSERT), eq(RabbitDatabaseTable.BALANCE), ArgumentMatchers.any());
    }

    @Test
    void fails_to_create_account_invalid_request_body() {
        given()
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .post(ACCOUNT_BASE_PATH)
        .then()
            .log().all()
            .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
            .body("statusCode", equalTo(HttpStatus.UNPROCESSABLE_ENTITY.value()))
            .body("message", hasSize(4))
            .body("message", hasItem("customerId field - must not be blank"))
            .body("message", hasItem("currencies field - must not be null"))
            .body("message", hasItem("country field - must not be blank"))
            .body("message", hasItem("country field - invalid country code"))
            .body("path", equalTo("/account"))
            .body("timeStamp", notNullValue());
    }

    @Test
    void fails_to_create_account_invalid_enum_value() {
        given()
            .contentType(ContentType.JSON)
            .body("{ " +
                    " \"customerId\": \"116a84ba-3629-46b3-9fa1-a3667268ce56\", " +
                    " \"currencies\": [\"EURS\"], " +
                    " \"country\": \"EST\" " +
                    "}")
        .when()
            .post(ACCOUNT_BASE_PATH)
        .then()
            .log().all()
            .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
            .body("statusCode", equalTo(HttpStatus.UNPROCESSABLE_ENTITY.value()))
            .body("message", equalTo("Invalid value for field 'currencies'."))
            .body("detail", equalTo("Provided value 'EURS', expected one of '[EUR, GBP, SEK, USD]'"))
            .body("path", equalTo("/account"))
            .body("timeStamp", notNullValue());
    }

}
