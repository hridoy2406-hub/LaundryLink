package com.laundrylink.controller;

import com.laundrylink.network.WeatherInfo;
import com.laundrylink.network.WeatherService;
import com.laundrylink.task.TaskManager;
import com.laundrylink.util.UiUtil;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;

/** Shows live weather (from a public JSON API) so customers know how drying will go. */
public class WeatherTab extends Tab {

    private final WeatherService service = new WeatherService();
    private final Label statusLabel = new Label();
    private final Label temperature = new Label("-");
    private final Label humidity = new Label("-");
    private final Label rain = new Label("-");
    private final Label wind = new Label("-");
    private final Label advice = new Label();
    private final Label forecast = new Label();

    public WeatherTab() {
        super("Weather");
        setClosable(false);

        advice.setWrapText(true);
        advice.getStyleClass().add("section-title");

        Button refreshBtn = new Button("Refresh Weather");
        refreshBtn.getStyleClass().add("primary-button");
        refreshBtn.setOnAction(e -> load());

        VBox content = UiUtil.page(UiUtil.sectionTitle("Weather in Dhaka (Open-Meteo API)"), refreshBtn, statusLabel,
                UiUtil.cardGrid(4,
                        UiUtil.statCard("Temperature (C)", temperature),
                        UiUtil.statCard("Humidity (%)", humidity),
                        UiUtil.statCard("Rain (mm)", rain),
                        UiUtil.statCard("Wind (km/h)", wind)),
                advice, UiUtil.sectionTitle("3-day forecast"), forecast);
        setContent(content);
        load();
    }

    private void load() {
        statusLabel.setText("Loading weather...");
        // network call runs on a pool thread so the UI never freezes
        Task<WeatherInfo> task = new Task<>() {
            @Override
            protected WeatherInfo call() throws Exception {
                return service.fetchWeather();
            }
        };
        task.setOnSucceeded(e -> show(task.getValue()));
        task.setOnFailed(e -> statusLabel.setText("Could not load weather: " + task.getException().getMessage()));
        TaskManager.execute(task);
    }

    private void show(WeatherInfo info) {
        statusLabel.setText("Updated just now");
        temperature.setText(String.valueOf(info.temperature()));
        humidity.setText(String.valueOf(info.humidity()));
        rain.setText(String.valueOf(info.precipitation()));
        wind.setText(String.valueOf(info.windSpeed()));
        advice.setText(info.dryingAdvice());
        forecast.setText(String.join("\n", info.forecast()));
    }
}