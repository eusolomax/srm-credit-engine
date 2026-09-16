package com.srm.credit_engine.controller;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.srm.credit_engine.controller.dto.CreateExchangeRateRequest;
import com.srm.credit_engine.controller.dto.ExchangeRateResponse;
import com.srm.credit_engine.domain.entity.ExchangeRate;
import com.srm.credit_engine.domain.enums.CurrencyCode;
import com.srm.credit_engine.mapper.ExchangeRateMapper;
import com.srm.credit_engine.service.ExchangeRateService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ExchangeRateControllerTest {

    private final ExchangeRateService service = mock(ExchangeRateService.class);
    private final ExchangeRateMapper mapper = mock(ExchangeRateMapper.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new ExchangeRateController(service, mapper))
                .setValidator(validator)
                .build();
    }

    // Garante que uma cotação válida seja cadastrada.
    @Test
    void shouldCreateExchangeRate() throws Exception {
        ExchangeRate exchangeRate = exchangeRate();
        ExchangeRateResponse response = response(exchangeRate);
        when(mapper.toEntity(any(CreateExchangeRateRequest.class))).thenReturn(exchangeRate);
        when(service.save(exchangeRate)).thenReturn(exchangeRate);
        when(mapper.toResponse(exchangeRate)).thenReturn(response);

        mockMvc.perform(post("/exchange-rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromCurrency": "USD",
                                  "toCurrency": "BRL",
                                  "rate": 5.4321,
                                  "effectiveAt": "2026-01-01T12:00:00Z"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fromCurrency").value("USD"))
                .andExpect(jsonPath("$.toCurrency").value("BRL"))
                .andExpect(jsonPath("$.rate").value(5.4321));

        verify(mapper).toEntity(any(CreateExchangeRateRequest.class));
        verify(service).save(exchangeRate);
        verify(mapper).toResponse(exchangeRate);
    }

    // Garante que campos obrigatórios sejam validados antes do service.
    @Test
    void shouldRejectMissingRequiredFields() throws Exception {
        mockMvc.perform(post("/exchange-rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service, mapper);
    }

    // Garante que uma taxa zero ou negativa seja rejeitada.
    @Test
    void shouldRejectNonPositiveRate() throws Exception {
        mockMvc.perform(post("/exchange-rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromCurrency": "USD",
                                  "toCurrency": "BRL",
                                  "rate": 0,
                                  "effectiveAt": "2026-01-01T12:00:00Z"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service, mapper);
    }

    // Garante que o endpoint devolva todas as cotações cadastradas.
    @Test
    void shouldReturnAllExchangeRates() throws Exception {
        ExchangeRate firstRate = exchangeRate();
        ExchangeRate secondRate = exchangeRate();
        List<ExchangeRate> exchangeRates = List.of(firstRate, secondRate);
        List<ExchangeRateResponse> responses = List.of(response(firstRate), response(secondRate));
        when(service.findAll()).thenReturn(exchangeRates);
        when(mapper.toResponse(exchangeRates)).thenReturn(responses);

        mockMvc.perform(get("/exchange-rates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(service).findAll();
        verify(mapper).toResponse(exchangeRates);
    }

    private ExchangeRate exchangeRate() {
        return new ExchangeRate(
                CurrencyCode.USD,
                CurrencyCode.BRL,
                new BigDecimal("5.4321"),
                Instant.parse("2026-01-01T12:00:00Z"));
    }

    private ExchangeRateResponse response(ExchangeRate exchangeRate) {
        return new ExchangeRateResponse(
                exchangeRate.getFromCurrency(),
                exchangeRate.getToCurrency(),
                exchangeRate.getRate(),
                exchangeRate.getEffectiveAt(),
                exchangeRate.getCreatedAt());
    }
}
