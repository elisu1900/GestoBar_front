package com.elias.gestobar.controllers;

import com.elias.gestobar.config.AppConfig;
import com.elias.gestobar.util.ApiException;
import com.elias.gestobar.util.JsonMapper;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ApiClient {

    private static ApiClient instance;

    private final HttpClient client;
    private final String baseUrl;

    private ApiClient() {
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        this.baseUrl = AppConfig.getApiBaseUrl();
        this.client  = HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .connectTimeout(Duration.ofSeconds(AppConfig.getConnectTimeout()))
                .build();
    }

    public static ApiClient getInstance() {
        if (instance == null) instance = new ApiClient();
        return instance;
    }

    public <T> T get(String endpoint, Class<T> responseType) throws ApiException {
        var request = HttpRequest.newBuilder()
                .uri(uri(endpoint))
                .header("Content-Type", "application/json")
                .GET()
                .build();
        return send(request, responseType);
    }

    public String getRaw(String endpoint) throws ApiException {
        var request = HttpRequest.newBuilder()
                .uri(uri(endpoint))
                .header("Content-Type", "application/json")
                .GET()
                .build();
        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            handleStatus(response.statusCode());
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new ApiException("Unable to connect to the server");
        }
    }

    public <T> T post(String endpoint, Object body, Class<T> responseType) throws ApiException {
        var request = HttpRequest.newBuilder()
                .uri(uri(endpoint))
                .header("Content-Type", "application/json")
                .POST(jsonBody(body))
                .build();
        return send(request, responseType);
    }

    public void post(String endpoint) throws ApiException {
        var request = HttpRequest.newBuilder()
                .uri(uri(endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        sendRaw(request);
    }

    public <T> T put(String endpoint, Object body, Class<T> responseType) throws ApiException {
        var request = HttpRequest.newBuilder()
                .uri(uri(endpoint))
                .header("Content-Type", "application/json")
                .PUT(jsonBody(body))
                .build();
        return send(request, responseType);
    }

    public <T> T patch(String endpoint, Object body, Class<T> responseType) throws ApiException {
        var request = HttpRequest.newBuilder()
                .uri(uri(endpoint))
                .header("Content-Type", "application/json")
                .method("PATCH", body != null ? jsonBody(body) : HttpRequest.BodyPublishers.noBody())
                .build();
        return send(request, responseType);
    }

    public void patch(String endpoint) throws ApiException {
        var request = HttpRequest.newBuilder()
                .uri(uri(endpoint))
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .build();
        sendRaw(request);
    }

    public void delete(String endpoint) throws ApiException {
        var request = HttpRequest.newBuilder()
                .uri(uri(endpoint))
                .header("Content-Type", "application/json")
                .DELETE()
                .build();
        sendRaw(request);
    }

    private <T> T send(HttpRequest request, Class<T> responseType) throws ApiException {
        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            handleStatus(response.statusCode());
            return JsonMapper.fromJson(response.body(), responseType);
        } catch (IOException | InterruptedException e) {
            throw new ApiException("Unable to connect to the server");
        }
    }

    private void sendRaw(HttpRequest request) throws ApiException {
        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            handleStatus(response.statusCode());
        } catch (IOException | InterruptedException e) {
            throw new ApiException("Unable to connect to the server");
        }
    }

    private void handleStatus(int status) throws ApiException {
        switch (status) {
            case 401 -> throw new ApiException("Unauthorized. Please log in.", 401);
            case 403 -> throw new ApiException("Access denied.", 403);
            case 404 -> throw new ApiException("Resource not found.", 404);
            case 204 -> {}
            default  -> { if (status >= 400) throw new ApiException("Server error: " + status, status); }
        }
    }

    private URI uri(String endpoint) {
        return URI.create(baseUrl + endpoint);
    }

    private HttpRequest.BodyPublisher jsonBody(Object body) {
        return HttpRequest.BodyPublishers.ofString(JsonMapper.toJson(body));
    }
}
