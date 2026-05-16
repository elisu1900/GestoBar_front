package com.elias.gestobar.service;

import com.elias.gestobar.config.AppConfig;
import com.elias.gestobar.model.dto.TableDto;
import com.elias.gestobar.model.dto.TableRequestDto;
import com.elias.gestobar.util.ApiException;
import com.elias.gestobar.util.JsonMapper;

import java.util.List;

public class TableApiService {

    private final ApiClient api = ApiClient.getInstance();

    // GET /api/tables
    public List<TableDto> getAllTables() throws ApiException {
        String json = api.getRaw(AppConfig.getTablesEndpoint());
        return JsonMapper.fromJsonList(json, TableDto.class);
    }

    // POST /api/tables
    public TableDto createTable(TableRequestDto request) throws ApiException {
        return api.post(AppConfig.getTablesEndpoint(), request, TableDto.class);
    }

    // PATCH /api/tables/{id}/deactivate
    public void deactivateTable(Integer tableId) throws ApiException {
        api.patch(AppConfig.getTablesEndpoint() + "/" + tableId + "/deactivate");
    }
}
