package com.srm.credit_engine.mapper;

import java.util.List;

import com.srm.credit_engine.controller.dto.SettlementResponse;
import com.srm.credit_engine.domain.entity.Settlement;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SettlementMapper {

    @Mapping(target = "receivableId", source = "receivable.id")
    SettlementResponse toResponse(Settlement settlement);

    List<SettlementResponse> toResponse(List<Settlement> settlements);
}
