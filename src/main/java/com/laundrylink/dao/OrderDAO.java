package com.laundrylink.dao;

import com.laundrylink.database.DatabaseConnection;
import com.laundrylink.model.LaundryOrder;
import com.laundrylink.model.OrderItem;
import com.laundrylink.model.OrderStatus;
import com.laundrylink.model.ServiceType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class OrderDAO implements GenericDAO<LaundryOrder> {

    /** Saves the order and all its items in one transaction. */
    @Override
    public boolean insert(LaundryOrder order) {
        String orderSql = "INSERT INTO orders (customer_id, staff_id, order_date, pickup_date, status, notes) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        String itemSql = "INSERT INTO order_items (order_id, item_name, service_type, quantity, unit_price) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, order.getCustomerId());
                ps.setObject(2, order.getStaffId() == 0 ? null : order.getStaffId());
                ps.setString(3, order.getOrderDate().toString());
                ps.setString(4, order.getPickupDate() == null ? null : order.getPickupDate().toString());
                ps.setString(5, order.getStatus().name());
                ps.setString(6, order.getNotes());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) order.setId(keys.getInt(1));
                }
                try (PreparedStatement itemPs = conn.prepareStatement(itemSql, Statement.RETURN_GENERATED_KEYS)) {
                    for (OrderItem item : order.getItems()) {
                        itemPs.setInt(1, order.getId());
                        itemPs.setString(2, item.getItemName());
                        itemPs.setString(3, item.getServiceType().name());
                        itemPs.setInt(4, item.getQuantity());
                        itemPs.setDouble(5, item.getUnitPrice());
                        itemPs.executeUpdate();
                        try (ResultSet k = itemPs.getGeneratedKeys()) {
                            if (k.next()) item.setId(k.getInt(1));
                        }
                        item.setOrderId(order.getId());
                    }
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("OrderDAO.insert: " + e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("OrderDAO.insert (connection): " + e.getMessage());
            return false;
        }
    }

    @Override
    public LaundryOrder findById(int id) {
        List<LaundryOrder> list = query("SELECT * FROM orders WHERE id = ?", id);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<LaundryOrder> findAll() {
        return query("SELECT * FROM orders ORDER BY id DESC", null);
    }

    /** Customer order history. */
    public List<LaundryOrder> findByCustomerId(int customerId) {
        return query("SELECT * FROM orders WHERE customer_id = ? ORDER BY id DESC", customerId);
    }

    public List<LaundryOrder> findByStatus(OrderStatus status) {
        return query("SELECT * FROM orders WHERE status = ? ORDER BY id", status.name());
    }

    @Override
    public boolean update(LaundryOrder order) {
        String sql = "UPDATE orders SET staff_id=?, pickup_date=?, status=?, notes=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, order.getStaffId() == 0 ? null : order.getStaffId());
            ps.setString(2, order.getPickupDate() == null ? null : order.getPickupDate().toString());
            ps.setString(3, order.getStatus().name());
            ps.setString(4, order.getNotes());
            ps.setInt(5, order.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("OrderDAO.update: " + e.getMessage());
            return false;
        }
    }

    /** Changes only the status. If staffId is null the assigned staff stays as it is. */
    public boolean updateStatus(int orderId, OrderStatus status, Integer staffId) {
        String sql = "UPDATE orders SET status = ?, staff_id = COALESCE(?, staff_id) WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setObject(2, staffId);
            ps.setInt(3, orderId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("OrderDAO.updateStatus: " + e.getMessage());
            return false;
        }
    }

    /** Items and invoice are deleted automatically (ON DELETE CASCADE). */
    @Override
    public boolean delete(int id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM orders WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("OrderDAO.delete: " + e.getMessage());
            return false;
        }
    }

    /** For reports: number of orders in every status. */
    public Map<OrderStatus, Integer> countByStatus() {
        Map<OrderStatus, Integer> map = new EnumMap<>(OrderStatus.class);
        for (OrderStatus s : OrderStatus.values()) map.put(s, 0);
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT status, COUNT(*) FROM orders GROUP BY status")) {
            while (rs.next()) map.put(OrderStatus.valueOf(rs.getString(1)), rs.getInt(2));
        } catch (SQLException e) {
            System.err.println("OrderDAO.countByStatus: " + e.getMessage());
        }
        return map;
    }

    // ---------- helpers ----------

    private List<LaundryOrder> query(String sql, Object param) {
        List<LaundryOrder> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (param != null) ps.setObject(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapOrder(rs));
            }
            for (LaundryOrder order : list) loadItems(conn, order);
        } catch (SQLException e) {
            System.err.println("OrderDAO.query: " + e.getMessage());
        }
        return list;
    }

    private LaundryOrder mapOrder(ResultSet rs) throws SQLException {
        String pickup = rs.getString("pickup_date");
        LaundryOrder order = new LaundryOrder(rs.getInt("customer_id"),
                pickup == null ? null : LocalDate.parse(pickup), rs.getString("notes"));
        order.setId(rs.getInt("id"));
        order.setStaffId(rs.getInt("staff_id")); // NULL becomes 0 = not assigned
        order.setOrderDate(LocalDate.parse(rs.getString("order_date")));
        order.setStatus(OrderStatus.valueOf(rs.getString("status")));
        return order;
    }

    private void loadItems(Connection conn, LaundryOrder order) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM order_items WHERE order_id = ?")) {
            ps.setInt(1, order.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem(rs.getString("item_name"),
                            ServiceType.valueOf(rs.getString("service_type")),
                            rs.getInt("quantity"), rs.getDouble("unit_price"));
                    item.setId(rs.getInt("id"));
                    item.setOrderId(order.getId());
                    order.addItem(item);
                }
            }
        }
    }
}