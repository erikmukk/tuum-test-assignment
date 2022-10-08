package com.mukk.tuum.model.rabbit;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RabbitMessage {

    private RabbitDatabaseAction databaseAction;

    private RabbitDatabaseTable table;

    private Object data;
}
