package com.mukk.tuum.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Currency {
    EUR("EUR"),
    GBP("GBP"),
    SEK("SEK"),
    USD("USD");

    private final String value;
}
