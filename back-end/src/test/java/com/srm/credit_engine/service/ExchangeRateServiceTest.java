package com.srm.credit_engine.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.srm.credit_engine.domain.entity.ExchangeRate;
import com.srm.credit_engine.domain.enums.CurrencyCode;
import com.srm.credit_engine.repository.ExchangeRateRepository;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExchangeRateServiceTest {

    private final ExchangeRateRepository repository = mock(ExchangeRateRepository.class);
    private final ExchangeRateService service = new ExchangeRateService(repository);

    // Garante que uma nova cotação seja encaminhada ao repositório e retornada.
    @Test
    void shouldSaveExchangeRate() {
        ExchangeRate exchangeRate = new ExchangeRate(
                CurrencyCode.USD,
                CurrencyCode.BRL,
                new BigDecimal("5.4321"),
                Instant.parse("2026-01-01T12:00:00Z"));

        when(repository.save(exchangeRate)).thenReturn(exchangeRate);

        ExchangeRate result = service.save(exchangeRate);

        assertThat(result).isSameAs(exchangeRate);
        verify(repository).save(exchangeRate);
    }

    // Garante que o serviço devolva todas as cotações armazenadas.
    @Test
    void shouldFindAllExchangeRates() {
        List<ExchangeRate> exchangeRates = List.of(mock(ExchangeRate.class));
        when(repository.findAll()).thenReturn(exchangeRates);

        List<ExchangeRate> result = service.findAll();

        assertThat(result).isSameAs(exchangeRates);
        verify(repository).findAll();
    }

    // Garante que a taxa mais recente válida seja buscada para o par informado.
    @Test
    void shouldFindLatestValidRateForCurrencyPair() {
        Instant timestamp = Instant.parse("2026-01-01T12:00:00Z");
        ExchangeRate exchangeRate = mock(ExchangeRate.class);
        when(repository.findLatestValidRate(CurrencyCode.USD.toString(), CurrencyCode.BRL.toString(), timestamp))
                .thenReturn(Optional.of(exchangeRate));

        Optional<ExchangeRate> result = service.findLatestValidRate(
                CurrencyCode.USD, CurrencyCode.BRL, timestamp);

        assertThat(result).containsSame(exchangeRate);
        verify(repository).findLatestValidRate(CurrencyCode.USD.toString(), CurrencyCode.BRL.toString(), timestamp);
    }

    // Garante que a busca considere apenas cotações vigentes até o instante informado.
    @Test
    void shouldNotConsiderRateAfterRequestedTimestamp() {
        Instant timestamp = Instant.parse("2026-01-01T12:00:00Z");
        when(repository.findLatestValidRate(CurrencyCode.USD.toString(), CurrencyCode.BRL.toString(), timestamp))
                .thenReturn(Optional.empty());

        Optional<ExchangeRate> result = service.findLatestValidRate(
                CurrencyCode.USD, CurrencyCode.BRL, timestamp);

        assertThat(result).isEmpty();
        verify(repository).findLatestValidRate(CurrencyCode.USD.toString(), CurrencyCode.BRL.toString(), timestamp);
    }

    // Garante que a busca retorne vazio quando não houver cotação válida para o par.
    @Test
    void shouldReturnEmptyWhenThereIsNoValidRateForCurrencyPair() {
        Instant timestamp = Instant.parse("2026-01-01T12:00:00Z");
        when(repository.findLatestValidRate(CurrencyCode.BRL.toString(), CurrencyCode.USD.toString(), timestamp))
                .thenReturn(Optional.empty());

        Optional<ExchangeRate> result = service.findLatestValidRate(
                CurrencyCode.BRL, CurrencyCode.USD, timestamp);

        assertThat(result).isEmpty();
        verify(repository).findLatestValidRate(CurrencyCode.BRL.toString(), CurrencyCode.USD.toString(), timestamp);
    }
}
