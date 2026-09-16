package com.srm.credit_engine.controller.dto;

import com.srm.credit_engine.domain.enums.CurrencyCode;

import java.math.BigDecimal;

public record PricingResult(
        BigDecimal roundedFaceValue,
        BigDecimal roundedPresentValue,
        BigDecimal roundedDiscount,
        CurrencyCode paymentCurrency,
        BigDecimal exchangeRateValue
) {}
