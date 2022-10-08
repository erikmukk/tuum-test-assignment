package com.mukk.tuum.model.request;

import com.mukk.tuum.model.enums.Currency;
import com.mukk.tuum.model.enums.TransactionDirection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class TransactionRequest {

    @NotNull
    private UUID accountId;

    @NotNull
    @Min(0L)
    private Double amount;

    @NotNull
    private Currency currency;

    @NotNull
    private TransactionDirection direction;

    @NotBlank
    @Size(max = 255)
    private String description;
}
