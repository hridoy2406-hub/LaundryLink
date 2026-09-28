package com.laundrylink.controller;

import com.laundrylink.dao.OrderDAO;
import com.laundrylink.model.LaundryOrder;
import com.laundrylink.model.OrderStatus;
import com.laundrylink.model.Staff;
import com.laundrylink.task.OrderProcessingTask;
import com.laundrylink.task.TaskManager;
import com.laundrylink.util.AlertUtil;
import com.laundrylink.util.UiUtil;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/** Select several pending orders and process them at the same time in the thread pool. */
public class BatchProcessTab extends Tab implements Refreshable {

    private final Staff staff;
    private final OrderDAO orderDAO = new OrderDAO();
    private final TableView<LaundryOrder> table = new TableView<>();
    private final TextArea logArea = new TextArea();

    public BatchProcessTab(Staff staff) {
        super("Batch Processing");
        this.staff = staff;
        setClosable(false);

        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        UiUtil.addColumn(table, "Order", o -> "#" + o.getId());
        UiUtil.addColumn(table, "Pickup", o -> o.getPickupDate() == null ? "-" : o.getPickupDate().toString());
        UiUtil.addColumn(table, "Pieces", o -> String.valueOf(o.getTotalQuantity()));
        UiUtil.addColumn(table, "Total", o -> UiUtil.money(o.calculateTotal()));
        UiUtil.bindColumnWidths(table, 0.20, 0.30, 0.20, 0.27);
        table.setPlaceholder(new Label("No pending orders"));

        logArea.setEditable(false);
        logArea.setPrefRowCount(8);

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> refresh());
        Button processBtn = new Button("Process Selected (Background)");
        processBtn.getStyleClass().add("primary-button");
        processBtn.setOnAction(e -> processSelected());

        VBox content = UiUtil.page(UiUtil.sectionTitle("Concurrent Order Processing"),
                new Label("Select one or more orders (Ctrl/Shift + click). The UI stays responsive while they run."),
                new HBox(10, refreshBtn, processBtn), table,
                UiUtil.sectionTitle("Activity log"), logArea);
        VBox.setVgrow(table, Priority.ALWAYS);
        setContent(content);
        refresh();
    }

    @Override
    public void refresh() {
        table.setItems(FXCollections.observableArrayList(orderDAO.findByStatus(OrderStatus.PENDING)));
    }

    private void processSelected() {
        List<LaundryOrder> selected = new ArrayList<>(table.getSelectionModel().getSelectedItems());
        if (selected.isEmpty()) {
            AlertUtil.error("No selection", "Select at least one pending order.");
            return;
        }
        log("Sending " + selected.size() + " order(s) to the thread pool...");
        for (LaundryOrder order : selected) {
            // Task runs the Callable on a pool thread; its callbacks run on the JavaFX thread
            Task<String> task = new Task<>() {
                @Override
                protected String call() throws Exception {
                    return new OrderProcessingTask(order.getId(), staff.getId()).call();
                }
            };
            task.setOnSucceeded(e -> {
                log(task.getValue());
                refresh();
            });
            task.setOnFailed(e -> log("Order #" + order.getId() + " failed: " + task.getException().getMessage()));
            TaskManager.execute(task);
        }
    }

    private void log(String message) {
        logArea.appendText(LocalTime.now().withNano(0) + "  " + message + "\n");
    }
}