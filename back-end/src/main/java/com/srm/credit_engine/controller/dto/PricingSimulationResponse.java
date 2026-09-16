package com.srm.credit_engine.controller.dto;

import java.math.BigDecimal;

import com.srm.credit_engine.domain.enums.CurrencyCode;

public record PricingSimulationResponse(
        BigDecimal faceValue,
        BigDecimal presentValue,
        BigDecimal discount,
        CurrencyCode paymentCurrency) {
}
