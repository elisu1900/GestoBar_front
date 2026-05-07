package com.elias.gestobar.model.dto;

public record TableDto(
        Integer tableId,
        Integer number,
        Integer capacity,
        Boolean isActive
) {}
