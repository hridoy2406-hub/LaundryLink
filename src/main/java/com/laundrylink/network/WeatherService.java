package com.laundrylink.network;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/** Sends an HTTP GET request to a public API and parses the JSON answer with Jackson. */
public class WeatherService {

    private static final String API_URL = "https://api.open-meteo.com/v1/forecast"
            + "?latitude=23.81&longitude=90.41"
            + "&current=temperature_2m,relative_humidity_2m,precipitation,wind_speed_10m"
            + "&daily=temperature_2m_max,precipitation_probability_max"
            + "&timezone=auto&forecast_days=3";

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    public WeatherInfo fetchWeather() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Weather API returned status " + response.statusCode());
        }
        return parse(response.body());
    }

    // JSON parsing with Jackson: readTree() gives a tree of JsonNode objects
    private WeatherInfo parse(String json) throws IOException {
        JsonNode root = mapper.readTree(json);
        JsonNode current = root.path("current");
        JsonNode daily = root.path("daily");

        List<String> forecast = new ArrayList<>();
        JsonNode days = daily.path("time");
        for (int i = 0; i < days.size(); i++) {
            forecast.add(days.get(i).asText()
                    + "   max " + daily.path("temperature_2m_max").get(i).asDouble() + " C"
                    + "   rain chance " + daily.path("precipitation_probability_max").get(i).asInt() + "%");
        }
        return new WeatherInfo(
                current.path("temperature_2m").asDouble(),
                current.path("relative_humidity_2m").asInt(),
                current.path("precipitation").asDouble(),
                current.path("wind_speed_10m").asDouble(),
                forecast);
    }
}