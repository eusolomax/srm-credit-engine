package com.srm.credit_engine.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.srm.credit_engine.domain.entity.ExchangeRate;
import com.srm.credit_engine.domain.enums.CurrencyCode;
import com.srm.credit_engine.repository.ExchangeRateRepository;

import org.springframework.stereotype.Service;

@Service
public class ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;

    public ExchangeRateService(ExchangeRateRepository exchangeRateRepository) {
        this.exchangeRateRepository = exchangeRateRepository;
    }

    public ExchangeRate save(ExchangeRate exchangeRate) {
        if (exchangeRate.getFromCurrency() == exchangeRate.getToCurrency()) {
            throw new IllegalArgumentException("Source and target currencies must be different");
        }

        return exchangeRateRepository.save(exchangeRate);
    }

    public List<ExchangeRate> findAll() {
        return exchangeRateRepository.findAll();
    }

    public Optional<ExchangeRate> findLatestValidRate(
            CurrencyCode fromCurrency,
            CurrencyCode toCurrency,
            Instant timestamp) {
        return exchangeRateRepository.findLatestValidRate(fromCurrency.name(), toCurrency.name(), timestamp);
    }
}
