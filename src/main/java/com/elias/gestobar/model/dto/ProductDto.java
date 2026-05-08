package com.elias.gestobar.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record ProductDto(
        @JsonProperty("productId")     Integer productId,
        @JsonProperty("categoryId")    Integer categoryId,
        @JsonProperty("categoryName")  String categoryName,
        @JsonProperty("name")          String name,
        @JsonProperty("sellPrice")     BigDecimal sellPrice,
        @JsonProperty("isActive")      Boolean isActive
) {}