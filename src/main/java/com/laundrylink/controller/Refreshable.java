package com.laundrylink.controller;

import javafx.scene.control.TabPane;

/** A tab that can reload its data. Tabs reload automatically when selected. */
public interface Refreshable {
    void refresh();

    static void enableAutoRefresh(TabPane tabPane) {
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab instanceof Refreshable r) r.refresh();
        });
    }
}
