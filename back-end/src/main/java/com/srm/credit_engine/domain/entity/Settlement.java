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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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
@Table(name = "settlements")
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 64)
    private String idempotencyKey;

    @OneToOne
    @JoinColumn(name = "receivable_id", nullable = false, unique = true)
    private Receivable receivable;

    @Column(name = "present_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal presentValue;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal discount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_currency", nullable = false, length = 3)
    private CurrencyCode paymentCurrency;

    @Column(name = "exchange_rate", precision = 18, scale = 8)
    private BigDecimal exchangeRate;

    @Column(name = "settled_at", nullable = false, updatable = false)
    private Instant settledAt;

    public Settlement(
            Receivable receivable,
            BigDecimal presentValue,
            BigDecimal discount,
            CurrencyCode paymentCurrency,
            BigDecimal exchangeRate,
            String idempotencyKey) {
        this.receivable = receivable;
        this.presentValue = presentValue;
        this.discount = discount;
        this.paymentCurrency = paymentCurrency;
        this.exchangeRate = exchangeRate;
        this.idempotencyKey = idempotencyKey;
    }

    @PrePersist
    void prePersist() {
        this.settledAt = Instant.now();
    }
}
