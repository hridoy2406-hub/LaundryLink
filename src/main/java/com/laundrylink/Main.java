package com.laundrylink;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Entry point of the JavaFX application.
 * For now it only shows a setup-check window.
 * In Stage 5 this will load the login screen instead.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        Label title = new Label("LaundryLink");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold;");

        Label subtitle = new Label("Laundry Management System");
        Label javaInfo = new Label("Java: " + System.getProperty("java.version"));
        Label fxInfo = new Label("JavaFX: " + System.getProperty("javafx.version"));
        Label sqliteInfo = new Label("SQLite JDBC: " + checkSqliteDriver());
        Label jacksonInfo = new Label("Jackson: " + new ObjectMapper().version());

        VBox content = new VBox(8, title, subtitle, javaInfo, fxInfo, sqliteInfo, jacksonInfo);
        content.setAlignment(Pos.CENTER);

        StackPane root = new StackPane(content);
        Scene scene = new Scene(root, 900, 600);

        primaryStage.setTitle("LaundryLink - Laundry Management System");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(700);
        primaryStage.setMinHeight(450);
        primaryStage.show();
    }

    // Quick check that the SQLite driver jar was downloaded by Maven.
    private String checkSqliteDriver() {
        try {
            Class.forName("org.sqlite.JDBC");
            return "OK";
        } catch (ClassNotFoundException e) {
            return "NOT FOUND";
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}