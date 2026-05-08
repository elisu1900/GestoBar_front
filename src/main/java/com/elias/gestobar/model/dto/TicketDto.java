package com.elias.gestobar.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

public record TicketDto(
        @JsonProperty("ticketId")     Integer ticketId,
        @JsonProperty("tableId")      Integer tableId,
        @JsonProperty("tableNumber")  Integer tableNumber,
        @JsonProperty("status")       String status,
        @JsonProperty("total")        BigDecimal total,
        @JsonProperty("details")      List<TicketDetailDto> details
) {}