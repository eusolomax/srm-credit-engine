package com.srm.credit_engine.controller.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.srm.credit_engine.domain.enums.CurrencyCode;

public record ExchangeRateResponse(
        CurrencyCode fromCurrency,
        CurrencyCode toCurrency,
        BigDecimal rate,
        Instant effectiveAt,
        Instant createdAt) {
}
