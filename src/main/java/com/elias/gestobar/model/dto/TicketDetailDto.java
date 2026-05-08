package com.elias.gestobar.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record TicketDetailDto(
        @JsonProperty("ticketId")     Integer ticketId,
        @JsonProperty("productId")    Integer productId,
        @JsonProperty("productName")  String productName,
        @JsonProperty("quantity")     Integer quantity,
        @JsonProperty("unitPrice")    BigDecimal unitPrice
) {}