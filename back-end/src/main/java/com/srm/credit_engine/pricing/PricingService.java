package com.srm.credit_engine.pricing;

import java.math.BigDecimal;

import com.srm.credit_engine.domain.enums.ReceivableType;

import org.springframework.stereotype.Service;

@Service
public class PricingService {

    private final PricingStrategy duplicataStrategy;
    private final PricingStrategy chequeStrategy;

    public PricingService(DuplicataPricingStrategy duplicataStrategy, ChequePricingStrategy chequeStrategy) {
        this.duplicataStrategy = duplicataStrategy;
        this.chequeStrategy = chequeStrategy;
    }

    public BigDecimal calculatePresentValue(BigDecimal faceValue, int termMonths, ReceivableType receivableType) {
        return switch (receivableType) {
            case DUPLICATA -> duplicataStrategy.calculatePresentValue(faceValue, termMonths);
            case CHEQUE -> chequeStrategy.calculatePresentValue(faceValue, termMonths);
        };
    }
}
