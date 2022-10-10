package com.mukk.tuum.model.request;

import com.mukk.tuum.model.enums.Currency;
import com.mukk.tuum.model.enums.TransactionDirection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class CreateTransactionRequest {

    @NotNull
    private UUID accountId;

    @NotNull
    @DecimalMin(value = "0.01")
    @Digits(integer = 9, fraction = 2)
    private BigDecimal amount;

    @NotNull
    private Currency currency;

    @NotNull
    private TransactionDirection direction;

    @NotBlank
    @Size(max = 255)
    private String description;
}
