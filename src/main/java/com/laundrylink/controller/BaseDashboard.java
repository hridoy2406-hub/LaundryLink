package com.laundrylink.controller;

import com.laundrylink.model.User;
import com.laundrylink.util.SceneNavigator;
import com.laundrylink.util.Session;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** Common layout of all dashboards: header on top, subclasses fill the center. */
public abstract class BaseDashboard extends BorderPane {

    protected final User user;

    protected BaseDashboard(User user) {
        this.user = user;
        setTop(buildHeader());
    }

    private HBox buildHeader() {
        Label title = new Label("LaundryLink");
        title.getStyleClass().add("header-title");
        Label role = new Label(user.getDashboardTitle());
        role.getStyleClass().add("header-sub");
        VBox titleBox = new VBox(2, title, role);

        Label welcome = new Label("Welcome, " + user.getFullName());
        welcome.getStyleClass().add("header-welcome");

        Button logout = new Button("Logout");
        logout.getStyleClass().add("danger-button");
        logout.setOnAction(e -> logout());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(15, titleBox, spacer, welcome, logout);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 20, 12, 20));
        header.getStyleClass().add("header-bar");
        return header;
    }

    protected void logout() {
        Session.clear();
        SceneNavigator.showLogin();
    }
}
