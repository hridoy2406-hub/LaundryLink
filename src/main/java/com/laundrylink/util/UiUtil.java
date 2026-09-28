package com.laundrylink.util;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.function.Function;

/** Small helpers to keep the UI classes short. */
public class UiUtil {
    private UiUtil() {}

    public static String money(double value) {
        return String.format("%.2f Tk", value);
    }

    public static <T> void addColumn(TableView<T> table, String title, Function<T, String> valueFn) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setCellValueFactory(data -> new SimpleStringProperty(valueFn.apply(data.getValue())));
        table.getColumns().add(column);
    }

    /** Responsive columns: every column gets a share of the table width (ratios add up to about 0.97). */
    public static void bindColumnWidths(TableView<?> table, double... ratios) {
        for (int i = 0; i < ratios.length && i < table.getColumns().size(); i++) {
            table.getColumns().get(i).prefWidthProperty().bind(table.widthProperty().multiply(ratios[i]));
        }
    }

    public static Label sectionTitle(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("section-title");
        return label;
    }

    public static VBox statCard(String title, Label valueLabel) {
        valueLabel.getStyleClass().add("stat-value");
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("stat-label");
        VBox card = new VBox(4, valueLabel, titleLabel);
        card.getStyleClass().add("stat-card");
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    /** GridPane with equal-width columns that grow with the window. */
    public static GridPane cardGrid(int columns, Node... cards) {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        for (int c = 0; c < columns; c++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / columns);
            grid.getColumnConstraints().add(cc);
        }
        for (int i = 0; i < cards.length; i++) {
            grid.add(cards[i], i % columns, i / columns);
        }
        return grid;
    }

    public static VBox page(Node... children) {
        VBox box = new VBox(12, children);
        box.setPadding(new Insets(15));
        return box;
    }
}
