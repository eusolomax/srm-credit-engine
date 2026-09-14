package com.srm.credit_engine.pricing;

import java.math.BigDecimal;
import java.math.MathContext;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DuplicataPricingStrategy implements PricingStrategy {

    private static final BigDecimal DUPLICATA_SPREAD = new BigDecimal("0.0150");
    private final BigDecimal baseRate;

    public DuplicataPricingStrategy(@Value("${pricing.base-rate:0.0100}") BigDecimal baseRate) {
        this.baseRate = baseRate;
    }

    @Override
    public BigDecimal calculatePresentValue(BigDecimal faceValue, int termMonths) {
        // Fórmula base: Valor Presente = Valor de Face / (1 + Taxa Base + Spread)^Prazo

        // Soma 1 à taxa base e ao spread para obter o fator mensal de desconto.
        BigDecimal monthlyRate = BigDecimal.ONE.add(baseRate).add(DUPLICATA_SPREAD);

        // Calcula o fator de desconto acumulado pelo número de meses do prazo.
        BigDecimal discountFactor = monthlyRate.pow(termMonths);

        // Divide o valor de face pelo fator de desconto para obter e retornar o valor presente.
        return faceValue.divide(discountFactor, MathContext.DECIMAL128);
    }
}
