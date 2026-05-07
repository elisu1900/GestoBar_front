package com.elias.gestobar.service;

import com.elias.gestobar.config.AppConfig;
import com.elias.gestobar.model.dto.TableDto;
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
}
