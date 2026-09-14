package com.srm.credit_engine.repository;

import com.srm.credit_engine.domain.entity.Receivable;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceivableRepository extends JpaRepository<Receivable, Long> {
}
