package com.elias.gestobar.service;

import com.elias.gestobar.config.AppConfig;
import com.elias.gestobar.model.dto.TicketDto;
import com.elias.gestobar.util.ApiException;

import java.util.HashMap;
import java.util.Map;

public class TicketApiService {

    private final ApiClient api = ApiClient.getInstance();

    // POST /api/tickets
    public TicketDto createTicket(Integer tableId) throws ApiException {
        Map<String, Object> body = new HashMap<>();
        body.put("tableId", tableId);
        return api.post(AppConfig.getTicketsEndpoint(), body, TicketDto.class);
    }

    // GET /api/tickets/{ticketId}
    public TicketDto getTicket(Integer ticketId) throws ApiException {
        return api.get(AppConfig.getTicketsEndpoint() + "/" + ticketId, TicketDto.class);
    }

    // POST /api/tickets/{ticketId}/details
    public void addProduct(Integer ticketId, Integer productId, Integer quantity) throws ApiException {
        Map<String, Object> body = new HashMap<>();
        body.put("productId", productId);
        body.put("quantity", quantity);
        api.post(AppConfig.getTicketsEndpoint() + "/" + ticketId + "/details", body, Object.class);
    }

    // PATCH /api/tickets/{ticketId}/details/{productId}

    public void updateQuantity(Integer ticketId, Integer productId, Integer quantity) throws ApiException {
        Map<String, Object> body = new HashMap<>();
        body.put("productId", productId);
        body.put("quantity", quantity);
        api.patch(AppConfig.getTicketsEndpoint() + "/" + ticketId + "/details/" + productId, body, Object.class);
    }

    // DELETE /api/tickets/{ticketId}/details/{productId}
    public void deleteProduct(Integer ticketId, Integer productId) throws ApiException {
        api.delete(AppConfig.getTicketsEndpoint() + "/" + ticketId + "/details/" + productId);
    }

    // PATCH /api/tickets/{ticketId}/close
    public void closeTicket(Integer ticketId) throws ApiException {
        api.patch(AppConfig.getTicketsEndpoint() + "/" + ticketId + "/close");
    }
}
