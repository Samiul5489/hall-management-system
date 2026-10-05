package com.hallmanagement.dao;

import com.hallmanagement.database.DBConnection;
import com.hallmanagement.model.ProvostInfo;

import java.sql.*;

public class ProvostDAO {

    public ProvostDAO() {
        try {
            ensureSchema();
        } catch (SQLException e) {
            System.err.println("[ProvostDAO] Schema initialization note: " + e.getMessage());
        }
    }

    public synchronized void ensureSchema() throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            try { stmt.executeUpdate("ALTER TABLE provosts ADD COLUMN name VARCHAR(100) DEFAULT 'Provost'"); } catch (SQLException ignored) {}
            try { stmt.executeUpdate("ALTER TABLE provosts ADD COLUMN phone VARCHAR(20) DEFAULT NULL"); } catch (SQLException ignored) {}
            try { stmt.executeUpdate("ALTER TABLE provosts ADD COLUMN age INT DEFAULT 45"); } catch (SQLException ignored) {}
            try { stmt.executeUpdate("ALTER TABLE provosts ADD COLUMN email VARCHAR(100) DEFAULT 'provost@ruet.ac.bd'"); } catch (SQLException ignored) {}
            try { stmt.executeUpdate("ALTER TABLE provosts ADD COLUMN designation VARCHAR(100) DEFAULT 'Professor & Provost'"); } catch (SQLException ignored) {}
            try { stmt.executeUpdate("ALTER TABLE provosts ADD COLUMN office_room VARCHAR(100) DEFAULT 'Provost Office, Ground Floor'"); } catch (SQLException ignored) {}
            try { stmt.executeUpdate("ALTER TABLE provosts ADD COLUMN status ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE'"); } catch (SQLException ignored) {}

            try { stmt.executeUpdate("ALTER TABLE halls ADD COLUMN provost_user_id VARCHAR(50) DEFAULT NULL"); } catch (SQLException ignored) {}
            try { stmt.executeUpdate("ALTER TABLE halls ADD COLUMN provost_name VARCHAR(100) DEFAULT NULL"); } catch (SQLException ignored) {}
            try { stmt.executeUpdate("ALTER TABLE halls ADD COLUMN provost_phone VARCHAR(20) DEFAULT NULL"); } catch (SQLException ignored) {}
        }
    }

    public ProvostInfo getProvostProfile(String userId) throws SQLException {
        ensureSchema();

        String sql =
            "SELECT p.user_id, p.name, p.phone, p.hall_id, COALESCE(h.hall_name, 'Shahid Lt. Selim Hall') AS hall_name, " +
            "       COALESCE(p.age, 45) AS age, " +
            "       COALESCE(p.email, CONCAT(p.user_id, '@ruet.ac.bd')) AS email, " +
            "       COALESCE(p.designation, 'Professor & Provost') AS designation, " +
            "       COALESCE(p.office_room, 'Provost Office, Ground Floor') AS office_room " +
            "FROM provosts p " +
            "LEFT JOIN halls h ON h.id = p.hall_id " +
            "WHERE p.user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new ProvostInfo(
                        rs.getString("user_id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getInt("hall_id"),
                        rs.getString("hall_name"),
                        rs.getInt("age"),
                        rs.getString("email"),
                        rs.getString("designation"),
                        rs.getString("office_room")
                    );
                }
            }
        }

        return null;
    }

    public void updateProvostProfile(String userId, String name, String phone, Integer age,
                                    String email, String designation, String officeRoom, int hallId) throws SQLException {
        ensureSchema();

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {

                String updateProvost =
                    "UPDATE provosts SET name = ?, phone = ?, hall_id = ?, age = ?, email = ?, designation = ?, office_room = ? WHERE user_id = ?";
                try (PreparedStatement psProvost = conn.prepareStatement(updateProvost)) {
                    psProvost.setString(1, name);
                    psProvost.setString(2, phone);
                    psProvost.setInt(3, hallId);
                    psProvost.setInt(4, age != null ? age : 45);
                    psProvost.setString(5, email);
                    psProvost.setString(6, designation);
                    psProvost.setString(7, officeRoom);
                    psProvost.setString(8, userId);
                    int affected = psProvost.executeUpdate();
                    if (affected == 0) {
                        String insertProvost =
                            "INSERT INTO provosts (user_id, password, name, phone, hall_id, age, email, designation, office_room, status) " +
                            "VALUES (?, 'pass', ?, ?, ?, ?, ?, ?, ?, 'ACTIVE')";
                        try (PreparedStatement psIns = conn.prepareStatement(insertProvost)) {
                            psIns.setString(1, userId);
                            psIns.setString(2, name);
                            psIns.setString(3, phone);
                            psIns.setInt(4, hallId);
                            psIns.setInt(5, age != null ? age : 45);
                            psIns.setString(6, email);
                            psIns.setString(7, designation);
                            psIns.setString(8, officeRoom);
                            psIns.executeUpdate();
                        }
                    }
                }

                String updateHall = "UPDATE halls SET provost_user_id = ?, provost_name = ?, provost_phone = ? WHERE id = ?";
                try (PreparedStatement psHall = conn.prepareStatement(updateHall)) {
                    psHall.setString(1, userId);
                    psHall.setString(2, name);
                    psHall.setString(3, phone);
                    psHall.setInt(4, hallId);
                    psHall.executeUpdate();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}
