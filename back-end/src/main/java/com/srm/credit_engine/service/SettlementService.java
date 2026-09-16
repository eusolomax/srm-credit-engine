package com.srm.credit_engine.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import com.srm.credit_engine.domain.entity.ExchangeRate;
import com.srm.credit_engine.domain.entity.Receivable;
import com.srm.credit_engine.domain.entity.Settlement;
import com.srm.credit_engine.domain.enums.CurrencyCode;
import com.srm.credit_engine.domain.enums.ReceivableStatus;
import com.srm.credit_engine.repository.SettlementRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettlementService {

    private final ReceivableService receivableService;
    private final PricingService pricingService;
    private final ExchangeRateService exchangeRateService;
    private final SettlementRepository settlementRepository;

    public SettlementService(
            ReceivableService receivableService,
            PricingService pricingService,
            ExchangeRateService exchangeRateService,
            SettlementRepository settlementRepository) {
        this.receivableService = receivableService;
        this.pricingService = pricingService;
        this.exchangeRateService = exchangeRateService;
        this.settlementRepository = settlementRepository;
    }

    @Transactional
    public Settlement settle(Long receivableId) {
        Receivable receivable = receivableService.findById(receivableId)
                .orElseThrow(() -> new IllegalArgumentException("Receivable not found: " + receivableId));

        if (receivable.getStatus() != ReceivableStatus.AVAILABLE) {
            throw new IllegalStateException("Receivable is not available for settlement");
        }

        if (receivable.getDueDate().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Receivable is overdue");
        }

        // Calcula o valor presente do recebível
        BigDecimal presentValueInBrl = pricingService.calculatePresentValue(
                receivable.getFaceValue(),
                receivable.getTermMonths(),
                receivable.getType());

        BigDecimal exchangeRateValue = null;
        BigDecimal finalPresentValue = presentValueInBrl;
        BigDecimal faceValueInPaymentCurrency = receivable.getFaceValue();
        Instant instantTimeStamp = Instant.now();

        // Caso a moeda de pagamento seja diferente do padrão BRL
        // Calcule a taxa de câmbio da moeda -> BRL
        if (receivable.getPaymentCurrency() != CurrencyCode.BRL) {
            ExchangeRate exchangeRate = exchangeRateService.findLatestValidRate(
                    receivable.getPaymentCurrency(),
                    CurrencyCode.BRL,
                    instantTimeStamp)
                    .orElseThrow(() -> new IllegalStateException(
                            String.format("No valid exchange rate for %s/BRL", receivable.getPaymentCurrency())));

            exchangeRateValue = exchangeRate.getRate();

            // Faz a conversão para a moeda estrangeira
            finalPresentValue = presentValueInBrl.divide(
                    exchangeRateValue,
                    MathContext.DECIMAL128);

            // Faz a conversão do valor de face para a moeda estrangeira
            // necessário para calcular o deságio
            faceValueInPaymentCurrency = receivable.getFaceValue()
                    .divide(exchangeRateValue, MathContext.DECIMAL128);
        }

        // O deságio é a diferença entre o valor de face e o valor presente
        BigDecimal discount = faceValueInPaymentCurrency
                .subtract(finalPresentValue)
                .setScale(2, RoundingMode.HALF_EVEN);


        BigDecimal roundedPresentValue = finalPresentValue.setScale(2, RoundingMode.HALF_EVEN);

        Settlement settlement = new Settlement(
                receivable,
                roundedPresentValue,
                discount,
                receivable.getPaymentCurrency(),
                exchangeRateValue);

        receivable.setStatus(ReceivableStatus.SETTLED);

        return settlementRepository.save(settlement);
    }

    public List<Settlement> findAll() {
        return settlementRepository.findAll();
    }
}
