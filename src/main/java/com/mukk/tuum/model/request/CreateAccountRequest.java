package com.mukk.tuum.model.request;

import com.mukk.tuum.model.enums.Currency;
import com.mukk.tuum.validator.countrycode.CountryCodeConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class CreateAccountRequest {

    @NotBlank
    private String customerId;

    @NotBlank
    @Size(min = 3, max = 3)
    @CountryCodeConstraint
    private String country;

    @NotNull
    private List<Currency> currencies;
}
