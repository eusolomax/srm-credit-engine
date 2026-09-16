package com.srm.credit_engine.controller;

import java.math.BigDecimal;

import com.srm.credit_engine.controller.dto.CreatePricingSimulationRequest;
import com.srm.credit_engine.controller.dto.PricingResult;
import com.srm.credit_engine.domain.enums.CurrencyCode;
import com.srm.credit_engine.service.PricingService;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PricingControllerTest {

    private final PricingService pricingService = mock(PricingService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new PricingController(pricingService))
                .setValidator(validator)
                .build();
    }

    // Garante que uma duplicata em BRL seja simulada sem consultar câmbio.
    @Test
    void shouldSimulateDuplicataInBrl() throws Exception {
        when(pricingService.calculatePricing(any(CreatePricingSimulationRequest.class)))
                .thenReturn(new PricingResult(
                        new BigDecimal("100000.00"),
                        new BigDecimal("92859.94"),
                        new BigDecimal("7140.06"),
                        CurrencyCode.BRL,
                        null));

        mockMvc.perform(post("/pricing/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "faceValue": 100000.00,
                                  "type": "DUPLICATA",
                                  "paymentCurrency": "BRL",
                                  "termMonths": 3
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.faceValue").value(100000.00))
                .andExpect(jsonPath("$.presentValue").value(92859.94))
                .andExpect(jsonPath("$.discount").value(7140.06))
                .andExpect(jsonPath("$.paymentCurrency").value("BRL"));

        verify(pricingService).calculatePricing(any(CreatePricingSimulationRequest.class));
    }

    // Garante que um cheque em BRL use a estratégia correta.
    @Test
    void shouldSimulateChequeInBrl() throws Exception {
        when(pricingService.calculatePricing(any(CreatePricingSimulationRequest.class)))
                .thenReturn(new PricingResult(
                        new BigDecimal("25000.00"),
                        new BigDecimal("23337.77"),
                        new BigDecimal("1662.23"),
                        CurrencyCode.BRL,
                        null));

        mockMvc.perform(post("/pricing/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "faceValue": 25000.00,
                                  "type": "CHEQUE",
                                  "paymentCurrency": "BRL",
                                  "termMonths": 2
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.faceValue").value(25000.00))
                .andExpect(jsonPath("$.presentValue").value(23337.77))
                .andExpect(jsonPath("$.discount").value(1662.23))
                .andExpect(jsonPath("$.paymentCurrency").value("BRL"));

        verify(pricingService).calculatePricing(any(CreatePricingSimulationRequest.class));
    }

    // Garante a conversão da face, do valor presente e do deságio para USD.
    @Test
    void shouldSimulateDuplicataInUsdUsingValidExchangeRate() throws Exception {
        when(pricingService.calculatePricing(any(CreatePricingSimulationRequest.class)))
                .thenReturn(new PricingResult(
                        new BigDecimal("18409.09"),
                        new BigDecimal("17094.67"),
                        new BigDecimal("1314.42"),
                        CurrencyCode.USD,
                        new BigDecimal("5.4321")));

        mockMvc.perform(post("/pricing/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "faceValue": 100000.00,
                                  "type": "DUPLICATA",
                                  "paymentCurrency": "USD",
                                  "termMonths": 3
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.faceValue").value(18409.09))
                .andExpect(jsonPath("$.presentValue").value(17094.67))
                .andExpect(jsonPath("$.discount").value(1314.42))
                .andExpect(jsonPath("$.paymentCurrency").value("USD"));

        verify(pricingService).calculatePricing(any(CreatePricingSimulationRequest.class));
    }

    // Garante que campos obrigatórios inválidos sejam rejeitados.
    @Test
    void shouldRejectInvalidRequest() throws Exception {
        mockMvc.perform(post("/pricing/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(pricingService);
    }

    // Garante que a simulação em moeda estrangeira falhe sem uma taxa válida.
    @Test
    void shouldRejectForeignCurrencyWithoutValidExchangeRate() throws Exception {
        when(pricingService.calculatePricing(any(CreatePricingSimulationRequest.class)))
                .thenThrow(new IllegalStateException("No valid exchange rate for USD/BRL"));

        mockMvc.perform(post("/pricing/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "faceValue": 100000.00,
                                  "type": "DUPLICATA",
                                  "paymentCurrency": "USD",
                                  "termMonths": 3
                                }
                                """))
                .andExpect(status().isConflict());

        verify(pricingService).calculatePricing(any(CreatePricingSimulationRequest.class));
    }
}
