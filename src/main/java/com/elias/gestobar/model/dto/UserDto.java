package com.elias.gestobar.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserDto(
        @JsonProperty("userId")   Integer userId,
        @JsonProperty("name")     String name,
        @JsonProperty("lastName") String lastName,
        @JsonProperty("role")     String role,
        @JsonProperty("isActive") Boolean isActive
) {}
