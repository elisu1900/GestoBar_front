package com.elias.gestobar.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record ProductBalanceDto(
        @JsonProperty("productName") String productName,
        @JsonProperty("qtySold")     Integer qtySold,
        @JsonProperty("revenue")     BigDecimal revenue,
        @JsonProperty("cost")        BigDecimal cost,
        @JsonProperty("profit")      BigDecimal profit
) {}
