package com.srm.credit_engine.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.srm.credit_engine.domain.entity.Receivable;
import com.srm.credit_engine.domain.entity.Settlement;
import com.srm.credit_engine.domain.enums.CurrencyCode;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    Optional<Settlement> findByReceivable(Receivable receivable);

    Optional<Settlement> findByIdempotencyKey(String idempotencyKey);

    @Query("""
            SELECT settlement
            FROM Settlement settlement
            JOIN settlement.receivable receivable
            WHERE (:assignor IS NULL OR receivable.assignor = :assignor)
              AND (:currency IS NULL OR settlement.paymentCurrency = :currency)
              AND (:fromInclusive IS NULL OR settlement.settledAt >= :fromInclusive)
              AND (:toExclusive IS NULL OR settlement.settledAt <= :toExclusive)
            """)
    List<Settlement> findByFilters(
            @Param("assignor") String assignor,
            @Param("currency") CurrencyCode currency,
            @Param("fromInclusive") Instant fromInclusive,
            @Param("toExclusive") Instant toExclusive);
}
