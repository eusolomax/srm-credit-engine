package com.srm.credit_engine.controller.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateSettlementRequest(
        @NotNull
        @Positive
        Long receivableId,

        @NotBlank
        @Size(max = 64)
        String idempotencyKey) {
}
