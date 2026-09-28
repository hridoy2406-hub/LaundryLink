package com.laundrylink.controller;

import com.laundrylink.dao.UserDAO;
import com.laundrylink.model.Customer;
import com.laundrylink.model.User;
import com.laundrylink.util.AlertUtil;
import com.laundrylink.util.SceneNavigator;
import com.laundrylink.util.Session;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class LoginController {

    @FXML private StackPane root;
    @FXML private VBox card;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    private void initialize() {
        // Responsive: the card is 40% of the window width, but never narrower than 320
        card.maxWidthProperty().bind(Bindings.max(320.0, root.widthProperty().multiply(0.4)));
        // Error label only takes space when it has text
        errorLabel.visibleProperty().bind(errorLabel.textProperty().isNotEmpty());
        errorLabel.managedProperty().bind(errorLabel.visibleProperty());
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter username and password");
            return;
        }
        User user = userDAO.authenticate(username, password);
        if (user == null) {
            errorLabel.setText("Invalid username or password");
            passwordField.clear();
            return;
        }
        Session.setCurrentUser(user);
        SceneNavigator.showDashboard(user);
    }

    @FXML
    private void handleRegister() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Customer Registration");

        TextField nameF = new TextField();
        TextField userF = new TextField();
        PasswordField passF = new PasswordField();
        TextField emailF = new TextField();
        TextField phoneF = new TextField();
        TextField addressF = new TextField();
        Label msg = new Label();
        msg.getStyleClass().add("error-label");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.addRow(0, new Label("Full name"), nameF);
        grid.addRow(1, new Label("Username"), userF);
        grid.addRow(2, new Label("Password"), passF);
        grid.addRow(3, new Label("Email"), emailF);
        grid.addRow(4, new Label("Phone"), phoneF);
        grid.addRow(5, new Label("Address"), addressF);
        grid.add(msg, 0, 6, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        boolean[] registered = {false};
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                if (userDAO.findByUsername(userF.getText().trim()) != null) {
                    throw new IllegalArgumentException("Username already taken");
                }
                Customer customer = new Customer(nameF.getText(), userF.getText(), passF.getText(),
                        emailF.getText(), phoneF.getText(), addressF.getText());
                if (!userDAO.insert(customer)) {
                    throw new IllegalArgumentException("Could not save the account");
                }
                registered[0] = true;
            } catch (IllegalArgumentException ex) {
                msg.setText(ex.getMessage());
                event.consume(); // keep the dialog open
            }
        });

        dialog.showAndWait();
        if (registered[0]) {
            usernameField.setText(userF.getText().trim());
            passwordField.clear();
            AlertUtil.info("Registration", "Account created. You can log in now.");
        }
    }
}
