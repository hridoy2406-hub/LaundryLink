package com.laundrylink.controller;

import com.laundrylink.dao.InventoryDAO;
import com.laundrylink.dao.InvoiceDAO;
import com.laundrylink.dao.OrderDAO;
import com.laundrylink.dao.UserDAO;
import com.laundrylink.model.Invoice;
import com.laundrylink.model.OrderStatus;
import com.laundrylink.model.Role;
import com.laundrylink.util.UiUtil;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;

public class ReportsTab extends Tab implements Refreshable {

    private final OrderDAO orderDAO = new OrderDAO();
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final UserDAO userDAO = new UserDAO();

    private final Label totalOrders = new Label();
    private final Label activeOrders = new Label();
    private final Label customers = new Label();
    private final Label revenue = new Label();
    private final Label unpaid = new Label();
    private final Label lowStock = new Label();
    private final BarChart<String, Number> barChart;
    private final PieChart pieChart = new PieChart();

    public ReportsTab() {
        super("Reports");
        setClosable(false);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Orders");
        barChart = new BarChart<>(new CategoryAxis(), yAxis);
        barChart.setTitle("Orders by Status");
        barChart.setLegendVisible(false);
        barChart.setAnimated(false);
        barChart.setPrefHeight(320);
        pieChart.setTitle("Payments (Tk)");
        pieChart.setAnimated(false);
        pieChart.setPrefHeight(320);

        HBox charts = new HBox(15, barChart, pieChart);
        HBox.setHgrow(barChart, Priority.ALWAYS);
        HBox.setHgrow(pieChart, Priority.ALWAYS);

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> refresh());

        VBox content = UiUtil.page(UiUtil.sectionTitle("Reports & Analytics"), refreshBtn,
                UiUtil.cardGrid(3,
                        UiUtil.statCard("Total Orders", totalOrders),
                        UiUtil.statCard("Active Orders", activeOrders),
                        UiUtil.statCard("Customers", customers),
                        UiUtil.statCard("Revenue Collected", revenue),
                        UiUtil.statCard("Payments Pending", unpaid),
                        UiUtil.statCard("Low Stock Items", lowStock)),
                charts);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        setContent(scrollPane);
        refresh();
    }

    @Override
    public void refresh() {
        Map<OrderStatus, Integer> counts = orderDAO.countByStatus();
        int total = counts.values().stream().mapToInt(Integer::intValue).sum();
        int active = counts.get(OrderStatus.PENDING) + counts.get(OrderStatus.PROCESSING) + counts.get(OrderStatus.READY);

        List<Invoice> invoices = invoiceDAO.findAll();
        double paid = invoices.stream().filter(Invoice::isPaid).mapToDouble(Invoice::calculateTotal).sum();
        double notPaid = invoices.stream().filter(i -> !i.isPaid()).mapToDouble(Invoice::calculateTotal).sum();

        totalOrders.setText(String.valueOf(total));
        activeOrders.setText(String.valueOf(active));
        customers.setText(String.valueOf(userDAO.findByRole(Role.CUSTOMER).size()));
        revenue.setText(UiUtil.money(paid));
        unpaid.setText(UiUtil.money(notPaid));
        lowStock.setText(String.valueOf(inventoryDAO.findLowStock().size()));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<OrderStatus, Integer> entry : counts.entrySet()) {
            series.getData().add(new XYChart.Data<String, Number>(entry.getKey().getDisplayName(), entry.getValue()));
        }
        barChart.getData().clear();
        barChart.getData().add(series);

        pieChart.getData().setAll(new PieChart.Data("Paid", paid), new PieChart.Data("Unpaid", notPaid));
    }
}