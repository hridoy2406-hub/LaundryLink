package com.laundrylink.dao;

import com.laundrylink.database.DatabaseConnection;
import com.laundrylink.model.Admin;
import com.laundrylink.model.Customer;
import com.laundrylink.model.Role;
import com.laundrylink.model.Staff;
import com.laundrylink.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserDAO implements GenericDAO<User> {

    @Override
    public boolean insert(User user) {
        String sql = """
                INSERT INTO users (full_name, username, password, email, phone, role,
                                   address, designation, shift, access_level)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fill(ps, user);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) user.setId(keys.getInt(1));
            }
            return true;
        } catch (SQLException e) {
            System.err.println("UserDAO.insert: " + e.getMessage());
            return false;
        }
    }

    @Override
    public User findById(int id) {
        List<User> list = query("SELECT * FROM users WHERE id = ?", id);
        return list.isEmpty() ? null : list.get(0);
    }

    public User findByUsername(String username) {
        List<User> list = query("SELECT * FROM users WHERE username = ?", username);
        return list.isEmpty() ? null : list.get(0);
    }

    /** Returns the user if username and password are correct, otherwise null. */
    public User authenticate(String username, String password) {
        User user = findByUsername(username);
        return (user != null && user.checkPassword(password)) ? user : null;
    }

    @Override
    public List<User> findAll() {
        return query("SELECT * FROM users ORDER BY id", null);
    }

    public List<User> findByRole(Role role) {
        return query("SELECT * FROM users WHERE role = ? ORDER BY id", role.name());
    }

    @Override
    public boolean update(User user) {
        String sql = """
                UPDATE users SET full_name=?, username=?, password=?, email=?, phone=?, role=?,
                                 address=?, designation=?, shift=?, access_level=?
                WHERE id=?
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            fill(ps, user);
            ps.setInt(11, user.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("UserDAO.update: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("UserDAO.delete: " + e.getMessage());
            return false;
        }
    }

    // ---------- helpers ----------

    private void fill(PreparedStatement ps, User u) throws SQLException {
        String address = null, designation = null, shift = null;
        Integer level = null;
        if (u instanceof Customer c) {
            address = c.getAddress();
        } else if (u instanceof Staff s) {
            designation = s.getDesignation();
            shift = s.getShift();
        } else if (u instanceof Admin a) {
            level = a.getAccessLevel();
        }
        ps.setString(1, u.getFullName());
        ps.setString(2, u.getUsername());
        ps.setString(3, u.getPassword());
        ps.setString(4, u.getEmail());
        ps.setString(5, u.getPhone());
        ps.setString(6, u.getRole().name());
        ps.setObject(7, address);
        ps.setObject(8, designation);
        ps.setObject(9, shift);
        ps.setObject(10, level);
    }

    private List<User> query(String sql, Object param) {
        List<User> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (param != null) ps.setObject(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("UserDAO.query: " + e.getMessage());
        }
        return list;
    }

    // Polymorphism: the role decides which subclass object is created
    private User mapRow(ResultSet rs) throws SQLException {
        String name = rs.getString("full_name");
        String username = rs.getString("username");
        String password = rs.getString("password");
        String email = rs.getString("email");
        String phone = rs.getString("phone");
        Role role = Role.valueOf(rs.getString("role"));

        User user = switch (role) {
            case CUSTOMER -> new Customer(name, username, password, email, phone, rs.getString("address"));
            case STAFF -> new Staff(name, username, password, email, phone,
                    rs.getString("designation"), rs.getString("shift"));
            case ADMIN -> {
                int level = rs.getInt("access_level");
                yield new Admin(name, username, password, email, phone, level == 0 ? 1 : level);
            }
        };
        user.setId(rs.getInt("id"));
        return user;
    }
}