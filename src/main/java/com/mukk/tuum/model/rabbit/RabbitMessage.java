package com.mukk.tuum.model.rabbit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class RabbitMessage {

    private RabbitDatabaseAction databaseAction;

    private RabbitDatabaseTable table;

    private Object data;
}
