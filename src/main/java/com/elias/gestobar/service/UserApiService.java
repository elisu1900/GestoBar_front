package com.elias.gestobar.service;

import com.elias.gestobar.config.AppConfig;
import com.elias.gestobar.model.dto.UserDto;
import com.elias.gestobar.model.dto.UserRequestDto;
import com.elias.gestobar.util.ApiException;
import com.elias.gestobar.util.JsonMapper;

import java.util.List;

public class UserApiService {

    private final ApiClient api = ApiClient.getInstance();

    // GET /api/users
    public List<UserDto> getAllUsers() throws ApiException {
        String json = api.getRaw(AppConfig.getUsersEndpoint());
        return JsonMapper.fromJsonList(json, UserDto.class);
    }

    // POST /api/users
    public UserDto createUser(UserRequestDto request) throws ApiException {
        return api.post(AppConfig.getUsersEndpoint(), request, UserDto.class);
    }

    // DELETE /api/users/{id}
    public void deleteUser(Integer id) throws ApiException {
        api.delete(AppConfig.getUsersEndpoint() + "/" + id);
    }
}
