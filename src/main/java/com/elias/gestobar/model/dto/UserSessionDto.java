package com.elias.gestobar.model.dto;

import com.elias.gestobar.model.enums.Role;
import com.fasterxml.jackson.annotation.JsonProperty;

public record UserSessionDto(
        @JsonProperty("id")       Integer id,
        @JsonProperty("name")   String name,
        @JsonProperty("lastName") String lastName,
        @JsonProperty("role")     Role role,
        @JsonProperty("active")   boolean active
) {}