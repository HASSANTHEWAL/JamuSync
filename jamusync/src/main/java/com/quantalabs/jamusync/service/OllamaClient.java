package com.quantalabs.jamusync.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public class OllamaClient {

    private static final String API_URL = "http://localhost:11434/api/chat";
    private static final String MODEL = "llama3.2";

    private final Gson gson = new Gson();
    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    public String chat(String systemPrompt, String userMessage) {
        try {
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", MODEL);
            requestBody.addProperty("stream", false);

            JsonArray messages = new JsonArray();

            JsonObject systemMsg = new JsonObject();
            systemMsg.addProperty("role", "system");
            systemMsg.addProperty("content", systemPrompt);
            messages.add(systemMsg);

            JsonObject userMsg = new JsonObject();
            userMsg.addProperty("role", "user");
            userMsg.addProperty("content", userMessage);
            messages.add(userMsg);

            requestBody.add("messages", messages);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return "Ollama service unavailable (HTTP " + response.statusCode() + "). " +
                       "Make sure Ollama is running with model " + MODEL + ".";
            }

            JsonObject json = gson.fromJson(response.body(), JsonObject.class);
            if (json.has("message")) {
                JsonObject message = json.getAsJsonObject("message");
                if (message.has("content")) {
                    return message.get("content").getAsString().trim();
                }
            }
            return "No response from Ollama.";
        } catch (Exception e) {
            return "Could not connect to Ollama at localhost:11434. " +
                   "Please ensure Ollama is running with the llama3.2 model. (" + e.getMessage() + ")";
        }
    }

    public String getProductRecommendation(String productContext, String userQuery) {
        String systemPrompt = "You are a helpful Jamu (traditional Indonesian herbal drink) advisor for JamuSync. " +
            "Recommend products from the catalog based on the user's health needs. " +
            "Be concise, friendly, and mention specific product names from the catalog.\n\n" +
            productContext;
        return chat(systemPrompt, userQuery);
    }

    public String getHealthAdvice(String productContext, String userQuery) {
        String systemPrompt = "You are a Jamu health advisor for JamuSync. " +
            "Provide general wellness advice about traditional Indonesian herbal drinks. " +
            "Do not provide medical diagnoses. Reference products from this catalog when relevant:\n\n" +
            productContext;
        return chat(systemPrompt, userQuery);
    }
}
