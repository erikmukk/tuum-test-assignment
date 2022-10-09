package com.mukk.tuum.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mukk.tuum.model.rabbit.RabbitDatabaseAction;
import com.mukk.tuum.model.rabbit.RabbitDatabaseTable;
import com.mukk.tuum.model.rabbit.RabbitMessage;
import com.mukk.tuum.persistence.entity.gen.AccountEntity;
import com.mukk.tuum.persistence.entity.gen.BalanceEntity;
import com.mukk.tuum.persistence.entity.gen.TransactionEntity;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RabbitSender {

    private final RabbitTemplate rabbitTemplate;

    private final Queue queue;

    private final ObjectMapper objectMapper;

    @SneakyThrows
    public void send(RabbitDatabaseAction databaseAction, RabbitDatabaseTable table, Object message) {
        var specificMessage = "";
        if (message instanceof AccountEntity) {
            specificMessage = String.format("accountId='%s'", ((AccountEntity)message).getAccountId());
        } else if (message instanceof TransactionEntity) {
            specificMessage = String.format("transactionId='%s'", ((TransactionEntity)message).getTransactionId());
        } else if (message instanceof BalanceEntity) {
            specificMessage = String.format("balanceId='%s'", ((BalanceEntity)message).getBalanceId());
        }
        rabbitTemplate.convertAndSend(queue.getName(), objectMapper.writeValueAsString(createMessage(databaseAction, table, message)));
        log.info("Publishing RabbitMQ message on {} {}, {}", databaseAction, table, specificMessage);
    }

    private RabbitMessage createMessage(RabbitDatabaseAction databaseAction, RabbitDatabaseTable table, Object message) {
        return RabbitMessage.builder()
                .data(message)
                .table(table)
                .databaseAction(databaseAction)
                .build();
    }
}
