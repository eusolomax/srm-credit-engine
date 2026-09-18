package com.srm.credit_engine.pricing;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DuplicataPricingStrategyTest {

    private static final BigDecimal USD_TO_BRL_RATE = new BigDecimal("5.4321");

    private final DuplicataPricingStrategy strategy = new DuplicataPricingStrategy(new BigDecimal("0.0100"));

    private BigDecimal roundToCents(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_EVEN);
    }

    // C1: DUPLICATA, R$100.000, 3 meses → R$92.859,94
    @Test
    void shouldReturnPresentValueFromGoldenCaseC1() {
        BigDecimal presentValue = strategy.calculatePresentValue(new BigDecimal("100000.00"), 3);

        assertThat(roundToCents(presentValue)).isEqualByComparingTo(new BigDecimal("92859.94"));
    }

    // Retorna o valor presente utilizando o base rate definido em application properties
    // Ou utilizando o valor default de 1%
    @Test
    void shouldUseConfiguredBaseRate() {
        DuplicataPricingStrategy configuredStrategy =
                new DuplicataPricingStrategy(new BigDecimal("0.0200"));

        BigDecimal presentValue = configuredStrategy.calculatePresentValue(new BigDecimal("1000.00"), 1);

        assertThat(roundToCents(presentValue)).isEqualByComparingTo(new BigDecimal("966.18"));
    }

    // C3: DUPLICATA, R$100.000, 3 meses, (USD - câmbio 5,4321) → US$17.094,67
    @Test
    void shouldReturnPresentValueFromGoldenCaseC3() {
        BigDecimal presentValueInBRL = strategy.calculatePresentValue(new BigDecimal("100000.00"), 3);
        BigDecimal presentValueInUSD = roundToCents(presentValueInBRL)
                .divide(USD_TO_BRL_RATE, MathContext.DECIMAL128);

        assertThat(roundToCents(presentValueInUSD)).isEqualByComparingTo(new BigDecimal("17094.67"));
    }

}
