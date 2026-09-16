package com.srm.credit_engine.service;
import java.time.LocalDate;
import java.util.List;

import com.srm.credit_engine.controller.dto.CreatePricingSimulationRequest;
import com.srm.credit_engine.controller.dto.PricingResult;
import com.srm.credit_engine.domain.entity.Receivable;
import com.srm.credit_engine.domain.entity.Settlement;
import com.srm.credit_engine.domain.enums.ReceivableStatus;
import com.srm.credit_engine.repository.SettlementRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettlementService {

    private final ReceivableService receivableService;
    private final PricingService pricingService;
    private final SettlementRepository settlementRepository;

    public SettlementService(
            ReceivableService receivableService,
            PricingService pricingService,
            SettlementRepository settlementRepository) {
        this.receivableService = receivableService;
        this.pricingService = pricingService;
        this.settlementRepository = settlementRepository;
    }

    @Transactional
    public Settlement settle(Long receivableId, String idempotencyKey) {
        // Verificação de idempotência
        Settlement existingSettlement = settlementRepository.findByIdempotencyKey(idempotencyKey).orElse(null);

        if (existingSettlement != null) {
            return existingSettlement;
        }

        Receivable receivable = receivableService.findById(receivableId)
                .orElseThrow(() -> new IllegalArgumentException("Receivable not found: " + receivableId));

        if (receivable.getStatus() != ReceivableStatus.AVAILABLE) {
            throw new IllegalStateException("Receivable is not available for settlement");
        }

        if (receivable.getDueDate().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Receivable is overdue");
        }

        CreatePricingSimulationRequest pricingSimulation = new CreatePricingSimulationRequest(
                receivable.getFaceValue(),
                receivable.getType(),
                receivable.getPaymentCurrency(),
                receivable.getTermMonths()
        );

        PricingResult pricingResult = pricingService.calculatePricing(pricingSimulation);

        Settlement settlement = new Settlement(
                receivable,
                pricingResult.roundedPresentValue(),
                pricingResult.roundedDiscount(),
                pricingResult.paymentCurrency(),
                pricingResult.exchangeRateValue(),
                idempotencyKey);

        receivable.setStatus(ReceivableStatus.SETTLED);

        try {
            return settlementRepository.saveAndFlush(settlement);
        } catch (DataIntegrityViolationException exception) {
            return settlementRepository.findByIdempotencyKey(idempotencyKey)
                    .orElseThrow(() -> exception);
        }
    }

    public List<Settlement> findAll() {
        return settlementRepository.findAll();
    }
}
