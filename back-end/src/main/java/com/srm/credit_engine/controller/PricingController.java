package com.srm.credit_engine.controller;

import com.srm.credit_engine.controller.dto.CreatePricingSimulationRequest;
import com.srm.credit_engine.controller.dto.PricingResult;
import com.srm.credit_engine.controller.dto.PricingSimulationResponse;
import com.srm.credit_engine.service.PricingService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pricing")
public class PricingController {

    private final PricingService pricingService;

    public PricingController(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    @PostMapping("/simulate")
    public ResponseEntity<PricingSimulationResponse> simulate(@Valid @RequestBody CreatePricingSimulationRequest request) {
        PricingResult pricingResult = pricingService.calculatePricing(request);

        return ResponseEntity.ok(new PricingSimulationResponse(
                pricingResult.roundedFaceValue(),
                pricingResult.roundedPresentValue(),
                pricingResult.roundedDiscount(),
                request.paymentCurrency()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ProblemDetail> handleIllegalState(IllegalStateException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }
}
