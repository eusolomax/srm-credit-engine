package com.srm.credit_engine.pricing;

import java.math.BigDecimal;

public interface PricingStrategy {

    BigDecimal calculatePresentValue(BigDecimal faceValue, int termMonths);
}