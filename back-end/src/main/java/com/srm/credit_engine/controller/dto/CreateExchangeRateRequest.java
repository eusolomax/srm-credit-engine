package com.srm.credit_engine.controller.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.srm.credit_engine.domain.enums.CurrencyCode;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record CreateExchangeRateRequest(
        @NotNull
        CurrencyCode fromCurrency,

        @NotNull
        CurrencyCode toCurrency,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal rate,

        @NotNull
        Instant effectiveAt) {
}
