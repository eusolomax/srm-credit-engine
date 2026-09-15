package com.srm.credit_engine.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import com.srm.credit_engine.domain.entity.ExchangeRate;
import com.srm.credit_engine.domain.entity.Receivable;
import com.srm.credit_engine.domain.entity.Settlement;
import com.srm.credit_engine.domain.enums.CurrencyCode;
import com.srm.credit_engine.domain.enums.ReceivableStatus;
import com.srm.credit_engine.domain.enums.ReceivableType;
import com.srm.credit_engine.repository.SettlementRepository;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SettlementServiceTest {

    private final ReceivableService receivableService = mock(ReceivableService.class);
    private final PricingService pricingService = mock(PricingService.class);
    private final ExchangeRateService exchangeRateService = mock(ExchangeRateService.class);
    private final SettlementRepository settlementRepository = mock(SettlementRepository.class);

    private final SettlementService service = new SettlementService(
            receivableService,
            pricingService,
            exchangeRateService,
            settlementRepository);

    // C1: liquida uma duplicata em BRL com o valor presente esperado.
    @Test
    void shouldSettleDuplicataInBrl() {
        Receivable receivable = receivable(
                new BigDecimal("100000.00"),
                ReceivableType.DUPLICATA,
                CurrencyCode.BRL,
                3,
                LocalDate.now().plusMonths(3));
        stubAvailableReceivable(receivable);
        when(pricingService.calculatePresentValue(
                receivable.getFaceValue(), receivable.getTermMonths(), receivable.getType()))
                .thenReturn(new BigDecimal("92859.941"));

        Settlement result = service.settle(1L);

        assertThat(result.getPresentValue()).isEqualByComparingTo(new BigDecimal("92859.94"));
        assertThat(result.getDiscount()).isEqualByComparingTo(new BigDecimal("7140.06"));
        assertThat(result.getPaymentCurrency()).isEqualTo(CurrencyCode.BRL);
        assertThat(result.getExchangeRate()).isNull();
        assertThat(receivable.getStatus()).isEqualTo(ReceivableStatus.SETTLED);
        verify(settlementRepository).save(any(Settlement.class));
        verifyNoInteractions(exchangeRateService);
    }

    // C2: liquida um cheque em BRL com o valor presente esperado.
    @Test
    void shouldSettleChequeInBrl() {
        Receivable receivable = receivable(
                new BigDecimal("25000.00"),
                ReceivableType.CHEQUE,
                CurrencyCode.BRL,
                2,
                LocalDate.now().plusMonths(2));
        stubAvailableReceivable(receivable);
        when(pricingService.calculatePresentValue(
                receivable.getFaceValue(), receivable.getTermMonths(), receivable.getType()))
                .thenReturn(new BigDecimal("23337.768"));

        Settlement result = service.settle(1L);

        assertThat(result.getPresentValue()).isEqualByComparingTo(new BigDecimal("23337.77"));
        assertThat(result.getDiscount()).isEqualByComparingTo(new BigDecimal("1662.23"));
        assertThat(result.getPaymentCurrency()).isEqualTo(CurrencyCode.BRL);
        assertThat(result.getExchangeRate()).isNull();
        assertThat(receivable.getStatus()).isEqualTo(ReceivableStatus.SETTLED);
        verify(settlementRepository).save(any(Settlement.class));
        verifyNoInteractions(exchangeRateService);
    }

    // C3: converte o PV preciso de BRL para USD e arredonda somente no resultado final.
    @Test
    void shouldSettleDuplicataWithUsdConversion() {
        Receivable receivable = receivable(
                new BigDecimal("100000.00"),
                ReceivableType.DUPLICATA,
                CurrencyCode.USD,
                3,
                LocalDate.now().plusMonths(3));

        ExchangeRate exchangeRate = exchangeRate(
                CurrencyCode.USD,
                CurrencyCode.BRL, new BigDecimal("5.4321"),
                Instant.now());


        stubAvailableReceivable(receivable);

        when(pricingService.calculatePresentValue(
                receivable.getFaceValue(), receivable.getTermMonths(), receivable.getType()))
                .thenReturn(new BigDecimal("92859.941"));

        when(exchangeRateService.findLatestValidRate(
                eq(CurrencyCode.USD),
                eq(CurrencyCode.BRL),
                any(Instant.class)))
                .thenReturn(Optional.of(exchangeRate));

        Settlement result = service.settle(1L);

        assertThat(result.getPresentValue()).isEqualByComparingTo(new BigDecimal("17094.67"));
        assertThat(result.getDiscount()).isEqualByComparingTo(new BigDecimal("1314.42"));
        assertThat(result.getPaymentCurrency()).isEqualTo(CurrencyCode.USD);
        assertThat(result.getExchangeRate()).isEqualByComparingTo(new BigDecimal("5.4321"));
        assertThat(receivable.getStatus()).isEqualTo(ReceivableStatus.SETTLED);

        verify(exchangeRateService).findLatestValidRate(
                eq(CurrencyCode.USD),
                eq(CurrencyCode.BRL),
                any(Instant.class));

        verify(settlementRepository).save(any(Settlement.class));
    }

    // Garante que um recebível inexistente seja rejeitado antes do cálculo.
    @Test
    void shouldRejectWhenReceivableDoesNotExist() {
        when(receivableService.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.settle(1L))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(pricingService, exchangeRateService, settlementRepository);
    }

    // Garante que um recebível já liquidado não seja processado novamente.
    @Test
    void shouldRejectAlreadySettledReceivable() {
        Receivable receivable = receivable(
                new BigDecimal("100000.00"),
                ReceivableType.DUPLICATA,
                CurrencyCode.BRL,
                3,
                LocalDate.now().plusMonths(3));
        receivable.setStatus(ReceivableStatus.SETTLED);
        when(receivableService.findById(1L)).thenReturn(Optional.of(receivable));

        assertThatThrownBy(() -> service.settle(1L))
                .isInstanceOf(IllegalStateException.class);

        verifyNoInteractions(pricingService, exchangeRateService, settlementRepository);
    }

    // Garante que recebíveis vencidos não possam ser liquidados.
    @Test
    void shouldRejectOverdueReceivable() {
        Receivable receivable = receivable(
                new BigDecimal("100000.00"),
                ReceivableType.DUPLICATA,
                CurrencyCode.BRL,
                3,
                LocalDate.now().minusDays(1));
        when(receivableService.findById(1L)).thenReturn(Optional.of(receivable));

        assertThatThrownBy(() -> service.settle(1L))
                .isInstanceOf(IllegalStateException.class);

        verifyNoInteractions(pricingService, exchangeRateService, settlementRepository);
    }

    // Garante que a conversão seja rejeitada quando não houver cotação vigente.
    @Test
    void shouldRejectWhenExchangeRateDoesNotExist() {
        Receivable receivable = receivable(
                new BigDecimal("100000.00"),
                ReceivableType.DUPLICATA,
                CurrencyCode.USD,
                3,
                LocalDate.now().plusMonths(3));
        stubAvailableReceivable(receivable);
        when(pricingService.calculatePresentValue(
                receivable.getFaceValue(), receivable.getTermMonths(), receivable.getType()))
                .thenReturn(new BigDecimal("92859.941"));
        when(exchangeRateService.findLatestValidRate(
                eq(CurrencyCode.USD),
                eq(CurrencyCode.BRL),
                any(Instant.class)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.settle(1L))
                .isInstanceOf(IllegalStateException.class);

        assertThat(receivable.getStatus()).isEqualTo(ReceivableStatus.AVAILABLE);
        verify(exchangeRateService).findLatestValidRate(
                eq(CurrencyCode.USD),
                eq(CurrencyCode.BRL),
                any(Instant.class));
        verify(settlementRepository, org.mockito.Mockito.never()).save(any(Settlement.class));
    }

    private void stubAvailableReceivable(Receivable receivable) {
        when(receivableService.findById(1L)).thenReturn(Optional.of(receivable));
        when(settlementRepository.findByReceivable(receivable)).thenReturn(Optional.empty());
        when(settlementRepository.save(any(Settlement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private Receivable receivable(
            BigDecimal faceValue,
            ReceivableType type,
            CurrencyCode paymentCurrency,
            int termMonths,
            LocalDate dueDate) {
        return new Receivable(faceValue, type, paymentCurrency, termMonths, dueDate);
    }

    private ExchangeRate exchangeRate(
            CurrencyCode fromCurrency,
            CurrencyCode toCurrency,
            BigDecimal rate,
            Instant effectiveAt) {
        return new ExchangeRate(fromCurrency, toCurrency, rate, effectiveAt);
    }
}
