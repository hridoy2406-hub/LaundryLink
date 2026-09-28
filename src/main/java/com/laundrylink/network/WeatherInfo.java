package com.laundrylink.network;

import java.util.List;

/** Parsed weather data (a record is a short immutable data class). */
public record WeatherInfo(double temperature, int humidity, double precipitation,
                          double windSpeed, List<String> forecast) {

    public String dryingAdvice() {
        if (precipitation > 0) {
            return "It is raining now. Clothes will dry slowly, so expect a small delay.";
        }
        if (humidity > 80) {
            return "Humidity is high. Drying may take longer than usual.";
        }
        return "Good weather for drying clothes.";
    }
}