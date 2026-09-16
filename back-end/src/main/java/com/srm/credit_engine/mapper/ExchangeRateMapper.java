package com.srm.credit_engine.mapper;

import java.util.List;

import com.srm.credit_engine.controller.dto.CreateExchangeRateRequest;
import com.srm.credit_engine.controller.dto.ExchangeRateResponse;
import com.srm.credit_engine.domain.entity.ExchangeRate;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ExchangeRateMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    ExchangeRate toEntity(CreateExchangeRateRequest request);

    ExchangeRateResponse toResponse(ExchangeRate exchangeRate);

    List<ExchangeRateResponse> toResponse(List<ExchangeRate> exchangeRates);
}
