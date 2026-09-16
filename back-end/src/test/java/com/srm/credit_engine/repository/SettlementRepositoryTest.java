package com.srm.credit_engine.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.srm.credit_engine.domain.entity.Receivable;
import com.srm.credit_engine.domain.entity.Settlement;
import com.srm.credit_engine.domain.enums.CurrencyCode;
import com.srm.credit_engine.domain.enums.ReceivableType;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class SettlementRepositoryTest {

    @Autowired
    private SettlementRepository settlementRepository;

    @Autowired
    private ReceivableRepository receivableRepository;

    @Test
    void shouldRejectDuplicateIdempotencyKey() {
        Receivable firstReceivable = receivable();
        Receivable secondReceivable = receivable();
        receivableRepository.saveAllAndFlush(java.util.List.of(firstReceivable, secondReceivable));

        settlementRepository.saveAndFlush(settlement(firstReceivable, "same-key"));

        assertThatThrownBy(() -> settlementRepository.saveAndFlush(
                settlement(secondReceivable, "same-key")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldApplyAssignorCurrencyAndPeriodFiltersTogether() {
        Receivable matchingReceivable = receivable("12345678901");
        Receivable wrongCurrencyReceivable = receivable("12345678901");
        Receivable wrongAssignorReceivable = receivable("98765432109");
        receivableRepository.saveAllAndFlush(java.util.List.of(
                matchingReceivable,
                wrongCurrencyReceivable,
                wrongAssignorReceivable));

        Settlement matchingSettlement = settlement(
                matchingReceivable,
                "matching-combined-key",
                CurrencyCode.BRL);
        Settlement wrongCurrencySettlement = settlement(
                wrongCurrencyReceivable,
                "wrong-currency-key",
                CurrencyCode.USD);
        Settlement wrongAssignorSettlement = settlement(
                wrongAssignorReceivable,
                "wrong-assignor-key",
                CurrencyCode.BRL);
        settlementRepository.saveAllAndFlush(java.util.List.of(
                matchingSettlement,
                wrongCurrencySettlement,
                wrongAssignorSettlement));

        matchingSettlement.setSettledAt(Instant.parse("2026-09-15T12:00:00Z"));
        wrongCurrencySettlement.setSettledAt(Instant.parse("2026-09-15T12:00:00Z"));
        wrongAssignorSettlement.setSettledAt(Instant.parse("2026-09-15T12:00:00Z"));
        settlementRepository.saveAllAndFlush(java.util.List.of(
                matchingSettlement,
                wrongCurrencySettlement,
                wrongAssignorSettlement));

        assertThat(settlementRepository.findByFilters(
                "12345678901",
                CurrencyCode.BRL,
                Instant.parse("2026-09-01T00:00:00Z"),
                Instant.parse("2026-10-01T00:00:00Z")))
                .hasSize(1)
                .first()
                .extracting(Settlement::getReceivable)
                .isSameAs(matchingReceivable);
    }

    private Receivable receivable() {
        return receivable("12345678901");
    }

    private Receivable receivable(String assignor) {
        return new Receivable(
                new BigDecimal("100000.00"),
                assignor,
                ReceivableType.DUPLICATA,
                CurrencyCode.BRL,
                3,
                LocalDate.now().plusMonths(3));
    }

    private Settlement settlement(Receivable receivable, String idempotencyKey) {
        return settlement(receivable, idempotencyKey, CurrencyCode.BRL);
    }

    private Settlement settlement(
            Receivable receivable,
            String idempotencyKey,
            CurrencyCode paymentCurrency) {
        return new Settlement(
                receivable,
                new BigDecimal("92859.94"),
                new BigDecimal("7140.06"),
                paymentCurrency,
                null,
                idempotencyKey);
    }
}
