package com.hallmanagement.dao;

import com.hallmanagement.database.DBConnection;
import com.hallmanagement.model.User;
import com.hallmanagement.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UserDAO {

    private static boolean isInitialized = false;

    public UserDAO() {
        if (!isInitialized) {
            ensureEmailColumnsAndSingleStudentSeed();
            isInitialized = true;
        }
    }

    public void ensureEmailColumnsAndSingleStudentSeed() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            try { stmt.executeUpdate("ALTER TABLE students ADD COLUMN email VARCHAR(100) DEFAULT NULL"); } catch (SQLException ignored) {}
            try { stmt.executeUpdate("ALTER TABLE provosts ADD COLUMN email VARCHAR(100) DEFAULT 'saniulsami@gmail.com'"); } catch (SQLException ignored) {}

            try { stmt.executeUpdate("UPDATE provosts SET email = 'saniulsami@gmail.com' WHERE email IS NULL OR email = ''"); } catch (SQLException ignored) {}

            boolean shamiulExists = false;
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM students WHERE user_id = '2403058'")) {
                if (rs.next() && rs.getInt(1) > 0) {
                    shamiulExists = true;
                }
            }

            try {
                stmt.executeUpdate("DELETE FROM students WHERE user_id NOT IN ('2403058')");
            } catch (SQLException ignored) {}

            if (!shamiulExists) {

                String insertShamiul = "INSERT INTO students (user_id, password, name, phone, email, department, year, current_hall_id, current_room, current_seat, status) " +
                                      "VALUES ('2403058', '1111', 'Shamiul Islam', '01921602024', 'shamiulislam39999@gmail.com', 'ECE', '1st Year', 1, 101, 1, 'ACTIVE')";
                stmt.executeUpdate(insertShamiul);

                try (ResultSet rs = stmt.executeQuery("SELECT id FROM students WHERE user_id = '2403058'")) {
                    if (rs.next()) {
                        int sId = rs.getInt(1);
                        stmt.executeUpdate("INSERT INTO student_hall_history (student_id, hall_id, room_number, seat_number, start_date) " +
                                          "VALUES (" + sId + ", 1, 101, 1, '2024-01-01') " +
                                          "ON DUPLICATE KEY UPDATE student_id=" + sId);
                    }
                }
            } else {

                stmt.executeUpdate("UPDATE students SET name = 'Shamiul Islam', email = 'shamiulislam39999@gmail.com', phone = '01921602024' WHERE user_id = '2403058' AND (email IS NULL OR email = '')");
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Migration/Seeding check: " + e.getMessage());
        }
    }

    public static class DatabaseException extends Exception {
        public DatabaseException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class InvalidCredentialsException extends Exception {
        public InvalidCredentialsException(String message) { super(message); }
    }

    public static class RoleMismatchException extends Exception {
        public RoleMismatchException(String message) { super(message); }
    }

    public static class InactiveAccountException extends Exception {
        public InactiveAccountException(String message) { super(message); }
    }

    public User authenticate(String userId, String plainPassword, String selectedRole)
            throws DatabaseException, InvalidCredentialsException,
                   RoleMismatchException, InactiveAccountException {

        boolean isStudentRole = "STUDENT".equalsIgnoreCase(selectedRole);
        String targetTable = isStudentRole ? "students" : "provosts";
        String oppositeTable = isStudentRole ? "provosts" : "students";

        String targetSql = "SELECT id, user_id, password, name, phone, status FROM " + targetTable + " WHERE user_id = ?";
        String oppositeSql = "SELECT id, user_id FROM " + oppositeTable + " WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection()) {

            try (PreparedStatement ps = conn.prepareStatement(targetSql)) {
                ps.setString(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String storedPassword = rs.getString("password");
                        String dbStatus     = rs.getString("status");
                        String dbName       = rs.getString("name");
                        String dbPhone      = rs.getString("phone");
                        int    dbId         = rs.getInt("id");
                        String dbUserId     = rs.getString("user_id");

                        if (!PasswordUtil.checkPassword(plainPassword, storedPassword)) {
                            throw new InvalidCredentialsException("Invalid User ID or Password.");
                        }

                        if (!"ACTIVE".equalsIgnoreCase(dbStatus)) {
                            throw new InactiveAccountException(
                                "This account is currently inactive.\nPlease contact the administrator."
                            );
                        }

                        return new User(dbId, dbUserId, dbName, dbPhone, isStudentRole ? "STUDENT" : "PROVOST", dbStatus);
                    }
                }
            }

            try (PreparedStatement psOpposite = conn.prepareStatement(oppositeSql)) {
                psOpposite.setString(1, userId);
                try (ResultSet rsOpposite = psOpposite.executeQuery()) {
                    if (rsOpposite.next()) {
                        throw new RoleMismatchException("Invalid access type for this account.");
                    }
                }
            }

            throw new InvalidCredentialsException("Invalid User ID or Password.");

        } catch (SQLException e) {
            System.err.println("[UserDAO] Database error during authentication: " + e.getMessage());
            throw new DatabaseException(
                "Unable to connect to the server.\nPlease check the database connection.", e
            );
        }
    }

    public User findByUserId(String userId) throws DatabaseException {
        try (Connection conn = DBConnection.getConnection()) {

            String studentSql = "SELECT id, user_id, name, phone, status FROM students WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(studentSql)) {
                ps.setString(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new User(
                            rs.getInt("id"),
                            rs.getString("user_id"),
                            rs.getString("name"),
                            rs.getString("phone"),
                            "STUDENT",
                            rs.getString("status")
                        );
                    }
                }
            }

            String provostSql = "SELECT id, user_id, name, phone, status FROM provosts WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(provostSql)) {
                ps.setString(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new User(
                            rs.getInt("id"),
                            rs.getString("user_id"),
                            rs.getString("name"),
                            rs.getString("phone"),
                            "PROVOST",
                            rs.getString("status")
                        );
                    }
                }
            }

            return null;
        } catch (SQLException e) {
            System.err.println("[UserDAO] Database error in findByUserId: " + e.getMessage());
            throw new DatabaseException(
                "Unable to connect to the server.\nPlease check the database connection.", e
            );
        }
    }

    public String findEmailByUserIdAndRole(String userId, String role) throws SQLException {
        boolean isStudent = "STUDENT".equalsIgnoreCase(role);
        String table = isStudent ? "students" : "provosts";
        String sql = "SELECT email FROM " + table + " WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("email");
                }
            }
        }
        return null;
    }

    public boolean updatePasswordByUserIdAndRole(String userId, String role, String newPassword) throws SQLException {
        boolean isStudent = "STUDENT".equalsIgnoreCase(role);
        String table = isStudent ? "students" : "provosts";
        String hashedPassword = PasswordUtil.hashPassword(newPassword);
        String sql = "UPDATE " + table + " SET password = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setString(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public void registerStudent(String userId, String plainPassword, String name, String phone, String email, String department, String year, Integer currentHallId, Integer currentRoom, Integer currentSeat, Integer prevHallId, Integer prevRoom, Integer prevSeat) throws Exception {
        String hashedPassword = PasswordUtil.hashPassword(plainPassword);

        String checkSeatSql = "SELECT COUNT(*) FROM students WHERE current_hall_id = ? AND current_room = ? AND current_seat = ?";
        String insertStudent = "INSERT INTO students (user_id, password, name, phone, email, department, year, current_hall_id, current_room, current_seat, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVE')";
        String insertHistory = "INSERT INTO student_hall_history (student_id, hall_id, room_number, seat_number, start_date, end_date) VALUES (?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            if (currentHallId != null && currentRoom != null && currentSeat != null) {
                try (PreparedStatement pstCheck = conn.prepareStatement(checkSeatSql)) {
                    pstCheck.setInt(1, currentHallId);
                    pstCheck.setInt(2, currentRoom);
                    pstCheck.setInt(3, currentSeat);
                    try (ResultSet rs = pstCheck.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            throw new Exception("This seat is already occupied. Please select another available seat.");
                        }
                    }
                }
            }

            try (PreparedStatement pstStudent = conn.prepareStatement(insertStudent, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement pstHistory = conn.prepareStatement(insertHistory)) {

                pstStudent.setString(1, userId);
                pstStudent.setString(2, hashedPassword);
                pstStudent.setString(3, name);
                pstStudent.setString(4, phone);
                pstStudent.setString(5, email != null && !email.trim().isEmpty() ? email.trim() : null);
                pstStudent.setString(6, department);
                pstStudent.setString(7, year);

                if (currentHallId != null) {
                    pstStudent.setInt(8, currentHallId);
                    pstStudent.setInt(9, currentRoom);
                    pstStudent.setInt(10, currentSeat);
                } else {
                    pstStudent.setNull(8, java.sql.Types.INTEGER);
                    pstStudent.setNull(9, java.sql.Types.INTEGER);
                    pstStudent.setNull(10, java.sql.Types.INTEGER);
                }
                pstStudent.executeUpdate();

                int generatedStudentId = -1;
                try (ResultSet rs = pstStudent.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedStudentId = rs.getInt(1);
                    }
                }

                if (generatedStudentId != -1) {

                    if (prevHallId != null) {
                        pstHistory.setInt(1, generatedStudentId);
                        pstHistory.setInt(2, prevHallId);
                        pstHistory.setInt(3, prevRoom != null ? prevRoom : 0);
                        pstHistory.setInt(4, prevSeat != null ? prevSeat : 0);
                        pstHistory.setDate(5, java.sql.Date.valueOf(java.time.LocalDate.now().minusYears(1)));
                        pstHistory.setDate(6, java.sql.Date.valueOf(java.time.LocalDate.now()));
                        pstHistory.executeUpdate();
                    }

                    if (currentHallId != null) {
                        pstHistory.setInt(1, generatedStudentId);
                        pstHistory.setInt(2, currentHallId);
                        pstHistory.setInt(3, currentRoom != null ? currentRoom : 0);
                        pstHistory.setInt(4, currentSeat != null ? currentSeat : 0);
                        pstHistory.setDate(5, java.sql.Date.valueOf(java.time.LocalDate.now()));
                        pstHistory.setNull(6, java.sql.Types.DATE);
                        pstHistory.executeUpdate();
                    }
                }

                conn.commit();
            }
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            if (e.getMessage().contains("uq_active_seat") || (e.getMessage().contains("Duplicate entry") && e.getMessage().contains("current_seat"))) {
                throw new Exception("This seat is already occupied. Please select another available seat.");
            }
            if (e.getMessage().contains("Duplicate entry")) {
                throw new Exception("User ID or Student ID already exists.");
            }
            throw new DatabaseException("Failed to register student.", e);
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
                conn.close();
            }
        }
    }

    public void registerStudent(String userId, String plainPassword, String name, String phone, String department, String year, Integer currentHallId, Integer currentRoom, Integer currentSeat, Integer prevHallId, Integer prevRoom, Integer prevSeat) throws Exception {
        registerStudent(userId, plainPassword, name, phone, null, department, year, currentHallId, currentRoom, currentSeat, prevHallId, prevRoom, prevSeat);
    }

    public void registerProvost(String userId, String plainPassword, String name, String phone, int hallId) throws Exception {
        String hashedPassword = PasswordUtil.hashPassword(plainPassword);

        String insertProvost = "INSERT INTO provosts (user_id, password, name, phone, hall_id, status) VALUES (?, ?, ?, ?, ?, 'ACTIVE')";
        String updateHall = "UPDATE halls SET provost_user_id = ?, provost_name = ?, provost_phone = ? WHERE id = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement pstProvost = conn.prepareStatement(insertProvost);
                 PreparedStatement pstHall = conn.prepareStatement(updateHall)) {

                pstProvost.setString(1, userId);
                pstProvost.setString(2, hashedPassword);
                pstProvost.setString(3, name);
                pstProvost.setString(4, phone);
                pstProvost.setInt(5, hallId);
                pstProvost.executeUpdate();

                pstHall.setString(1, userId);
                pstHall.setString(2, name);
                pstHall.setString(3, phone);
                pstHall.setInt(4, hallId);
                pstHall.executeUpdate();

                conn.commit();
            }
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            if (e.getMessage().contains("Duplicate entry")) {
                throw new Exception("User ID already exists.");
            }
            throw new DatabaseException("Failed to register provost.", e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
                conn.close();
            }
        }
    }
}
