package com.mukk.tuum.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mukk.tuum.model.rabbit.RabbitDatabaseAction;
import com.mukk.tuum.model.rabbit.RabbitDatabaseTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RabbitSenderTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Mock
    private RabbitTemplate rabbitTemplate;
    @Mock
    private Queue queue;
    @InjectMocks
    private RabbitSender rabbitSender;

    @BeforeEach
    void setUp() {
        rabbitSender = new RabbitSender(rabbitTemplate, queue, objectMapper);
    }

    @Test
    void sends_message() {
        when(queue.getName()).thenReturn("queue-name");

        rabbitSender.send(RabbitDatabaseAction.INSERT, RabbitDatabaseTable.ACCOUNT, "{}");

        verify(rabbitTemplate).convertAndSend("queue-name", "{\"databaseAction\":\"INSERT\",\"table\":\"ACCOUNT\",\"data\":\"{}\"}");
    }
}
