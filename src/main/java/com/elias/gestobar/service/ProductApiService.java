package com.elias.gestobar.service;

import com.elias.gestobar.config.AppConfig;
import com.elias.gestobar.model.dto.ProductDto;
import com.elias.gestobar.util.ApiException;
import com.elias.gestobar.util.JsonMapper;

import java.util.List;

public class ProductApiService {

    private final ApiClient api = ApiClient.getInstance();

    // GET /api/products/active
    public List<ProductDto> getActiveProducts() throws ApiException {
        String json = api.getRaw(AppConfig.getProductsActiveEndpoint());
        return JsonMapper.fromJsonList(json, ProductDto.class);
    }
}
