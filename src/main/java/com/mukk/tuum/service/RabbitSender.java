package com.mukk.tuum.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mukk.tuum.model.rabbit.RabbitDatabaseAction;
import com.mukk.tuum.model.rabbit.RabbitDatabaseTable;
import com.mukk.tuum.model.rabbit.RabbitMessage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RabbitSender {

    private final RabbitTemplate rabbitTemplate;

    private final Queue queue;

    private final ObjectMapper objectMapper;

    @SneakyThrows
    public void send(RabbitDatabaseAction databaseAction, RabbitDatabaseTable table, Object message) {
        rabbitTemplate.convertAndSend(queue.getName(), objectMapper.writeValueAsString(createMessage(databaseAction, table, message)));
    }

    private RabbitMessage createMessage(RabbitDatabaseAction databaseAction, RabbitDatabaseTable table, Object message) {
        return RabbitMessage.builder()
                .data(message)
                .table(table)
                .databaseAction(databaseAction)
                .build();
    }
}
