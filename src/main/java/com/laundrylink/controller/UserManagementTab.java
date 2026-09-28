package com.laundrylink.controller;

import com.laundrylink.dao.UserDAO;
import com.laundrylink.model.Admin;
import com.laundrylink.model.Customer;
import com.laundrylink.model.Role;
import com.laundrylink.model.Staff;
import com.laundrylink.model.User;
import com.laundrylink.util.AlertUtil;
import com.laundrylink.util.Session;
import com.laundrylink.util.UiUtil;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class UserManagementTab extends Tab implements Refreshable {

    private final UserDAO userDAO = new UserDAO();
    private final TableView<User> table = new TableView<>();

    public UserManagementTab() {
        super("Users");
        setClosable(false);

        UiUtil.addColumn(table, "ID", u -> String.valueOf(u.getId()));
        UiUtil.addColumn(table, "Name", User::getFullName);
        UiUtil.addColumn(table, "Username", User::getUsername);
        UiUtil.addColumn(table, "Role", u -> u.getRole().toString());
        UiUtil.addColumn(table, "Email", u -> nz(u.getEmail()));
        UiUtil.addColumn(table, "Phone", u -> nz(u.getPhone()));
        UiUtil.addColumn(table, "Details", this::details);
        UiUtil.bindColumnWidths(table, 0.06, 0.16, 0.13, 0.09, 0.18, 0.13, 0.22);

        Button addBtn = new Button("Add User");
        addBtn.getStyleClass().add("primary-button");
        addBtn.setOnAction(e -> showUserDialog(null));
        Button editBtn = new Button("Edit Selected");
        editBtn.setOnAction(e -> {
            User selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) AlertUtil.error("No selection", "Select a user first.");
            else showUserDialog(selected);
        });
        Button deleteBtn = new Button("Delete Selected");
        deleteBtn.getStyleClass().add("danger-button");
        deleteBtn.setOnAction(e -> deleteSelected());
        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> refresh());

        VBox content = UiUtil.page(UiUtil.sectionTitle("User Management"),
                new HBox(10, addBtn, editBtn, deleteBtn, refreshBtn), table);
        VBox.setVgrow(table, Priority.ALWAYS);
        setContent(content);
        refresh();
    }

    @Override
    public void refresh() {
        table.setItems(FXCollections.observableArrayList(userDAO.findAll()));
    }

    private void deleteSelected() {
        User selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.error("No selection", "Select a user first.");
            return;
        }
        if (selected.getId() == Session.getCurrentUser().getId()) {
            AlertUtil.error("Not allowed", "You cannot delete the account you are logged in with.");
            return;
        }
        if (AlertUtil.confirm("Delete user", "Delete " + selected.getFullName()
                + "? A customer's orders will be deleted too.")) {
            userDAO.delete(selected.getId());
            refresh();
        }
    }

    private void showUserDialog(User existing) {
        boolean editing = existing != null;
        TextField nameF = new TextField(editing ? existing.getFullName() : "");
        TextField userF = new TextField(editing ? existing.getUsername() : "");
        PasswordField passF = new PasswordField();
        if (editing) passF.setPromptText("Leave empty to keep current");
        TextField emailF = new TextField(editing ? nz(existing.getEmail()) : "");
        TextField phoneF = new TextField(editing ? nz(existing.getPhone()) : "");
        ComboBox<Role> roleBox = new ComboBox<>(FXCollections.observableArrayList(Role.values()));
        roleBox.setValue(editing ? existing.getRole() : Role.STAFF);
        TextField extraF = new TextField(editing ? extraValue(existing) : "");
        Label extraLabel = new Label();
        Label msg = new Label();
        msg.getStyleClass().add("error-label");

        Runnable updateLabel = () -> extraLabel.setText(switch (roleBox.getValue()) {
            case CUSTOMER -> "Address";
            case STAFF -> "Designation";
            case ADMIN -> "Access level (1-3)";
        });
        roleBox.valueProperty().addListener((obs, o, n) -> updateLabel.run());
        updateLabel.run();
        if (editing) {
            userF.setDisable(true);
            roleBox.setDisable(true);
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.addRow(0, new Label("Full name"), nameF);
        grid.addRow(1, new Label("Username"), userF);
        grid.addRow(2, new Label("Password"), passF);
        grid.addRow(3, new Label("Email"), emailF);
        grid.addRow(4, new Label("Phone"), phoneF);
        grid.addRow(5, new Label("Role"), roleBox);
        grid.addRow(6, extraLabel, extraF);
        grid.add(msg, 0, 7, 2, 1);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(editing ? "Edit User" : "Add User");
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                String extra = extraF.getText().trim();
                if (editing) {
                    existing.setFullName(nameF.getText());
                    existing.setEmail(emailF.getText());
                    existing.setPhone(phoneF.getText());
                    if (!passF.getText().isEmpty()) existing.setPassword(passF.getText());
                    if (existing instanceof Customer c) c.setAddress(extra);
                    else if (existing instanceof Staff s) s.setDesignation(extra);
                    else if (existing instanceof Admin a) a.setAccessLevel(Integer.parseInt(extra));
                    if (!userDAO.update(existing)) throw new IllegalArgumentException("Could not save changes");
                } else {
                    if (userDAO.findByUsername(userF.getText().trim()) != null) {
                        throw new IllegalArgumentException("Username already taken");
                    }
                    User created = switch (roleBox.getValue()) {
                        case CUSTOMER -> new Customer(nameF.getText(), userF.getText(), passF.getText(),
                                emailF.getText(), phoneF.getText(), extra);
                        case STAFF -> new Staff(nameF.getText(), userF.getText(), passF.getText(),
                                emailF.getText(), phoneF.getText(), extra.isEmpty() ? "Staff" : extra, "Morning");
                        case ADMIN -> new Admin(nameF.getText(), userF.getText(), passF.getText(),
                                emailF.getText(), phoneF.getText(), extra.isEmpty() ? 1 : Integer.parseInt(extra));
                    };
                    if (!userDAO.insert(created)) throw new IllegalArgumentException("Could not save user");
                }
            } catch (NumberFormatException ex) {
                msg.setText("Access level must be a number (1-3)");
                event.consume();
            } catch (IllegalArgumentException ex) {
                msg.setText(ex.getMessage());
                event.consume();
            }
        });

        dialog.showAndWait();
        refresh();
    }

    private String details(User u) {
        if (u instanceof Customer c) return nz(c.getAddress());
        if (u instanceof Staff s) return nz(s.getDesignation()) + " / " + nz(s.getShift());
        if (u instanceof Admin a) return "Access level " + a.getAccessLevel();
        return "";
    }

    private String extraValue(User u) {
        if (u instanceof Customer c) return nz(c.getAddress());
        if (u instanceof Staff s) return nz(s.getDesignation());
        if (u instanceof Admin a) return String.valueOf(a.getAccessLevel());
        return "";
    }

    private String nz(String s) {
        return s == null ? "" : s;
    }
}