package com.laundrylink.util;

import com.laundrylink.database.DatabaseConnection;
import com.laundrylink.database.DatabaseInitializer;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Console demo that proves the tables and relationships work. Run this class directly. */
public class DatabaseDemo {

    public static void main(String[] args) {
        DatabaseInitializer.initialize();
        System.out.println("Database file: " + new File(DatabaseConnection.DB_FILE).getAbsolutePath());

        try (Connection conn = DatabaseConnection.getConnection()) {
            printTableCounts(conn);
            printUsers(conn);
            testForeignKey(conn);
            testCascadeDelete(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void printTableCounts(Connection conn) throws SQLException {
        System.out.println("\n=== Tables and row counts ===");
        String[] tables = {"users", "orders", "order_items", "invoices", "inventory_items", "inventory_usage"};
        for (String table : tables) {
            System.out.println("   " + table + " : " + queryInt(conn, "SELECT COUNT(*) FROM " + table));
        }
    }

    private static void printUsers(Connection conn) throws SQLException {
        System.out.println("\n=== Default users ===");
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, full_name, username, role FROM users")) {
            while (rs.next()) {
                System.out.println("   #" + rs.getInt("id") + " " + rs.getString("username")
                        + " (" + rs.getString("role") + ") - " + rs.getString("full_name"));
            }
        }
    }

    // An order for a customer that does not exist must be rejected by the database.
    private static void testForeignKey(Connection conn) {
        System.out.println("\n=== Foreign key test ===");
        String sql = "INSERT INTO orders (customer_id, order_date, status) VALUES (9999, '2026-01-01', 'PENDING')";
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("PROBLEM: foreign keys are NOT enforced");
            stmt.executeUpdate("DELETE FROM orders WHERE customer_id = 9999"); // clean up
        } catch (SQLException e) {
            System.out.println("Good: the database rejected an order for a customer that does not exist");
            System.out.println("   Reason: " + e.getMessage());
        }
    }

    // Deleting an order must also delete its items (ON DELETE CASCADE).
    private static void testCascadeDelete(Connection conn) throws SQLException {
        System.out.println("\n=== Cascade delete test: deleting an order deletes its items ===");

        int customerId = queryInt(conn, "SELECT id FROM users WHERE role = 'CUSTOMER' LIMIT 1");

        int orderId;
        String insertOrder = "INSERT INTO orders (customer_id, order_date, status) VALUES (?, '2026-01-01', 'PENDING')";
        try (PreparedStatement ps = conn.prepareStatement(insertOrder, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, customerId);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                orderId = keys.getInt(1);
            }
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("INSERT INTO order_items (order_id, item_name, service_type, quantity, unit_price) "
                    + "VALUES (" + orderId + ", 'Shirt', 'WASH', 2, 30)");
            stmt.executeUpdate("INSERT INTO order_items (order_id, item_name, service_type, quantity, unit_price) "
                    + "VALUES (" + orderId + ", 'Pant', 'IRON', 1, 20)");
        }

        String countItems = "SELECT COUNT(*) FROM order_items WHERE order_id = " + orderId;
        System.out.println("   Items before delete: " + queryInt(conn, countItems));

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM orders WHERE id = " + orderId);
        }

        System.out.println("   Items after delete : " + queryInt(conn, countItems));
    }

    private static int queryInt(Connection conn, String sql) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}