package com.srm.credit_engine.controller.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.srm.credit_engine.domain.enums.CurrencyCode;

public record SettlementResponse(
        Long receivableId,
        String assignor,
        BigDecimal faceValue,
        BigDecimal presentValue,
        BigDecimal discount,
        CurrencyCode paymentCurrency,
        BigDecimal exchangeRate,
        Instant settledAt) {
}
