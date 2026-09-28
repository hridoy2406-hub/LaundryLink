package com.laundrylink.dao;

import com.laundrylink.database.DatabaseConnection;
import com.laundrylink.model.Invoice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InvoiceDAO implements GenericDAO<Invoice> {

    @Override
    public boolean insert(Invoice invoice) {
        String sql = "INSERT INTO invoices (order_id, issue_date, subtotal, tax_rate, discount, paid) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, invoice.getOrderId());
            ps.setString(2, invoice.getIssueDate().toString());
            ps.setDouble(3, invoice.getSubtotal());
            ps.setDouble(4, invoice.getTaxRate());
            ps.setDouble(5, invoice.getDiscount());
            ps.setInt(6, invoice.isPaid() ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) invoice.setId(keys.getInt(1));
            }
            return true;
        } catch (SQLException e) {
            System.err.println("InvoiceDAO.insert: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Invoice findById(int id) {
        List<Invoice> list = query("SELECT * FROM invoices WHERE id = ?", id);
        return list.isEmpty() ? null : list.get(0);
    }

    public Invoice findByOrderId(int orderId) {
        List<Invoice> list = query("SELECT * FROM invoices WHERE order_id = ?", orderId);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<Invoice> findAll() {
        return query("SELECT * FROM invoices ORDER BY id DESC", null);
    }

    @Override
    public boolean update(Invoice invoice) {
        String sql = "UPDATE invoices SET subtotal=?, tax_rate=?, discount=?, paid=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, invoice.getSubtotal());
            ps.setDouble(2, invoice.getTaxRate());
            ps.setDouble(3, invoice.getDiscount());
            ps.setInt(4, invoice.isPaid() ? 1 : 0);
            ps.setInt(5, invoice.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("InvoiceDAO.update: " + e.getMessage());
            return false;
        }
    }

    public boolean markAsPaid(int invoiceId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE invoices SET paid = 1 WHERE id = ?")) {
            ps.setInt(1, invoiceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("InvoiceDAO.markAsPaid: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM invoices WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("InvoiceDAO.delete: " + e.getMessage());
            return false;
        }
    }

    private List<Invoice> query(String sql, Object param) {
        List<Invoice> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (param != null) ps.setObject(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Invoice inv = new Invoice(rs.getInt("order_id"), rs.getDouble("subtotal"),
                            rs.getDouble("tax_rate"), rs.getDouble("discount"));
                    inv.setId(rs.getInt("id"));
                    inv.setIssueDate(LocalDate.parse(rs.getString("issue_date")));
                    inv.setPaid(rs.getInt("paid") == 1);
                    list.add(inv);
                }
            }
        } catch (SQLException e) {
            System.err.println("InvoiceDAO.query: " + e.getMessage());
        }
        return list;
    }
}
