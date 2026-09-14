package com.srm.credit_engine.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.srm.credit_engine.domain.enums.CurrencyCode;
import com.srm.credit_engine.domain.enums.ReceivableType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record CreateReceivableRequest(
        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal faceValue,

        @NotNull
        ReceivableType type,

        @NotNull
        CurrencyCode paymentCurrency,

        @NotNull
        Integer termMonths,

        @NotNull
        LocalDate dueDate) {
}
