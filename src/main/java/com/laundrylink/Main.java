package com.laundrylink;

import com.laundrylink.database.DatabaseInitializer;
import com.laundrylink.util.SceneNavigator;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        DatabaseInitializer.initialize();
        SceneNavigator.init(primaryStage);
        SceneNavigator.showLogin();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
