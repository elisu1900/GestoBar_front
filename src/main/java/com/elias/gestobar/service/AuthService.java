package com.elias.gestobar.service;

import com.elias.gestobar.config.AppConfig;
import com.elias.gestobar.config.SessionManager;
import com.elias.gestobar.model.dto.LoginRequest;
import com.elias.gestobar.model.dto.UserSessionDto;
import com.elias.gestobar.util.ApiException;

public class AuthService {

    private final ApiClient api = ApiClient.getInstance();

    public UserSessionDto login(String username, String password) throws ApiException {
        var request = new LoginRequest(username, password);
        return api.post(
                AppConfig.getLoginEndpoint(),
                request,
                UserSessionDto.class
        );
    }

    public UserSessionDto me() throws ApiException {
        return api.get(AppConfig.getMeEndpoint(), UserSessionDto.class);
    }


    public void logout() throws ApiException {
        api.post(AppConfig.getLogoutEndpoint());
        SessionManager.getInstance().clear();
    }
}