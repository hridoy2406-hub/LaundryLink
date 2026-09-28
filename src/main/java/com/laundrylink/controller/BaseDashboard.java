package com.laundrylink.controller;

import com.laundrylink.model.User;
import com.laundrylink.task.ReminderTask;
import com.laundrylink.task.TaskManager;
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

import java.time.LocalTime;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/** Common layout of all dashboards + background reminders. */
public abstract class BaseDashboard extends BorderPane {

    protected final User user;
    private final Label notificationLabel = new Label("No notifications yet");
    private final ScheduledFuture<?> reminderFuture;

    protected BaseDashboard(User user) {
        this.user = user;
        setTop(buildHeader());
        setBottom(buildNotificationBar());

        // reminder check starts after 3 seconds and repeats every 30 seconds
        reminderFuture = TaskManager.scheduleRepeating(
                new ReminderTask(user, this::showNotification), 3, 30, TimeUnit.SECONDS);
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

    private HBox buildNotificationBar() {
        notificationLabel.getStyleClass().add("notification-label");
        HBox bar = new HBox(notificationLabel);
        bar.setPadding(new Insets(6, 20, 6, 20));
        bar.getStyleClass().add("notification-bar");
        return bar;
    }

    protected void showNotification(String message) {
        notificationLabel.setText(LocalTime.now().withNano(0) + "   " + message);
    }

    protected void logout() {
        reminderFuture.cancel(true); // stop this user's reminder task
        Session.clear();
        SceneNavigator.showLogin();
    }
}