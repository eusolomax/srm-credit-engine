package com.srm.credit_engine.controller.dto;

import java.math.BigDecimal;

import com.srm.credit_engine.domain.enums.CurrencyCode;
import com.srm.credit_engine.domain.enums.ReceivableType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreatePricingSimulationRequest(
        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal faceValue,

        @NotNull
        ReceivableType type,

        @NotNull
        CurrencyCode paymentCurrency,

        @NotNull
        @Positive
        Integer termMonths) {
}
