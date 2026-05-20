package com.elias.gestobar.service;

import com.elias.gestobar.config.AppConfig;
import com.elias.gestobar.util.ApiException;
import com.elias.gestobar.util.JsonMapper;

import java.net.URI;
import java.net.http.HttpRequest;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.http.HttpClient;
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

    // GET
    public <T> T get(String endpoint, Class<T> responseType) throws ApiException {
        var request = HttpRequest.newBuilder()
                .uri(uri(endpoint))
                .header("Content-Type", "application/json")
                .GET()
                .build();
        return send(request, responseType);
    }

    // GET — String JSON (listass)
    public String getRaw(String endpoint) throws ApiException {
        var request = HttpRequest.newBuilder()
                .uri(uri(endpoint))
                .header("Content-Type", "application/json")
                .GET()
                .build();
        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            handleStatus(response.statusCode(), response.body());
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new ApiException("Unable to connect to the server");
        }
    }

    // POST con body y respuesta
    public <T> T post(String endpoint, Object body, Class<T> responseType) throws ApiException {
        var request = HttpRequest.newBuilder()
                .uri(uri(endpoint))
                .header("Content-Type", "application/json")
                .POST(jsonBody(body))
                .build();
        return send(request, responseType);
    }

    // POST sin body
    public void post(String endpoint) throws ApiException {
        var request = HttpRequest.newBuilder()
                .uri(uri(endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        sendRaw(request);
    }

    // PUT
    public <T> T put(String endpoint, Object body, Class<T> responseType) throws ApiException {
        var request = HttpRequest.newBuilder()
                .uri(uri(endpoint))
                .header("Content-Type", "application/json")
                .PUT(jsonBody(body))
                .build();
        return send(request, responseType);
    }

    // PATCH con body y respuesta
    public <T> T patch(String endpoint, Object body, Class<T> responseType) throws ApiException {
        var request = HttpRequest.newBuilder()
                .uri(uri(endpoint))
                .header("Content-Type", "application/json")
                .method("PATCH", body != null ? jsonBody(body) : HttpRequest.BodyPublishers.noBody())
                .build();
        return send(request, responseType);
    }

    // PATCH sin body
    public void patch(String endpoint) throws ApiException {
        var request = HttpRequest.newBuilder()
                .uri(uri(endpoint))
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .build();
        sendRaw(request);
    }

    // DELETE
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
            handleStatus(response.statusCode(), response.body());
            return JsonMapper.fromJson(response.body(), responseType);
        } catch (IOException | InterruptedException e) {
            throw new ApiException("Unable to connect to the server");
        }
    }

    private void sendRaw(HttpRequest request) throws ApiException {
        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            handleStatus(response.statusCode(), response.body());
        } catch (IOException | InterruptedException e) {
            throw new ApiException("Unable to connect to the server");
        }
    }

    private void handleStatus(int status, String body) throws ApiException {
        switch (status) {
            case 400 -> throw new ApiException(parseErrorMessage(body, "Datos incorrectos."), 400);
            case 401 -> throw new ApiException("No autorizado. Inicia sesión.", 401);
            case 403 -> throw new ApiException("Acceso denegado.", 403);
            case 404 -> throw new ApiException("Recurso no encontrado.", 404);
            case 409 -> throw new ApiException(parseErrorMessage(body, "El recurso ya existe."), 409);
            case 204 -> {}
            default  -> { if (status >= 400) throw new ApiException(
                    parseErrorMessage(body, "Error del servidor: " + status), status); }
        }
    }

    private String parseErrorMessage(String body, String fallback) {
        try {
            // El backend devuelve {"error": "mensaje", ...}
            var node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(body);
            if (node.has("error")) return node.get("error").asText();
            if (node.has("message")) return node.get("message").asText();
        } catch (Exception ignored) {}
        return fallback;
    }

    private URI uri(String endpoint) {
        return URI.create(baseUrl + endpoint);
    }

    private HttpRequest.BodyPublisher jsonBody(Object body) {
        return HttpRequest.BodyPublishers.ofString(JsonMapper.toJson(body));
    }
}