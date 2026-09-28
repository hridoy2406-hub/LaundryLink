package com.laundrylink.controller;

import com.laundrylink.dao.InvoiceDAO;
import com.laundrylink.model.Invoice;
import com.laundrylink.util.AlertUtil;
import com.laundrylink.util.UiUtil;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

public class BillingTab extends Tab implements Refreshable {

    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private final TableView<Invoice> table = new TableView<>();
    private final Label summary = new Label();

    public BillingTab() {
        super("Billing");
        setClosable(false);

        UiUtil.addColumn(table, "Invoice", i -> "#" + i.getId());
        UiUtil.addColumn(table, "Order", i -> "#" + i.getOrderId());
        UiUtil.addColumn(table, "Date", i -> i.getIssueDate().toString());
        UiUtil.addColumn(table, "Subtotal", i -> UiUtil.money(i.getSubtotal()));
        UiUtil.addColumn(table, "Tax", i -> UiUtil.money(i.calculateTaxAmount()));
        UiUtil.addColumn(table, "Discount", i -> UiUtil.money(i.getDiscount()));
        UiUtil.addColumn(table, "Total", i -> UiUtil.money(i.calculateTotal()));
        UiUtil.addColumn(table, "Payment", i -> i.isPaid() ? "PAID" : "UNPAID");
        UiUtil.bindColumnWidths(table, 0.09, 0.09, 0.13, 0.13, 0.11, 0.12, 0.14, 0.13);

        Button paidBtn = new Button("Mark as Paid");
        paidBtn.getStyleClass().add("primary-button");
        paidBtn.setOnAction(e -> {
            Invoice invoice = selected();
            if (invoice != null) {
                invoiceDAO.markAsPaid(invoice.getId());
                refresh();
            }
        });
        Button deleteBtn = new Button("Delete Invoice");
        deleteBtn.getStyleClass().add("danger-button");
        deleteBtn.setOnAction(e -> {
            Invoice invoice = selected();
            if (invoice != null && AlertUtil.confirm("Delete", "Delete invoice #" + invoice.getId() + "?")) {
                invoiceDAO.delete(invoice.getId());
                refresh();
            }
        });
        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> refresh());

        VBox content = UiUtil.page(UiUtil.sectionTitle("Billing"), summary,
                new HBox(10, paidBtn, deleteBtn, refreshBtn), table);
        VBox.setVgrow(table, Priority.ALWAYS);
        setContent(content);
        refresh();
    }

    @Override
    public void refresh() {
        List<Invoice> invoices = invoiceDAO.findAll();
        table.setItems(FXCollections.observableArrayList(invoices));
        double paid = invoices.stream().filter(Invoice::isPaid).mapToDouble(Invoice::calculateTotal).sum();
        double unpaid = invoices.stream().filter(i -> !i.isPaid()).mapToDouble(Invoice::calculateTotal).sum();
        summary.setText("Collected: " + UiUtil.money(paid) + "     Unpaid: " + UiUtil.money(unpaid));
    }

    private Invoice selected() {
        Invoice invoice = table.getSelectionModel().getSelectedItem();
        if (invoice == null) AlertUtil.error("No selection", "Select an invoice first.");
        return invoice;
    }
}