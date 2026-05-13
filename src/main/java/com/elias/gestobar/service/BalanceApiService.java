package com.elias.gestobar.service;

import com.elias.gestobar.config.AppConfig;
import com.elias.gestobar.model.dto.DailyBalanceDto;
import com.elias.gestobar.util.ApiException;

public class BalanceApiService {

    private final ApiClient api = ApiClient.getInstance();

    // GET /api/balance/daily
    public DailyBalanceDto getDailyBalance() throws ApiException {
        return api.get(AppConfig.getBalanceDailyEndpoint(), DailyBalanceDto.class);
    }

    // POST /api/balance/reset
    public void resetDay() throws ApiException {
        api.post(AppConfig.getBalanceResetEndpoint());
    }
}
