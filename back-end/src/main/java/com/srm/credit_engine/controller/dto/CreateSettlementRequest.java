package com.srm.credit_engine.controller.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateSettlementRequest(
        @NotNull
        @Positive
        Long receivableId) {
}
