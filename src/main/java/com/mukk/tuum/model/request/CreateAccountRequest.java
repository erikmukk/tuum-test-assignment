package com.mukk.tuum.model.request;

import com.mukk.tuum.model.enums.Currency;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
public class CreateAccountRequest {

    @NotNull
    private String customerId;

    @NotNull
    private String country;

    @NotNull
    private List<Currency> currencies;
}
