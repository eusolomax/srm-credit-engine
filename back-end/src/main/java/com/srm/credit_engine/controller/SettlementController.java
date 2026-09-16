package com.srm.credit_engine.controller;

import java.util.List;

import com.srm.credit_engine.controller.dto.CreateSettlementRequest;
import com.srm.credit_engine.controller.dto.SettlementResponse;
import com.srm.credit_engine.domain.entity.Settlement;
import com.srm.credit_engine.mapper.SettlementMapper;
import com.srm.credit_engine.service.SettlementService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/settlements")
public class SettlementController {

    private final SettlementService settlementService;
    private final SettlementMapper mapper;

    public SettlementController(SettlementService settlementService, SettlementMapper mapper) {
        this.settlementService = settlementService;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<SettlementResponse> settle(@Valid @RequestBody CreateSettlementRequest request) {
        Settlement settlement = settlementService.settle(request.receivableId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapper.toResponse(settlement));
    }

    @GetMapping
    public ResponseEntity<List<SettlementResponse>> findAll() {
        List<Settlement> settlements = settlementService.findAll();

        return ResponseEntity.ok(mapper.toResponse(settlements));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(problem);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ProblemDetail> handleIllegalState(IllegalStateException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(problem);
    }
}
