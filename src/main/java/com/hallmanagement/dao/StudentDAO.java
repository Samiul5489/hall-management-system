package com.hallmanagement.dao;

import com.hallmanagement.database.DBConnection;
import com.hallmanagement.model.HallHistory;
import com.hallmanagement.model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    public Student getStudentByUserId(String userId) throws SQLException {
        String sql =
            "SELECT s.id, s.user_id, s.name, s.phone, s.email, s.department, s.year, s.current_hall_id, " +
            "       h.hall_name, s.current_room, s.current_seat " +
            "FROM students s " +
            "LEFT JOIN halls h ON s.current_hall_id = h.id " +
            "WHERE s.user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Object hallIdObj = rs.getObject("current_hall_id");
                    Integer currentHallId = hallIdObj instanceof Number ? ((Number) hallIdObj).intValue() : null;

                    Object roomObj = rs.getObject("current_room");
                    Integer currentRoom = roomObj instanceof Number ? ((Number) roomObj).intValue() : null;

                    Object seatObj = rs.getObject("current_seat");
                    Integer currentSeat = seatObj instanceof Number ? ((Number) seatObj).intValue() : null;

                    return new Student(
                        rs.getInt("id"),
                        rs.getString("user_id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("department"),
                        rs.getString("year"),
                        currentHallId,
                        rs.getString("hall_name"),
                        currentRoom,
                        currentSeat
                    );
                }
            }
        }
        return null;
    }

    public boolean updateStudentName(String userId, String newName) throws SQLException {
        String updateStudents = "UPDATE students SET name = ? WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(updateStudents)) {

            pst.setString(1, newName);
            pst.setString(2, userId);
            return pst.executeUpdate() > 0;
        }
    }

    public boolean updateStudentPhone(String userId, String newPhone) throws SQLException {
        String updateStudents = "UPDATE students SET phone = ? WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(updateStudents)) {

            pst.setString(1, newPhone);
            pst.setString(2, userId);
            return pst.executeUpdate() > 0;
        }
    }

    public boolean updateStudentEmail(String userId, String newEmail) throws SQLException {
        String updateStudents = "UPDATE students SET email = ? WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(updateStudents)) {

            pst.setString(1, newEmail);
            pst.setString(2, userId);
            return pst.executeUpdate() > 0;
        }
    }

    public List<HallHistory> getHallHistory(int studentDbId) throws SQLException {
        List<HallHistory> history = new ArrayList<>();

        String sql =
            "SELECT h.hall_name, shh.room_number, shh.seat_number, shh.start_date, shh.end_date " +
            "FROM student_hall_history shh " +
            "JOIN halls h ON shh.hall_id = h.id " +
            "WHERE shh.student_id = ? " +
            "ORDER BY shh.start_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentDbId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    history.add(new HallHistory(
                        rs.getString("hall_name"),
                        rs.getInt("room_number"),
                        rs.getInt("seat_number"),
                        rs.getDate("start_date").toLocalDate(),
                        rs.getDate("end_date") != null ? rs.getDate("end_date").toLocalDate() : null
                    ));
                }
            }
        }
        return history;
    }
}
