package com.elias.gestobar.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserRequestDto(
        @JsonProperty("name")     String name,
        @JsonProperty("lastName") String lastName,
        @JsonProperty("password") String password,
        @JsonProperty("role")     String role,
        @JsonProperty("isActive") Boolean isActive
) {}