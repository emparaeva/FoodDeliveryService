package com.example.fooddeliverysystem2.client.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpClientUtil {

    private static final String BASE_URL = "http://localhost:8080/api";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String sendGetRequest(String endpoint) throws IOException {
        // Убираем начальный слеш из endpoint, чтобы избежать дублирования
        String cleanedEndpoint = endpoint.startsWith("/") ? endpoint.substring(1) : endpoint;
        String url = BASE_URL + "/" + cleanedEndpoint;
        System.out.println("Sending GET request to: " + url);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json; charset=UTF-8")
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
            }
            System.out.println("GET /" + cleanedEndpoint + " response: " + response.body());
            return response.body();
        } catch (InterruptedException e) {
            System.out.println("Interrupted during GET /" + cleanedEndpoint + ": " + e.getMessage());
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        }
    }

    public static String sendPostRequest(String endpoint, Map<String, Object> data) throws IOException {
        String cleanedEndpoint = endpoint.startsWith("/") ? endpoint.substring(1) : endpoint;
        String url = BASE_URL + "/" + cleanedEndpoint;
        String json = mapper.writeValueAsString(data);
        System.out.println("POST /" + cleanedEndpoint + " request body: " + json);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
            }
            System.out.println("POST /" + cleanedEndpoint + " response: " + response.body());
            return response.body();
        } catch (InterruptedException e) {
            System.out.println("Interrupted during POST /" + cleanedEndpoint + ": " + e.getMessage());
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        }
    }

    public static String sendPutRequest(String endpoint, Map<String, Object> data) throws IOException {
        String cleanedEndpoint = endpoint.startsWith("/") ? endpoint.substring(1) : endpoint;
        String url = BASE_URL + "/" + cleanedEndpoint;
        String json = mapper.writeValueAsString(data);
        System.out.println("PUT /" + cleanedEndpoint + " request body: " + json);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json; charset=UTF-8")
                .PUT(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
            }
            System.out.println("PUT /" + cleanedEndpoint + " response: " + response.body());
            return response.body();
        } catch (InterruptedException e) {
            System.out.println("Interrupted during PUT /" + cleanedEndpoint + ": " + e.getMessage());
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        }
    }

    public static void sendDeleteRequest(String endpoint) throws IOException {
        String cleanedEndpoint = endpoint.startsWith("/") ? endpoint.substring(1) : endpoint;
        String url = BASE_URL + "/" + cleanedEndpoint;
        System.out.println("Sending DELETE request to: " + url);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json; charset=UTF-8")
                .DELETE()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
            }
            System.out.println("DELETE /" + cleanedEndpoint + " response: " + response.body());
        } catch (InterruptedException e) {
            System.out.println("Interrupted during DELETE /" + cleanedEndpoint + ": " + e.getMessage());
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        }
    }
}