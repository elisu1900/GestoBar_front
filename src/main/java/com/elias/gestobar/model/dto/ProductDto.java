package com.elias.gestobar.model.dto;

import java.math.BigDecimal;

public record ProductDto(
        Integer productId,
        Integer categoryId,
        String categoryName,
        String name,
        BigDecimal sellPrice,
        Boolean isActive
) {}
