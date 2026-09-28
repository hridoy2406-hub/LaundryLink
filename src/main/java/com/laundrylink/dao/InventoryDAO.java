package com.laundrylink.dao;

import com.laundrylink.database.DatabaseConnection;
import com.laundrylink.model.InventoryItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InventoryDAO implements GenericDAO<InventoryItem> {

    @Override
    public boolean insert(InventoryItem item) {
        String sql = "INSERT INTO inventory_items (name, unit, quantity, reorder_level) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, item.getName());
            ps.setString(2, item.getUnit());
            ps.setDouble(3, item.getQuantity());
            ps.setDouble(4, item.getReorderLevel());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) item.setId(keys.getInt(1));
            }
            return true;
        } catch (SQLException e) {
            System.err.println("InventoryDAO.insert: " + e.getMessage());
            return false;
        }
    }

    @Override
    public InventoryItem findById(int id) {
        List<InventoryItem> list = query("SELECT * FROM inventory_items WHERE id = ?", id);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<InventoryItem> findAll() {
        return query("SELECT * FROM inventory_items ORDER BY id", null);
    }

    public List<InventoryItem> findLowStock() {
        return query("SELECT * FROM inventory_items WHERE quantity <= reorder_level ORDER BY id", null);
    }

    @Override
    public boolean update(InventoryItem item) {
        String sql = "UPDATE inventory_items SET name=?, unit=?, quantity=?, reorder_level=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getName());
            ps.setString(2, item.getUnit());
            ps.setDouble(3, item.getQuantity());
            ps.setDouble(4, item.getReorderLevel());
            ps.setInt(5, item.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("InventoryDAO.update: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM inventory_items WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("InventoryDAO.delete: " + e.getMessage());
            return false;
        }
    }

    /** Staff uses stock for an order: saves usage row and reduces stock in one transaction. */
    public boolean recordUsage(int orderId, int inventoryItemId, int staffId, double quantityUsed) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                double current;
                try (PreparedStatement check = conn.prepareStatement(
                        "SELECT quantity FROM inventory_items WHERE id = ?")) {
                    check.setInt(1, inventoryItemId);
                    try (ResultSet rs = check.executeQuery()) {
                        if (!rs.next()) return false;
                        current = rs.getDouble(1);
                    }
                }
                if (current < quantityUsed) {
                    conn.rollback();
                    return false; // not enough stock
                }
                try (PreparedStatement usage = conn.prepareStatement(
                        "INSERT INTO inventory_usage (order_id, inventory_item_id, staff_id, quantity_used, used_on) "
                                + "VALUES (?, ?, ?, ?, ?)")) {
                    usage.setInt(1, orderId);
                    usage.setInt(2, inventoryItemId);
                    usage.setInt(3, staffId);
                    usage.setDouble(4, quantityUsed);
                    usage.setString(5, LocalDate.now().toString());
                    usage.executeUpdate();
                }
                try (PreparedStatement stock = conn.prepareStatement(
                        "UPDATE inventory_items SET quantity = quantity - ? WHERE id = ?")) {
                    stock.setDouble(1, quantityUsed);
                    stock.setInt(2, inventoryItemId);
                    stock.executeUpdate();
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("InventoryDAO.recordUsage: " + e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("InventoryDAO.recordUsage (connection): " + e.getMessage());
            return false;
        }
    }

    private List<InventoryItem> query(String sql, Object param) {
        List<InventoryItem> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (param != null) ps.setObject(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InventoryItem item = new InventoryItem(rs.getString("name"), rs.getString("unit"),
                            rs.getDouble("quantity"), rs.getDouble("reorder_level"));
                    item.setId(rs.getInt("id"));
                    list.add(item);
                }
            }
        } catch (SQLException e) {
            System.err.println("InventoryDAO.query: " + e.getMessage());
        }
        return list;
    }
}
