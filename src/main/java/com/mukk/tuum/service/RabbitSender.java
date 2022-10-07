package com.mukk.tuum.service;

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
    public void send() {
        rabbitTemplate.convertAndSend(queue.getName(), "testmessage");
    }
}
