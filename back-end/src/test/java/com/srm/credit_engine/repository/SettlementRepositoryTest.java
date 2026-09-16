package com.srm.credit_engine.repository;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.srm.credit_engine.domain.entity.Receivable;
import com.srm.credit_engine.domain.entity.Settlement;
import com.srm.credit_engine.domain.enums.CurrencyCode;
import com.srm.credit_engine.domain.enums.ReceivableType;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

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

    private Receivable receivable() {
        return new Receivable(
                new BigDecimal("100000.00"),
                "12345678901",
                ReceivableType.DUPLICATA,
                CurrencyCode.BRL,
                3,
                LocalDate.now().plusMonths(3));
    }

    private Settlement settlement(Receivable receivable, String idempotencyKey) {
        return new Settlement(
                receivable,
                new BigDecimal("92859.94"),
                new BigDecimal("7140.06"),
                CurrencyCode.BRL,
                null,
                idempotencyKey);
    }
}
