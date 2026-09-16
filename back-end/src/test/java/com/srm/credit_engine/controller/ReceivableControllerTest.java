package com.srm.credit_engine.controller;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.math.BigDecimal;

import com.srm.credit_engine.controller.dto.CreateReceivableRequest;
import com.srm.credit_engine.controller.dto.ReceivableResponse;
import com.srm.credit_engine.domain.entity.Receivable;
import com.srm.credit_engine.domain.enums.CurrencyCode;
import com.srm.credit_engine.domain.enums.ReceivableStatus;
import com.srm.credit_engine.domain.enums.ReceivableType;
import com.srm.credit_engine.mapper.ReceivableMapper;
import com.srm.credit_engine.service.ReceivableService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReceivableControllerTest {

    private final ReceivableService service = mock(ReceivableService.class);
    private final ReceivableMapper mapper = mock(ReceivableMapper.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new ReceivableController(service, mapper))
                .setValidator(validator)
                .build();
    }

    // Garante que um recebível válido seja criado com status AVAILABLE.
    @Test
    void shouldCreateReceivable() throws Exception {
        Receivable receivable = createReceivable();
        Receivable savedReceivable = createReceivable();
        savedReceivable.setCreatedAt(Instant.parse("2026-01-01T21:00:00Z"));
        ReceivableResponse response = createResponse(savedReceivable);
        when(mapper.toEntity(any(CreateReceivableRequest.class))).thenReturn(receivable);
        when(service.save(receivable)).thenReturn(savedReceivable);
        when(mapper.toResponse(savedReceivable)).thenReturn(response);

        mockMvc.perform(post("/receivables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "faceValue": 100000.00,
                                  "type": "DUPLICATA",
                                  "paymentCurrency": "BRL",
                                  "termMonths": 3,
                                  "dueDate": "2026-12-14"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("AVAILABLE"));

        var receivableCaptor = forClass(Receivable.class);
        verify(service).save(receivableCaptor.capture());
        assertThat(receivableCaptor.getValue().getStatus()).isEqualTo(ReceivableStatus.AVAILABLE);
        verify(mapper).toEntity(any(CreateReceivableRequest.class));
        verify(mapper).toResponse(savedReceivable);
    }

    // Garante que valores financeiros e prazo inválidos sejam rejeitados.
    @Test
    void shouldRejectInvalidPayload() throws Exception {
        mockMvc.perform(post("/receivables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "faceValue": 0,
                                  "type": "DUPLICATA",
                                  "paymentCurrency": "BRL",
                                  "termMonths": 0,
                                  "dueDate": "2026-12-14"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    // Garante que os campos obrigatórios sejam validados antes do service.
    @Test
    void shouldRejectMissingRequiredFields() throws Exception {
        mockMvc.perform(post("/receivables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    // Garante que a consulta retorne o recebível quando ele existir.
    @Test
    void shouldReturnExistingReceivable() throws Exception {
        Receivable receivable = createReceivable();
        receivable.setId(1L);
        receivable.setCreatedAt(Instant.parse("2026-01-01T21:00:00Z"));
        when(service.findById(1L)).thenReturn(Optional.of(receivable));
        when(mapper.toResponse(receivable)).thenReturn(createResponse(receivable));

        mockMvc.perform(get("/receivables/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("DUPLICATA"))
                .andExpect(jsonPath("$.paymentCurrency").value("BRL"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));

        verify(service).findById(eq(1L));
        verify(mapper).toResponse(receivable);
    }

    // Garante que a consulta retorne 404 quando o recebível não existir.
    @Test
    void shouldReturnNotFoundWhenReceivableDoesNotExist() throws Exception {
        when(service.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/receivables/1"))
                .andExpect(status().isNotFound());

        verify(service).findById(eq(1L));
    }

    private Receivable createReceivable() {
        return new Receivable(
                new BigDecimal("100000.00"),
                ReceivableType.DUPLICATA,
                CurrencyCode.BRL,
                3,
                LocalDate.of(2026, 12, 14));
    }

    private ReceivableResponse createResponse(Receivable receivable) {
        return new ReceivableResponse(
                receivable.getFaceValue(),
                receivable.getType(),
                receivable.getPaymentCurrency(),
                receivable.getTermMonths(),
                receivable.getDueDate(),
                receivable.getStatus(),
                receivable.getCreatedAt());
    }
}
