package com.srm.credit_engine.repository;

import java.time.Instant;
import java.util.Optional;

import com.srm.credit_engine.domain.entity.ExchangeRate;
import com.srm.credit_engine.domain.enums.CurrencyCode;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    @Query(value = """
            SELECT *
            FROM exchange_rates
            WHERE from_currency = :fromCurrency
              AND to_currency = :toCurrency
              AND effective_at <= :timestamp
            ORDER BY effective_at DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<ExchangeRate> findLatestValidRate(
            @Param("fromCurrency") CurrencyCode fromCurrency,
            @Param("toCurrency") CurrencyCode toCurrency,
            @Param("timestamp") Instant timestamp);
}
