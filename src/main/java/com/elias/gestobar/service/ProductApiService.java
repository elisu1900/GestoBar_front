package com.elias.gestobar.service;

import com.elias.gestobar.config.AppConfig;
import com.elias.gestobar.model.dto.ProductDto;
import com.elias.gestobar.model.dto.ProductRequestDto;
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
    // GET /api/products
    public List<ProductDto> getAllProducts() throws ApiException {
        String json = api.getRaw(AppConfig.getProductsEndpoint());
        return JsonMapper.fromJsonList(json, ProductDto.class);
    }

    // POST /api/products
    public ProductDto createProduct(ProductRequestDto request) throws ApiException {
        return api.post(AppConfig.getProductsEndpoint(), request, ProductDto.class);
    }

    // PUT /api/products/{id}
    public ProductDto updateProduct(Integer id, ProductRequestDto request) throws ApiException {
        return api.put(AppConfig.getProductsEndpoint() + "/" + id, request, ProductDto.class);
    }

    // DELETE /api/products/{id}
    public void deleteProduct(Integer id) throws ApiException {
        api.delete(AppConfig.getProductsEndpoint() + "/" + id);
    }
}
