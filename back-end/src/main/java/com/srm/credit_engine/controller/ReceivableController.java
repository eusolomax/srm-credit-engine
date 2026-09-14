package com.srm.credit_engine.controller;

import java.util.Optional;

import com.srm.credit_engine.controller.dto.CreateReceivableRequest;
import com.srm.credit_engine.controller.dto.ReceivableResponse;
import com.srm.credit_engine.domain.entity.Receivable;
import com.srm.credit_engine.mapper.ReceivableMapper;
import com.srm.credit_engine.service.ReceivableService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/receivables")
public class ReceivableController {

    private final ReceivableService receivableService;
    private final ReceivableMapper mapper;

    public ReceivableController(ReceivableService receivableService, ReceivableMapper receivableMapper) {
        this.receivableService = receivableService;
        this.mapper = receivableMapper;
    }

    @PostMapping
    public ResponseEntity<ReceivableResponse> create(@Valid @RequestBody CreateReceivableRequest request) {
        Receivable receivable = mapper.toEntity(request);

        Receivable savedReceivable = receivableService.save(receivable);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(savedReceivable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReceivableResponse> findById(@PathVariable Long id) {
        Optional<Receivable> receivable = receivableService.findById(id);

        return receivable
                .map(value -> ResponseEntity.ok(mapper.toResponse(value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
