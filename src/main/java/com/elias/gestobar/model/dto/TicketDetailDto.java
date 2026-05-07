package com.elias.gestobar.model.dto;

import java.math.BigDecimal;

public record TicketDetailDto(
        Integer ticketId,
        Integer productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice
) {}
