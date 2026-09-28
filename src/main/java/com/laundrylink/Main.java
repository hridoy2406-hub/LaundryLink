package com.laundrylink;

import com.laundrylink.database.DatabaseInitializer;
import com.laundrylink.task.TaskManager;
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

    @Override
    public void stop() {
        TaskManager.shutdown(); // stop the thread pool when the window closes
    }

    public static void main(String[] args) {
        launch(args);
    }
}