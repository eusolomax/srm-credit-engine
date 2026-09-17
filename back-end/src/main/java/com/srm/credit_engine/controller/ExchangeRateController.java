package com.srm.credit_engine.controller;

import java.util.List;

import com.srm.credit_engine.controller.dto.CreateExchangeRateRequest;
import com.srm.credit_engine.controller.dto.ExchangeRateResponse;
import com.srm.credit_engine.domain.entity.ExchangeRate;
import com.srm.credit_engine.mapper.ExchangeRateMapper;
import com.srm.credit_engine.service.ExchangeRateService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/exchange-rates")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;
    private final ExchangeRateMapper mapper;

    public ExchangeRateController(ExchangeRateService exchangeRateService, ExchangeRateMapper mapper) {
        this.exchangeRateService = exchangeRateService;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<ExchangeRateResponse> create(@Valid @RequestBody CreateExchangeRateRequest request) {
        ExchangeRate exchangeRate = mapper.toEntity(request);
        ExchangeRate savedExchangeRate = exchangeRateService.save(exchangeRate);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(savedExchangeRate));
    }

    @GetMapping
    public ResponseEntity<List<ExchangeRateResponse>> findAll() {
        List<ExchangeRate> exchangeRates = exchangeRateService.findAll();

        return ResponseEntity.ok(mapper.toResponse(exchangeRates));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(problem);
    }
}
