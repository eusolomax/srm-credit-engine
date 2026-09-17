package com.srm.credit_engine.mapper;

import com.srm.credit_engine.controller.dto.CreateReceivableRequest;
import com.srm.credit_engine.controller.dto.ReceivableResponse;
import com.srm.credit_engine.domain.entity.Receivable;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReceivableMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "settlement", ignore = true)
    Receivable toEntity(CreateReceivableRequest request);

    ReceivableResponse toResponse(Receivable receivable);
    List<ReceivableResponse> toResponse(List<Receivable> receivables);

}
