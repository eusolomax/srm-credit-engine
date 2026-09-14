package com.srm.credit_engine.domain.entity;

import java.math.BigDecimal;
import java.time.Instant;

import com.srm.credit_engine.domain.enums.CurrencyCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "exchange_rates")
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_currency", nullable = false, length = 3)
    private CurrencyCode fromCurrency;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_currency", nullable = false, length = 3)
    private CurrencyCode toCurrency;

    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal rate;

    @Column(name = "effective_at", nullable = false)
    private Instant effectiveAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
    }
}