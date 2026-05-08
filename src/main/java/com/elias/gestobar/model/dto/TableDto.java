package com.elias.gestobar.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TableDto(
        @JsonProperty("tableId")   Integer tableId,
        @JsonProperty("number")    Integer number,
        @JsonProperty("capacity")  Integer capacity,
        @JsonProperty("isActive")  Boolean isActive
) {}