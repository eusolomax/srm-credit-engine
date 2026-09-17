package com.srm.credit_engine.service;

import java.util.List;
import java.util.Optional;

import com.srm.credit_engine.domain.entity.Receivable;
import com.srm.credit_engine.repository.ReceivableRepository;

import org.springframework.stereotype.Service;

@Service
public class ReceivableService {

    private final ReceivableRepository receivableRepository;

    public ReceivableService(ReceivableRepository receivableRepository) {
        this.receivableRepository = receivableRepository;
    }

    public Receivable save(Receivable receivable) {
        return receivableRepository.save(receivable);
    }

    public Optional<Receivable> findById(Long id) {
        return receivableRepository.findById(id);
    }

    public List<Receivable> findAll() {
        return receivableRepository.findAll();
    }
}
