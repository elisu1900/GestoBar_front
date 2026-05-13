package com.elias.gestobar.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

public record DailyBalanceDto(
        @JsonProperty("totalRevenue") BigDecimal totalRevenue,
        @JsonProperty("totalCosts")   BigDecimal totalCosts,
        @JsonProperty("realProfit")   BigDecimal realProfit,
        @JsonProperty("breakdown")    List<ProductBalanceDto> breakdown
) {}
