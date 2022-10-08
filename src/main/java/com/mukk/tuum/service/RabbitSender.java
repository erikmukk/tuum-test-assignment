package com.mukk.tuum.service;

import com.mukk.tuum.model.rabbit.RabbitDatabaseAction;
import com.mukk.tuum.model.rabbit.RabbitDatabaseTable;
import com.mukk.tuum.model.rabbit.RabbitMessage;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitSender {

    @Autowired
    private AmqpTemplate rabbitTemplate;

    @Autowired
    private Queue queue;

    public void send(RabbitDatabaseAction databaseAction, RabbitDatabaseTable table, Object message) {
        rabbitTemplate.convertAndSend(queue.getName(), createMessage(databaseAction, table, message));
    }

    private RabbitMessage createMessage(RabbitDatabaseAction databaseAction, RabbitDatabaseTable table, Object message) {
        return RabbitMessage.builder()
                .data(message)
                .table(table)
                .databaseAction(databaseAction)
                .build();
    }
}
