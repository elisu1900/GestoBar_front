package com.elias.gestobar.model.dto;

import java.math.BigDecimal;
import java.util.List;

public record TicketDto(
        Integer ticketId,
        Integer tableId,
        Integer tableNumber,
        String status,
        BigDecimal total,
        List<TicketDetailDto> details
) {}
