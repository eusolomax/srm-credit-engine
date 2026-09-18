package com.srm.credit_engine.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChequePricingStrategyTest {

    private final ChequePricingStrategy strategy = new ChequePricingStrategy(new BigDecimal("0.0100"));

    private BigDecimal roundToCents(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_EVEN);
    }

    // C2: CHEQUE, R$25.000, 2 meses → R$23.337,77
    @Test
    void shouldReturnPresentValueFromGoldenCaseC2() {
        BigDecimal presentValue = strategy.calculatePresentValue(new BigDecimal("25000.00"), 2);

        assertThat(roundToCents(presentValue)).isEqualByComparingTo(new BigDecimal("23337.77"));
    }

}
