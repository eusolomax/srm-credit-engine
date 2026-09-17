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

        BigDecimal roundedPresentValueInBrl = presentValueInBrl
                .setScale(2, RoundingMode.HALF_EVEN);

        BigDecimal exchangeRateValue = null;
        BigDecimal finalPresentValue = presentValueInBrl;

        // Caso a moeda de pagamento seja diferente do padrão BRL
        // Calcule a taxa de câmbio da moeda -> BRL
        if (request.paymentCurrency() != CurrencyCode.BRL) {
            ExchangeRate exchangeRate = exchangeRateService.findLatestValidRate(
                            request.paymentCurrency(),
                            CurrencyCode.BRL,
                            Instant.now())
                    .orElseThrow(() -> new IllegalStateException(
                            String.format("No valid exchange rate for %s/BRL", request.paymentCurrency())));

            exchangeRateValue = exchangeRate.getRate();

            // Converte valor presente para a moeda estrangeira
            finalPresentValue = roundedPresentValueInBrl
                    .divide(exchangeRateValue, MathContext.DECIMAL128);
        }

        //Arredondamentos finais:
        BigDecimal roundedFaceValue = request.faceValue()
                .setScale(2, RoundingMode.HALF_EVEN);

        BigDecimal roundedFinalPresentValue = finalPresentValue
                .setScale(2, RoundingMode.HALF_EVEN);

        BigDecimal roundedDiscount = request.faceValue()
                .subtract(roundedPresentValueInBrl)
                .setScale(2, RoundingMode.HALF_EVEN);

        return new PricingResult(
                roundedFaceValue,
                roundedFinalPresentValue,
                roundedDiscount,
                request.paymentCurrency(),
                exchangeRateValue);
    }
}
