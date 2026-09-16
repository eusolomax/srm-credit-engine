package com.srm.credit_engine.controller;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.srm.credit_engine.controller.dto.SettlementResponse;
import com.srm.credit_engine.domain.entity.Receivable;
import com.srm.credit_engine.domain.entity.Settlement;
import com.srm.credit_engine.domain.enums.CurrencyCode;
import com.srm.credit_engine.domain.enums.ReceivableType;
import com.srm.credit_engine.mapper.SettlementMapper;
import com.srm.credit_engine.service.SettlementService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SettlementControllerTest {

    private final SettlementService service = mock(SettlementService.class);
    private final SettlementMapper mapper = mock(SettlementMapper.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new SettlementController(service, mapper))
                .setValidator(validator)
                .build();
    }

    // Garante que uma liquidação válida retorne 201 Created.
    @Test
    void shouldCreateSettlement() throws Exception {
        Settlement settlement = settlement(1L);
        SettlementResponse response = response(settlement);
        when(service.settle(1L, "settlement-key")).thenReturn(settlement);
        when(mapper.toResponse(settlement)).thenReturn(response);

        mockMvc.perform(post("/settlements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "receivableId": 1,
                                  "idempotencyKey": "settlement-key"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.receivableId").value(1))
                .andExpect(jsonPath("$.assignor").value("12345678901"))
                .andExpect(jsonPath("$.presentValue").value(92859.94))
                .andExpect(jsonPath("$.paymentCurrency").value("BRL"));

        verify(service).settle(1L, "settlement-key");
        verify(mapper).toResponse(settlement);
    }

    // Garante que o identificador do recebível seja obrigatório.
    @Test
    void shouldRejectInvalidRequest() throws Exception {
        mockMvc.perform(post("/settlements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service, mapper);
    }

    // Garante que um recebível inexistente retorne 404 Not Found.
    @Test
    void shouldReturnNotFoundWhenReceivableDoesNotExist() throws Exception {
        when(service.settle(1L, "settlement-key"))
                .thenThrow(new IllegalArgumentException());

        mockMvc.perform(post("/settlements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"receivableId\":1,\"idempotencyKey\":\"settlement-key\"}"))
                .andExpect(status().isNotFound());

        verify(service).settle(1L, "settlement-key");
        verifyNoInteractions(mapper);
    }

    // Garante que um recebível vencido ou já liquidado retorne 409 Conflict.
    @Test
    void shouldReturnConflictWhenReceivableIsOverdueOrNotAvailableForSettlement() throws Exception {
        when(service.settle(1L, "settlement-key"))
                .thenThrow(new IllegalStateException());

        mockMvc.perform(post("/settlements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"receivableId\":1,\"idempotencyKey\":\"settlement-key\"}"))
                .andExpect(status().isConflict());

        verify(service).settle(1L, "settlement-key");
        verifyNoInteractions(mapper);
    }

    private Settlement settlement(Long receivableId) {
        Receivable receivable = new Receivable(
                new BigDecimal("100000.00"),
                "12345678901",
                ReceivableType.DUPLICATA,
                CurrencyCode.BRL,
                3,
                LocalDate.now().plusMonths(3));
        receivable.setId(receivableId);

        return new Settlement(
                receivable,
                new BigDecimal("92859.94"),
                new BigDecimal("7140.06"),
                CurrencyCode.BRL,
                null,
                "settlement-key");
    }

    private SettlementResponse response(Settlement settlement) {
        return new SettlementResponse(
                settlement.getReceivable().getId(),
                settlement.getReceivable().getAssignor(),
                settlement.getPresentValue(),
                settlement.getDiscount(),
                settlement.getPaymentCurrency(),
                settlement.getExchangeRate(),
                settlement.getSettledAt());
    }
}
