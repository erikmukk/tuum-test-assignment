package com.mukk.tuum.persistence.entity;

import com.mukk.tuum.model.enums.Currency;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Balance {

    private Currency currency;

    @Setter(AccessLevel.NONE)
    private BigDecimal amount;

    public void setAmount(Double amount) {
        this.amount = BigDecimal.valueOf(amount).setScale(2, RoundingMode.UP);
    }

}