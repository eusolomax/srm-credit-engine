package com.srm.credit_engine.controller;

import java.util.List;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import com.srm.credit_engine.controller.dto.CreateSettlementRequest;
import com.srm.credit_engine.controller.dto.SettlementResponse;
import com.srm.credit_engine.domain.entity.Settlement;
import com.srm.credit_engine.domain.enums.CurrencyCode;
import com.srm.credit_engine.mapper.SettlementMapper;
import com.srm.credit_engine.service.SettlementService;

import jakarta.validation.Valid;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
        Settlement settlement = settlementService.settle(
                request.receivableId(),
                request.idempotencyKey());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapper.toResponse(settlement));
    }

    @GetMapping
    public ResponseEntity<List<SettlementResponse>> findAll(
            @RequestParam(required = false) String assignor,
            @RequestParam(required = false) CurrencyCode currency,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        // Valida se existe filtro de periodo
        // E se o periodo "from" é depois do periodo "to"
        if (from != null && to != null && from.isAfter(to)) {
            return ResponseEntity.badRequest().build();
        }

        List<Settlement> settlements;

        if (assignor == null && currency == null && from == null && to == null) {
            // Se não existe filtro, retorna todos
            settlements = settlementService.findAll();
        } else {
            // Se existe algum filtro, envia pro serviço correto
            // (Se existe filtro de periodo - converte LocalDate -> Instant)
            Instant fromInclusive = from == null ? null : from.atStartOfDay(ZoneOffset.UTC).toInstant();
            Instant toExclusive = to == null ? null : to.atStartOfDay(ZoneOffset.UTC).toInstant();

            settlements = settlementService.findByFilters(
                    assignor,
                    currency,
                    fromInclusive,
                    toExclusive);
        }

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

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrityViolation() {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unable to complete settlement");

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(problem);
    }
}
