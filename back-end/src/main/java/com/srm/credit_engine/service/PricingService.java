package com.srm.credit_engine.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;

import com.srm.credit_engine.controller.dto.CreatePricingSimulationRequest;
import com.srm.credit_engine.controller.dto.PricingResult;
import com.srm.credit_engine.domain.entity.ExchangeRate;
import com.srm.credit_engine.domain.enums.CurrencyCode;
import com.srm.credit_engine.domain.enums.ReceivableType;

import com.srm.credit_engine.pricing.ChequePricingStrategy;
import com.srm.credit_engine.pricing.DuplicataPricingStrategy;
import com.srm.credit_engine.pricing.PricingStrategy;
import org.springframework.stereotype.Service;

@Service
public class PricingService {

    private final PricingStrategy duplicataStrategy;
    private final PricingStrategy chequeStrategy;
    private final ExchangeRateService exchangeRateService;

    public PricingService(DuplicataPricingStrategy duplicataStrategy, ChequePricingStrategy chequeStrategy, ExchangeRateService exchangeRateService) {
        this.duplicataStrategy = duplicataStrategy;
        this.chequeStrategy = chequeStrategy;
        this.exchangeRateService = exchangeRateService;
    }

    public BigDecimal calculatePresentValue(BigDecimal faceValue, int termMonths, ReceivableType receivableType) {
        return switch (receivableType) {
            case DUPLICATA -> duplicataStrategy.calculatePresentValue(faceValue, termMonths);
            case CHEQUE -> chequeStrategy.calculatePresentValue(faceValue, termMonths);
        };
    }

    public PricingResult calculatePricing(CreatePricingSimulationRequest request) {
        // Calcula o valor presente do recebível
        BigDecimal presentValueInBrl = calculatePresentValue(
                request.faceValue(),
                request.termMonths(),
                request.type());

        BigDecimal exchangeRateValue = null;
        BigDecimal finalPresentValue = presentValueInBrl;
        BigDecimal faceValueInPaymentCurrency = request.faceValue();
        Instant instantTimeStamp = Instant.now();

        // Caso a moeda de pagamento seja diferente do padrão BRL
        // Calcule a taxa de câmbio da moeda -> BRL
        if (request.paymentCurrency() != CurrencyCode.BRL) {
            ExchangeRate exchangeRate = exchangeRateService.findLatestValidRate(
                            request.paymentCurrency(),
                            CurrencyCode.BRL,
                            instantTimeStamp)
                    .orElseThrow(() -> new IllegalStateException(
                            String.format("No valid exchange rate for %s/BRL", request.paymentCurrency())));

            exchangeRateValue = exchangeRate.getRate();

            // Faz a conversão para a moeda estrangeira
            finalPresentValue = presentValueInBrl.divide(
                    exchangeRateValue,
                    MathContext.DECIMAL128);

            // Faz a conversão do valor de face para a moeda estrangeira
            // necessário para calcular o deságio
            faceValueInPaymentCurrency = request.faceValue()
                    .divide(exchangeRateValue, MathContext.DECIMAL128);
        }

        BigDecimal roundedFaceValue = faceValueInPaymentCurrency.setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal roundedPresentValue = finalPresentValue.setScale(2, RoundingMode.HALF_EVEN);

        // O deságio é a diferença entre o valor de face e o valor presente
        BigDecimal roundedDiscount = faceValueInPaymentCurrency
                .subtract(finalPresentValue)
                .setScale(2, RoundingMode.HALF_EVEN);

        return new PricingResult(
                roundedFaceValue,
                roundedPresentValue,
                roundedDiscount,
                request.paymentCurrency(),
                exchangeRateValue);
    }
}
