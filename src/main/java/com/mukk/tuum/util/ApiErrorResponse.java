package com.mukk.tuum.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

    private int statusCode;

    private Object message;

    private String detail;

    private String path;

    @Builder.Default
    private LocalDateTime timeStamp = LocalDateTime.now(ZoneOffset.UTC);
}
