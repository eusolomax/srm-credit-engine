package com.srm.credit_engine.domain.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.srm.credit_engine.domain.enums.CurrencyCode;
import com.srm.credit_engine.domain.enums.ReceivableStatus;
import com.srm.credit_engine.domain.enums.ReceivableType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "receivables")
public class Receivable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "face_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal faceValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReceivableType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_currency", nullable = false, length = 3)
    private CurrencyCode paymentCurrency;

    @Column(name = "term_months", nullable = false)
    private Integer termMonths;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReceivableStatus status = ReceivableStatus.AVAILABLE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToOne(mappedBy = "receivable")
    private Settlement settlement;

    public Receivable(
            BigDecimal faceValue,
            ReceivableType type,
            CurrencyCode paymentCurrency,
            Integer termMonths,
            LocalDate dueDate) {
        this.faceValue = faceValue;
        this.type = type;
        this.paymentCurrency = paymentCurrency;
        this.termMonths = termMonths;
        this.dueDate = dueDate;
    }

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
    }
}
