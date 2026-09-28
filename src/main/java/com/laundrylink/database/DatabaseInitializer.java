package com.laundrylink.database;

import com.laundrylink.model.Role;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Creates all tables (if they don't exist yet) and inserts default data.
 * Safe to call every time the app starts.
 */
public class DatabaseInitializer {

    // ---------------- Table definitions ----------------

    private static final String CREATE_USERS = """
            CREATE TABLE IF NOT EXISTS users (
                id            INTEGER PRIMARY KEY AUTOINCREMENT,
                full_name     TEXT NOT NULL,
                username      TEXT NOT NULL UNIQUE,
                password      TEXT NOT NULL,
                email         TEXT,
                phone         TEXT,
                role          TEXT NOT NULL CHECK (role IN ('CUSTOMER', 'STAFF', 'ADMIN')),
                address       TEXT,     -- Customer only
                designation   TEXT,     -- Staff only
                shift         TEXT,     -- Staff only
                access_level  INTEGER   -- Admin only
            )
            """;

    private static final String CREATE_ORDERS = """
            CREATE TABLE IF NOT EXISTS orders (
                id           INTEGER PRIMARY KEY AUTOINCREMENT,
                customer_id  INTEGER NOT NULL,
                staff_id     INTEGER,
                order_date   TEXT NOT NULL,
                pickup_date  TEXT,
                status       TEXT NOT NULL DEFAULT 'PENDING'
                             CHECK (status IN ('PENDING', 'PROCESSING', 'READY', 'DELIVERED', 'CANCELLED')),
                notes        TEXT,
                FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE,
                FOREIGN KEY (staff_id)    REFERENCES users(id) ON DELETE SET NULL
            )
            """;

    private static final String CREATE_ORDER_ITEMS = """
            CREATE TABLE IF NOT EXISTS order_items (
                id            INTEGER PRIMARY KEY AUTOINCREMENT,
                order_id      INTEGER NOT NULL,
                item_name     TEXT NOT NULL,
                service_type  TEXT NOT NULL
                              CHECK (service_type IN ('WASH', 'WASH_AND_IRON', 'IRON', 'DRY_CLEAN')),
                quantity      INTEGER NOT NULL CHECK (quantity > 0),
                unit_price    REAL NOT NULL CHECK (unit_price >= 0),
                FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
            )
            """;

    private static final String CREATE_INVOICES = """
            CREATE TABLE IF NOT EXISTS invoices (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                order_id    INTEGER NOT NULL UNIQUE,   -- UNIQUE = one invoice per order (1:1)
                issue_date  TEXT NOT NULL,
                subtotal    REAL NOT NULL CHECK (subtotal >= 0),
                tax_rate    REAL NOT NULL DEFAULT 0.05,
                discount    REAL NOT NULL DEFAULT 0 CHECK (discount >= 0),
                paid        INTEGER NOT NULL DEFAULT 0 CHECK (paid IN (0, 1)),
                FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
            )
            """;

    private static final String CREATE_INVENTORY_ITEMS = """
            CREATE TABLE IF NOT EXISTS inventory_items (
                id             INTEGER PRIMARY KEY AUTOINCREMENT,
                name           TEXT NOT NULL UNIQUE,
                unit           TEXT,
                quantity       REAL NOT NULL DEFAULT 0 CHECK (quantity >= 0),
                reorder_level  REAL NOT NULL DEFAULT 0 CHECK (reorder_level >= 0)
            )
            """;

    private static final String CREATE_INVENTORY_USAGE = """
            CREATE TABLE IF NOT EXISTS inventory_usage (
                id                 INTEGER PRIMARY KEY AUTOINCREMENT,
                order_id           INTEGER NOT NULL,
                inventory_item_id  INTEGER NOT NULL,
                staff_id           INTEGER,
                quantity_used      REAL NOT NULL CHECK (quantity_used > 0),
                used_on            TEXT NOT NULL,
                FOREIGN KEY (order_id)          REFERENCES orders(id)          ON DELETE CASCADE,
                FOREIGN KEY (inventory_item_id) REFERENCES inventory_items(id) ON DELETE CASCADE,
                FOREIGN KEY (staff_id)          REFERENCES users(id)           ON DELETE SET NULL
            )
            """;

    private static final String CREATE_INDEX_ORDERS_CUSTOMER =
            "CREATE INDEX IF NOT EXISTS idx_orders_customer ON orders(customer_id)";

    private static final String CREATE_INDEX_ORDERS_STATUS =
            "CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status)";

    private DatabaseInitializer() {
        // utility class
    }

    // ---------------- Public entry point ----------------

    public static void initialize() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            createTables(conn);
            insertDefaultData(conn);
        } catch (SQLException e) {
            throw new RuntimeException("Could not initialize the database: " + e.getMessage(), e);
        }
    }

    // ---------------- Creating tables ----------------

    private static void createTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(CREATE_USERS);
            stmt.execute(CREATE_ORDERS);
            stmt.execute(CREATE_ORDER_ITEMS);
            stmt.execute(CREATE_INVOICES);
            stmt.execute(CREATE_INVENTORY_ITEMS);
            stmt.execute(CREATE_INVENTORY_USAGE);
            stmt.execute(CREATE_INDEX_ORDERS_CUSTOMER);
            stmt.execute(CREATE_INDEX_ORDERS_STATUS);
        }
    }

    // ---------------- Default data ----------------

    private static void insertDefaultData(Connection conn) throws SQLException {
        if (isTableEmpty(conn, "users")) {
            insertUser(conn, "System Admin", "admin", "admin123", "admin@laundrylink.com", "01700000000",
                    Role.ADMIN.name(), null, null, null, 3);
            insertUser(conn, "Karim Hossain", "staff1", "staff123", "karim@laundrylink.com", "01800000000",
                    Role.STAFF.name(), null, "Washer", "Morning", null);
            insertUser(conn, "Rahim Uddin", "customer1", "cust123", "rahim@mail.com", "01711111111",
                    Role.CUSTOMER.name(), "House 12, Road 5", null, null, null);
        }

        if (isTableEmpty(conn, "inventory_items")) {
            insertInventory(conn, "Detergent Powder", "kg", 50, 10);
            insertInventory(conn, "Fabric Softener", "liter", 30, 8);
            insertInventory(conn, "Bleach", "liter", 20, 5);
            insertInventory(conn, "Hangers", "piece", 200, 50);
            insertInventory(conn, "Packaging Bags", "piece", 300, 100);
        }
    }

    private static boolean isTableEmpty(Connection conn, String tableName) throws SQLException {
        // tableName always comes from our own code above, never from user input
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            return rs.next() && rs.getInt(1) == 0;
        }
    }

    private static void insertUser(Connection conn, String fullName, String username, String password,
                                   String email, String phone, String role,
                                   String address, String designation, String shift,
                                   Integer accessLevel) throws SQLException {
        String sql = """
                INSERT INTO users (full_name, username, password, email, phone, role,
                                   address, designation, shift, access_level)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setString(2, username);
            ps.setString(3, password);
            ps.setString(4, email);
            ps.setString(5, phone);
            ps.setString(6, role);
            ps.setString(7, address);
            ps.setString(8, designation);
            ps.setString(9, shift);
            ps.setObject(10, accessLevel);
            ps.executeUpdate();
        }
    }

    private static void insertInventory(Connection conn, String name, String unit,
                                        double quantity, double reorderLevel) throws SQLException {
        String sql = "INSERT INTO inventory_items (name, unit, quantity, reorder_level) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, unit);
            ps.setDouble(3, quantity);
            ps.setDouble(4, reorderLevel);
            ps.executeUpdate();
        }
    }
}