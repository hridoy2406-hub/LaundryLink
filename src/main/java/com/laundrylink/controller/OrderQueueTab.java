package com.laundrylink.controller;

import com.laundrylink.dao.InventoryDAO;
import com.laundrylink.dao.OrderDAO;
import com.laundrylink.dao.UserDAO;
import com.laundrylink.model.InventoryItem;
import com.laundrylink.model.LaundryOrder;
import com.laundrylink.model.OrderStatus;
import com.laundrylink.model.Role;
import com.laundrylink.model.Staff;
import com.laundrylink.model.User;
import com.laundrylink.util.AlertUtil;
import com.laundrylink.util.UiUtil;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Shows all orders of one status and lets staff move them to the next status. */
public class OrderQueueTab extends Tab implements Refreshable {

    private final Staff staff;
    private final OrderStatus status;
    private final OrderDAO orderDAO = new OrderDAO();
    private final UserDAO userDAO = new UserDAO();
    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final TableView<LaundryOrder> table = new TableView<>();
    private final Map<Integer, String> customerNames = new HashMap<>();

    public OrderQueueTab(Staff staff, OrderStatus status) {
        super(status.getDisplayName() + " Orders");
        this.staff = staff;
        this.status = status;
        setClosable(false);

        UiUtil.addColumn(table, "Order", o -> "#" + o.getId());
        UiUtil.addColumn(table, "Customer", o -> customerNames.getOrDefault(o.getCustomerId(), "#" + o.getCustomerId()));
        UiUtil.addColumn(table, "Pickup", o -> o.getPickupDate() == null ? "-" : o.getPickupDate().toString());
        UiUtil.addColumn(table, "Pieces", o -> String.valueOf(o.getTotalQuantity()));
        UiUtil.addColumn(table, "Total", o -> UiUtil.money(o.calculateTotal()));
        UiUtil.addColumn(table, "Notes", o -> o.getNotes() == null ? "" : o.getNotes());
        UiUtil.bindColumnWidths(table, 0.10, 0.20, 0.15, 0.10, 0.15, 0.27);
        table.setPlaceholder(new Label("No " + status.getDisplayName().toLowerCase() + " orders"));

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> refresh());
        Button actionBtn = new Button(actionText());
        actionBtn.getStyleClass().add("primary-button");
        actionBtn.setOnAction(e -> moveToNext());
        HBox buttons = new HBox(10, refreshBtn, actionBtn);

        if (status == OrderStatus.PROCESSING) {
            Button inventoryBtn = new Button("Use Inventory");
            inventoryBtn.setOnAction(e -> useInventory());
            buttons.getChildren().add(inventoryBtn);
        }

        VBox content = UiUtil.page(UiUtil.sectionTitle(status.getDisplayName() + " Orders"), buttons, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        setContent(content);
        refresh();
    }

    private String actionText() {
        return switch (status) {
            case PENDING -> "Start Processing";
            case PROCESSING -> "Mark as Ready";
            case READY -> "Mark as Delivered";
            default -> "Update";
        };
    }

    @Override
    public void refresh() {
        customerNames.clear();
        for (User u : userDAO.findByRole(Role.CUSTOMER)) customerNames.put(u.getId(), u.getFullName());
        table.setItems(FXCollections.observableArrayList(orderDAO.findByStatus(status)));
    }

    private LaundryOrder selectedOrder() {
        LaundryOrder order = table.getSelectionModel().getSelectedItem();
        if (order == null) AlertUtil.error("No selection", "Please select an order first.");
        return order;
    }

    private void moveToNext() {
        LaundryOrder order = selectedOrder();
        if (order == null) return;
        orderDAO.updateStatus(order.getId(), status.next(), staff.getId());
        refresh();
    }

    private void useInventory() {
        LaundryOrder order = selectedOrder();
        if (order == null) return;

        ComboBox<InventoryItem> itemBox = new ComboBox<>(FXCollections.observableArrayList(inventoryDAO.findAll()));
        itemBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(InventoryItem item) {
                return item == null ? "" : item.getSummary();
            }

            @Override
            public InventoryItem fromString(String s) {
                return null;
            }
        });
        itemBox.getSelectionModel().selectFirst();
        TextField qtyField = new TextField("1");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.addRow(0, new Label("Item"), itemBox);
        grid.addRow(1, new Label("Quantity used"), qtyField);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Use inventory for order #" + order.getId());
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                double qty = Double.parseDouble(qtyField.getText().trim());
                InventoryItem item = itemBox.getValue();
                if (item == null || qty <= 0) {
                    AlertUtil.error("Invalid input", "Choose an item and a quantity above 0.");
                } else if (inventoryDAO.recordUsage(order.getId(), item.getId(), staff.getId(), qty)) {
                    AlertUtil.info("Saved", qty + " " + item.getUnit() + " of " + item.getName() + " used.");
                } else {
                    AlertUtil.error("Not enough stock", "Only " + item.getQuantity() + " " + item.getUnit() + " left.");
                }
            } catch (NumberFormatException e) {
                AlertUtil.error("Invalid input", "Quantity must be a number.");
            }
        }
    }
}