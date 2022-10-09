package com.mukk.tuum.controller;

import com.mukk.tuum.ContainerTestBase;
import com.mukk.tuum.service.RabbitSender;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;

import static io.restassured.config.EncoderConfig.encoderConfig;

@ActiveProfiles("integration-test")
@AutoConfigureMockMvc
@SpringBootTest
public abstract class IntegrationTestBase extends ContainerTestBase {

    @MockBean
    RabbitSender rabbitSender;

    @Autowired
    private WebApplicationContext ctx;

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.webAppContextSetup(ctx);
        RestAssuredMockMvc.config()
                .encoderConfig(encoderConfig()
                        .defaultCharsetForContentType(StandardCharsets.UTF_8, ContentType.JSON)
                        .defaultCharsetForContentType(StandardCharsets.UTF_8, ContentType.XML)
                        .defaultContentCharset(StandardCharsets.UTF_8));
    }
}
