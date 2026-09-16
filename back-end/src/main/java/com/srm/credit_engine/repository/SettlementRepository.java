package com.srm.credit_engine.repository;

import java.util.Optional;

import com.srm.credit_engine.domain.entity.Receivable;
import com.srm.credit_engine.domain.entity.Settlement;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    Optional<Settlement> findByReceivable(Receivable receivable);

    Optional<Settlement> findByIdempotencyKey(String idempotencyKey);
}
