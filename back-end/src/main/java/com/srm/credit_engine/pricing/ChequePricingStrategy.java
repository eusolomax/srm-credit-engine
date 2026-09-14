package com.srm.credit_engine.pricing;

import java.math.BigDecimal;
import java.math.MathContext;

import org.springframework.stereotype.Component;

@Component
public class ChequePricingStrategy implements PricingStrategy {

    private static final BigDecimal BASE_RATE = new BigDecimal("0.0100");
    private static final BigDecimal CHEQUE_SPREAD = new BigDecimal("0.0250");

    @Override
    public BigDecimal calculatePresentValue(BigDecimal faceValue, int termMonths) {
        // Fórmula base: Valor Presente = Valor de Face / (1 + Taxa Base + Spread)^Prazo

        // Soma 1 à taxa base e ao spread para obter o fator mensal de desconto.
        BigDecimal monthlyRate = BigDecimal.ONE.add(BASE_RATE).add(CHEQUE_SPREAD);

        // Calcula o fator de desconto acumulado pelo número de meses do prazo.
        BigDecimal discountFactor = monthlyRate.pow(termMonths);

        // Divide o valor de face pelo fator de desconto para obter e retornar o valor presente.
        return faceValue.divide(discountFactor, MathContext.DECIMAL128);
    }
}