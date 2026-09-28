package com.laundrylink.controller;

import com.laundrylink.dao.InventoryDAO;
import com.laundrylink.model.InventoryItem;
import com.laundrylink.util.AlertUtil;
import com.laundrylink.util.UiUtil;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class InventoryTab extends Tab implements Refreshable {

    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final TableView<InventoryItem> table = new TableView<>();

    public InventoryTab() {
        super("Inventory");
        setClosable(false);

        UiUtil.addColumn(table, "ID", i -> String.valueOf(i.getId()));
        UiUtil.addColumn(table, "Name", InventoryItem::getName);
        UiUtil.addColumn(table, "Unit", i -> i.getUnit() == null ? "" : i.getUnit());
        UiUtil.addColumn(table, "Quantity", i -> String.valueOf(i.getQuantity()));
        UiUtil.addColumn(table, "Reorder Level", i -> String.valueOf(i.getReorderLevel()));
        UiUtil.addColumn(table, "Status", i -> i.isLowStock() ? "LOW STOCK" : "OK");
        UiUtil.bindColumnWidths(table, 0.07, 0.27, 0.12, 0.15, 0.18, 0.18);

        // low stock rows are shown in red
        table.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(InventoryItem item, boolean empty) {
                super.updateItem(item, empty);
                setStyle(!empty && item != null && item.isLowStock() ? "-fx-background-color: #fee2e2;" : "");
            }
        });

        Button addBtn = new Button("Add Item");
        addBtn.getStyleClass().add("primary-button");
        addBtn.setOnAction(e -> showDialog(null));
        Button editBtn = new Button("Edit Selected");
        editBtn.setOnAction(e -> {
            InventoryItem item = selected();
            if (item != null) showDialog(item);
        });
        Button restockBtn = new Button("Add Stock");
        restockBtn.setOnAction(e -> restock());
        Button deleteBtn = new Button("Delete Selected");
        deleteBtn.getStyleClass().add("danger-button");
        deleteBtn.setOnAction(e -> {
            InventoryItem item = selected();
            if (item != null && AlertUtil.confirm("Delete", "Delete " + item.getName() + "?")) {
                inventoryDAO.delete(item.getId());
                refresh();
            }
        });
        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> refresh());

        VBox content = UiUtil.page(UiUtil.sectionTitle("Inventory Management"),
                new HBox(10, addBtn, editBtn, restockBtn, deleteBtn, refreshBtn), table);
        VBox.setVgrow(table, Priority.ALWAYS);
        setContent(content);
        refresh();
    }

    @Override
    public void refresh() {
        table.setItems(FXCollections.observableArrayList(inventoryDAO.findAll()));
    }

    private InventoryItem selected() {
        InventoryItem item = table.getSelectionModel().getSelectedItem();
        if (item == null) AlertUtil.error("No selection", "Select an item first.");
        return item;
    }

    private void restock() {
        InventoryItem item = selected();
        if (item == null) return;
        TextInputDialog input = new TextInputDialog("10");
        input.setTitle("Add stock");
        input.setHeaderText(null);
        input.setContentText("Amount to add to " + item.getName() + ":");
        input.showAndWait().ifPresent(text -> {
            try {
                item.addStock(Double.parseDouble(text.trim()));
                inventoryDAO.update(item);
                refresh();
            } catch (IllegalArgumentException e) {
                AlertUtil.error("Invalid amount", "Enter a number above 0.");
            }
        });
    }

    private void showDialog(InventoryItem existing) {
        boolean editing = existing != null;
        TextField nameF = new TextField(editing ? existing.getName() : "");
        TextField unitF = new TextField(editing && existing.getUnit() != null ? existing.getUnit() : "");
        TextField qtyF = new TextField(editing ? String.valueOf(existing.getQuantity()) : "0");
        TextField reorderF = new TextField(editing ? String.valueOf(existing.getReorderLevel()) : "10");
        Label msg = new Label();
        msg.getStyleClass().add("error-label");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.addRow(0, new Label("Name"), nameF);
        grid.addRow(1, new Label("Unit (kg, liter, piece)"), unitF);
        grid.addRow(2, new Label("Quantity"), qtyF);
        grid.addRow(3, new Label("Reorder level"), reorderF);
        grid.add(msg, 0, 4, 2, 1);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(editing ? "Edit Item" : "Add Item");
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                double qty = Double.parseDouble(qtyF.getText().trim());
                double reorder = Double.parseDouble(reorderF.getText().trim());
                boolean saved;
                if (editing) {
                    existing.setName(nameF.getText());
                    existing.setUnit(unitF.getText());
                    existing.setQuantity(qty);
                    existing.setReorderLevel(reorder);
                    saved = inventoryDAO.update(existing);
                } else {
                    saved = inventoryDAO.insert(new InventoryItem(nameF.getText(), unitF.getText(), qty, reorder));
                }
                if (!saved) throw new IllegalArgumentException("Could not save (name already exists?)");
            } catch (NumberFormatException ex) {
                msg.setText("Quantity and reorder level must be numbers");
                event.consume();
            } catch (IllegalArgumentException ex) {
                msg.setText(ex.getMessage());
                event.consume();
            }
        });

        dialog.showAndWait();
        refresh();
    }
}