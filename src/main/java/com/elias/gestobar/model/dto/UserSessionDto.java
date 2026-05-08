package com.elias.gestobar.model.dto;

import com.elias.gestobar.model.enums.Role;
import com.fasterxml.jackson.annotation.JsonProperty;

public record UserSessionDto(
        @JsonProperty("id")       Long id,
        @JsonProperty("nombre")   String nombre,
        @JsonProperty("apellido") String apellido,
        @JsonProperty("role")     Role role,
        @JsonProperty("active")   boolean active
) {}