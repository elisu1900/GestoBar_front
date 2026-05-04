package com.elias.gestobar.service;

import com.elias.gestobar.config.AppConfig;
import com.elias.gestobar.config.SessionManager;
import com.elias.gestobar.model.dto.LoginRequest;
import com.elias.gestobar.model.dto.UserSessionDto;
import com.elias.gestobar.util.ApiException;

// services/AuthService.java
public class AuthService {

    private final ApiClient api = ApiClient.getInstance();

    /**
     * POST /api/auth/login
     * Devuelve los datos del usuario y establece la cookie de sesión automáticamente.
     */
    public UserSessionDto login(String username, String password) throws ApiException {
        var request = new LoginRequest(username, password);
        return api.post(
                AppConfig.getLoginEndpoint(),
                request,
                UserSessionDto.class
        );
    }

    /**
     * GET /api/auth/me
     * Útil para verificar si la sesión sigue activa al arrancar la app.
     */
    public UserSessionDto me() throws ApiException {
        return api.get(AppConfig.getMeEndpoint(), UserSessionDto.class);
    }

    /**
     * POST /api/auth/logout
     * Invalida la sesión en el servidor y limpia los datos locales.
     */
    public void logout() throws ApiException {
        api.post(AppConfig.getLogoutEndpoint());
        SessionManager.getInstance().clear();
    }
}