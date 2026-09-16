package com.srm.credit_engine.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.srm.credit_engine.domain.enums.ReceivableType;

import com.srm.credit_engine.service.ExchangeRateService;
import com.srm.credit_engine.service.PricingService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class PricingServiceTest {

    // Adiciona o base rate padrão de 1% para os testes
    private final PricingService pricingService = new PricingService(
            new DuplicataPricingStrategy(new BigDecimal("0.0100")),
            new ChequePricingStrategy(new BigDecimal("0.0100")),
            mock(ExchangeRateService.class)
    );

    // Verifica se o resultado foi utilizado o strategy de Duplicata
    @Test
    void shouldUseDuplicataStrategyForDuplicataType() {
        BigDecimal result = pricingService.calculatePresentValue(
                new BigDecimal("100000.00"),
                3,
                ReceivableType.DUPLICATA);

        assertThat(result.setScale(2, RoundingMode.HALF_EVEN))
                .isEqualByComparingTo(new BigDecimal("92859.94"));
    }

    // Verifica se o resultado foi utilizado o strategy de Cheque
    @Test
    void shouldUseChequeStrategyForChequeType() {
        BigDecimal result = pricingService.calculatePresentValue(
                new BigDecimal("25000.00"),
                2,
                ReceivableType.CHEQUE);

        assertThat(result.setScale(2, RoundingMode.HALF_EVEN))
                .isEqualByComparingTo(new BigDecimal("23337.77"));
    }
}
