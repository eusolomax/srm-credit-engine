package com.srm.credit_engine.controller.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.srm.credit_engine.domain.enums.CurrencyCode;
import com.srm.credit_engine.domain.enums.ReceivableStatus;
import com.srm.credit_engine.domain.enums.ReceivableType;

public record ReceivableResponse(
        BigDecimal faceValue,
        ReceivableType type,
        CurrencyCode paymentCurrency,
        Integer termMonths,
        LocalDate dueDate,
        ReceivableStatus status,
        Instant createdAt) {
}