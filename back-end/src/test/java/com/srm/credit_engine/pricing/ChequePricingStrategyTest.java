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

    // Retorna o valor presente utilizando o base rate definido em application properties
    // Ou utilizando o valor default de 1%
    @Test
    void shouldUseConfiguredBaseRate() {
        ChequePricingStrategy configuredStrategy =
                new ChequePricingStrategy(new BigDecimal("0.0200"));

        BigDecimal presentValue = configuredStrategy.calculatePresentValue(new BigDecimal("1000.00"), 1);

        assertThat(roundToCents(presentValue)).isEqualByComparingTo(new BigDecimal("956.94"));
    }

    // Retorna o valor de face caso o prazo seja de 0 meses
    @Test
    void shouldReturnFaceValueWhenTermIsZero() {
        BigDecimal presentValue = strategy.calculatePresentValue(new BigDecimal("50000.00"), 0);

        assertThat(roundToCents(presentValue)).isEqualByComparingTo(new BigDecimal("50000.00"));
    }
}
