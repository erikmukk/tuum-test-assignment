package com.mukk.tuum.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransactionDirection {
    IN("IN"),
    OUT("OUT");

    private final String value;
}
