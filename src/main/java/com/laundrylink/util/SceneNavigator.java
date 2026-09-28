package com.laundrylink.util;

import com.laundrylink.controller.AdminDashboard;
import com.laundrylink.controller.BaseDashboard;
import com.laundrylink.controller.CustomerDashboard;
import com.laundrylink.controller.StaffDashboard;
import com.laundrylink.model.Admin;
import com.laundrylink.model.Customer;
import com.laundrylink.model.Staff;
import com.laundrylink.model.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

/** Switches screens by replacing the root of the one and only Scene. */
public class SceneNavigator {
    private static Stage stage;
    private static Scene scene;

    private SceneNavigator() {}

    public static void init(Stage primaryStage) {
        stage = primaryStage;
        scene = new Scene(new StackPane(), 1000, 650);
        scene.getStylesheets().add(SceneNavigator.class.getResource("/css/app.css").toExternalForm());
        stage.setScene(scene);
        stage.setMinWidth(700);
        stage.setMinHeight(500);
    }

    public static void showLogin() {
        try {
            Parent root = FXMLLoader.load(SceneNavigator.class.getResource("/fxml/login.fxml"));
            setRoot(root, "LaundryLink - Login");
        } catch (IOException e) {
            throw new RuntimeException("Cannot load login screen", e);
        }
    }

    public static void showDashboard(User user) {
        BaseDashboard dashboard = switch (user.getRole()) {
            case CUSTOMER -> new CustomerDashboard((Customer) user);
            case STAFF -> new StaffDashboard((Staff) user);
            case ADMIN -> new AdminDashboard((Admin) user);
        };
        setRoot(dashboard, "LaundryLink - " + user.getDashboardTitle());
    }

    private static void setRoot(Parent root, String title) {
        scene.setRoot(root);
        stage.setTitle(title);
    }
}
