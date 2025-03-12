package com.noteapp.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class KeycloakAuthService {

    private final String AUTH_SERVER_URL = "http://localhost:8080/realms/noteapp/protocol/openid-connect/token";
    private final String CLIENT_ID = "noteapp-client";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private String accessToken;
    private String refreshToken;

    public KeycloakAuthService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public boolean login(String username, String password) throws IOException, InterruptedException {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("client_id", CLIENT_ID);
        parameters.put("username", username);
        parameters.put("password", password);
        parameters.put("grant_type", "password");

        String form = parameters.entrySet()
                .stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AUTH_SERVER_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JsonNode jsonNode = objectMapper.readTree(response.body());
            this.accessToken = jsonNode.get("access_token").asText();
            this.refreshToken = jsonNode.get("refresh_token").asText();
            return true;
        } else {
            System.err.println("Login failed: " + response.statusCode());
            return false;
        }
    }

    public boolean refreshTokens() throws IOException, InterruptedException {
        if (refreshToken == null) {
            return false;
        }

        Map<String, String> parameters = new HashMap<>();
        parameters.put("client_id", CLIENT_ID);
        parameters.put("refresh_token", refreshToken);
        parameters.put("grant_type", "refresh_token");

        String form = parameters.entrySet()
                .stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AUTH_SERVER_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JsonNode jsonNode = objectMapper.readTree(response.body());
            this.accessToken = jsonNode.get("access_token").asText();
            this.refreshToken = jsonNode.get("refresh_token").asText();
            return true;
        } else {
            System.err.println("Token refresh failed: " + response.statusCode());
            return false;
        }
    }

    public void logout() {
        this.accessToken = null;
        this.refreshToken = null;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public boolean isLoggedIn() {
        return accessToken != null;
    }
}