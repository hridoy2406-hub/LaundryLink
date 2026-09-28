package com.laundrylink.util;

import com.laundrylink.controller.AdminDashboard;
import com.laundrylink.controller.BaseDashboard;
import com.laundrylink.controller.CustomerDashboard;
import com.laundrylink.controller.StaffDashboard;
import com.laundrylink.model.Admin;
import com.laundrylink.model.Customer;
import com.laundrylink.model.Staff;
import com.laundrylink.model.User;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

/** Switches screens. Every screen sits inside an AnchorPane that follows the window size. */
public class SceneNavigator {
    private static final double COMPACT_WIDTH = 850;

    private static Stage stage;
    private static Scene scene;

    private SceneNavigator() {}

    public static void init(Stage primaryStage) {
        stage = primaryStage;
        scene = new Scene(new AnchorPane(), 1000, 650);
        scene.getStylesheets().add(SceneNavigator.class.getResource("/css/app.css").toExternalForm());
        // react to window width changes
        scene.widthProperty().addListener((obs, oldWidth, newWidth) -> updateCompactMode());
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

    private static void setRoot(Parent content, String title) {
        AnchorPane holder = new AnchorPane(content);
        // all four anchors = 0: the content always fills the whole window
        AnchorPane.setTopAnchor(content, 0.0);
        AnchorPane.setBottomAnchor(content, 0.0);
        AnchorPane.setLeftAnchor(content, 0.0);
        AnchorPane.setRightAnchor(content, 0.0);

        // live window size, bound to the scene width and height
        Label sizeLabel = new Label();
        sizeLabel.textProperty().bind(Bindings.format("%.0f x %.0f", scene.widthProperty(), scene.heightProperty()));
        sizeLabel.getStyleClass().add("size-label");
        sizeLabel.setMouseTransparent(true);
        AnchorPane.setBottomAnchor(sizeLabel, 6.0);
        AnchorPane.setRightAnchor(sizeLabel, 10.0);
        holder.getChildren().add(sizeLabel);

        scene.setRoot(holder);
        stage.setTitle(title);
        updateCompactMode();
    }

    private static void updateCompactMode() {
        Parent root = scene.getRoot();
        boolean compact = scene.getWidth() < COMPACT_WIDTH;
        if (compact && !root.getStyleClass().contains("compact")) {
            root.getStyleClass().add("compact");
        } else if (!compact) {
            root.getStyleClass().remove("compact");
        }
    }
}