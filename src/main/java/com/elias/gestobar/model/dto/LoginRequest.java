package com.elias.gestobar.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginRequest(
        @JsonProperty("username") String username,
        @JsonProperty("password") String password
) {}