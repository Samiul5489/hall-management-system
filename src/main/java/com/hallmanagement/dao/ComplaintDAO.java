package com.hallmanagement.dao;

import com.hallmanagement.database.DBConnection;
import com.hallmanagement.model.Complaint;
import com.hallmanagement.model.ProvostInfo;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ComplaintDAO {

    public ComplaintDAO() {
        ensureSchema();
    }

    public void ensureSchema() {
        String createComplaintsTable =
            "CREATE TABLE IF NOT EXISTS complaints (" +
            "    id                INT UNSIGNED    NOT NULL AUTO_INCREMENT," +
            "    student_id        VARCHAR(50)     NOT NULL," +
            "    provost_id        VARCHAR(50)     NOT NULL," +
            "    title             VARCHAR(200)    NOT NULL," +
            "    message           TEXT            NOT NULL," +
            "    submitted_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP," +
            "    status            ENUM('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED') NOT NULL DEFAULT 'PENDING'," +
            "    processed_at      TIMESTAMP       NULL DEFAULT NULL," +
            "    student_viewed_at TIMESTAMP       NULL DEFAULT NULL," +
            "    PRIMARY KEY (id)," +
            "    KEY idx_comp_student (student_id)," +
            "    KEY idx_comp_provost (provost_id)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

        String createResponsesTable =
            "CREATE TABLE IF NOT EXISTS complaint_responses (" +
            "    id               INT UNSIGNED    NOT NULL AUTO_INCREMENT," +
            "    complaint_id     INT UNSIGNED    NOT NULL," +
            "    provost_id       VARCHAR(50)     NOT NULL," +
            "    response_message TEXT            NOT NULL," +
            "    responded_at     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP," +
            "    PRIMARY KEY (id)," +
            "    UNIQUE KEY uq_complaint_response (complaint_id)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createComplaintsTable);
            stmt.execute(createResponsesTable);

            try (ResultSet rs = conn.getMetaData().getColumns(null, null, "complaints", "student_viewed_at")) {
                if (!rs.next()) {
                    stmt.executeUpdate("ALTER TABLE complaints ADD COLUMN student_viewed_at TIMESTAMP NULL DEFAULT NULL");
                }
            }
        } catch (SQLException e) {
            System.err.println("ComplaintDAO.ensureSchema error: " + e.getMessage());
        }
    }

    public static int countWords(String text) {
        if (text == null || text.trim().isEmpty()) return 0;
        String[] words = text.trim().split("\\s+");
        return words.length;
    }

    public Complaint createComplaint(String studentId, String provostId, String title, String message) throws SQLException {
        ensureSchema();

        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Complaint title cannot be empty.");
        }
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Complaint message cannot be empty.");
        }
        if (provostId == null || provostId.trim().isEmpty()) {
            throw new IllegalArgumentException("Please select an assigned Provost.");
        }

        String sql = "INSERT INTO complaints (student_id, provost_id, title, message, status) VALUES (?, ?, ?, ?, 'PENDING')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, studentId.trim());
            ps.setString(2, provostId.trim());
            ps.setString(3, title.trim());
            ps.setString(4, message.trim());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    return getComplaintById(id);
                }
            }
        }
        return null;
    }

    public boolean updatePendingComplaint(int complaintId, String studentId, String title, String message, String provostId) throws SQLException {
        ensureSchema();

        if (title == null || title.trim().isEmpty()) throw new IllegalArgumentException("Title cannot be empty.");
        if (message == null || message.trim().isEmpty()) throw new IllegalArgumentException("Message cannot be empty.");
        if (provostId == null || provostId.trim().isEmpty()) throw new IllegalArgumentException("Please select a Provost.");

        String sql = "UPDATE complaints SET title = ?, message = ?, provost_id = ? " +
                     "WHERE id = ? AND student_id = ? AND status = 'PENDING'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title.trim());
            ps.setString(2, message.trim());
            ps.setString(3, provostId.trim());
            ps.setInt(4, complaintId);
            ps.setString(5, studentId.trim());

            int updated = ps.executeUpdate();
            return updated > 0;
        }
    }

    public boolean cancelComplaint(int complaintId, String studentId) throws SQLException {
        ensureSchema();

        String sql = "UPDATE complaints SET status = 'CANCELLED', processed_at = NOW() " +
                     "WHERE id = ? AND student_id = ? AND status = 'PENDING'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, complaintId);
            ps.setString(2, studentId.trim());

            int updated = ps.executeUpdate();
            return updated > 0;
        }
    }

    public boolean deleteComplaint(int complaintId, String studentId) throws SQLException {
        ensureSchema();

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {

                String delResponses = "DELETE FROM complaint_responses WHERE complaint_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(delResponses)) {
                    ps.setInt(1, complaintId);
                    ps.executeUpdate();
                }

                String delComplaint = "DELETE FROM complaints WHERE id = ? AND student_id = ?";
                int rows;
                try (PreparedStatement ps = conn.prepareStatement(delComplaint)) {
                    ps.setInt(1, complaintId);
                    ps.setString(2, studentId.trim());
                    rows = ps.executeUpdate();
                }

                conn.commit();
                return rows > 0;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public Complaint getComplaintById(int complaintId) throws SQLException {
        ensureSchema();

        String sql =
            "SELECT c.id, c.student_id, c.provost_id, c.title, c.message, c.submitted_at, c.status, c.processed_at, " +
            "       s.name AS student_name, s.department AS student_dept, " +
            "       h_stu.hall_name AS current_hall_name, s.current_room, s.current_seat, " +
            "       p.name AS provost_name, h_prv.hall_name AS provost_hall_name, " +
            "       cr.response_message, cr.responded_at " +
            "FROM complaints c " +
            "JOIN students s ON c.student_id = s.user_id " +
            "LEFT JOIN halls h_stu ON s.current_hall_id = h_stu.id " +
            "LEFT JOIN provosts p ON c.provost_id = p.user_id " +
            "LEFT JOIN halls h_prv ON p.hall_id = h_prv.id " +
            "LEFT JOIN complaint_responses cr ON c.id = cr.complaint_id " +
            "WHERE c.id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, complaintId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapComplaint(rs);
                }
            }
        }
        return null;
    }

    public List<Complaint> getComplaintsByStudent(String studentId) throws SQLException {
        ensureSchema();

        List<Complaint> list = new ArrayList<>();
        String sql =
            "SELECT c.id, c.student_id, c.provost_id, c.title, c.message, c.submitted_at, c.status, c.processed_at, " +
            "       s.name AS student_name, s.department AS student_dept, " +
            "       h_stu.hall_name AS current_hall_name, s.current_room, s.current_seat, " +
            "       p.name AS provost_name, h_prv.hall_name AS provost_hall_name, " +
            "       cr.response_message, cr.responded_at " +
            "FROM complaints c " +
            "JOIN students s ON c.student_id = s.user_id " +
            "LEFT JOIN halls h_stu ON s.current_hall_id = h_stu.id " +
            "LEFT JOIN provosts p ON c.provost_id = p.user_id " +
            "LEFT JOIN halls h_prv ON p.hall_id = h_prv.id " +
            "LEFT JOIN complaint_responses cr ON c.id = cr.complaint_id " +
            "WHERE c.student_id = ? " +
            "ORDER BY c.submitted_at DESC, c.id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapComplaint(rs));
                }
            }
        }
        return list;
    }

    public List<Complaint> getComplaintsByProvost(String provostId) throws SQLException {
        ensureSchema();

        List<Complaint> list = new ArrayList<>();
        String sql =
            "SELECT c.id, c.student_id, c.provost_id, c.title, c.message, c.submitted_at, c.status, c.processed_at, " +
            "       s.name AS student_name, s.department AS student_dept, " +
            "       h_stu.hall_name AS current_hall_name, s.current_room, s.current_seat, " +
            "       p.name AS provost_name, h_prv.hall_name AS provost_hall_name, " +
            "       cr.response_message, cr.responded_at " +
            "FROM complaints c " +
            "JOIN students s ON c.student_id = s.user_id " +
            "LEFT JOIN halls h_stu ON s.current_hall_id = h_stu.id " +
            "LEFT JOIN provosts p ON c.provost_id = p.user_id " +
            "LEFT JOIN halls h_prv ON p.hall_id = h_prv.id " +
            "LEFT JOIN complaint_responses cr ON c.id = cr.complaint_id " +
            "WHERE c.provost_id = ? " +
            "ORDER BY (c.status = 'PENDING') DESC, " +
            "         CASE WHEN c.status = 'PENDING' THEN c.submitted_at END ASC, " +
            "         c.submitted_at DESC, c.id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, provostId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapComplaint(rs));
                }
            }
        }
        return list;
    }

    public void processComplaint(int complaintId, String provostId, String status, String responseMessage) throws SQLException {
        ensureSchema();

        if (!"APPROVED".equalsIgnoreCase(status) && !"REJECTED".equalsIgnoreCase(status)) {
            throw new IllegalArgumentException("Invalid status: " + status + ". Must be APPROVED or REJECTED.");
        }

        String finalMsg = (responseMessage == null || responseMessage.trim().isEmpty())
            ? "We will look into this matter."
            : responseMessage.trim();

        int wordCount = countWords(finalMsg);
        if (wordCount > 100) {
            throw new IllegalArgumentException("Response cannot exceed 100 words. Current words: " + wordCount);
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {

                String lockSql = "SELECT id, provost_id, status FROM complaints WHERE id = ? FOR UPDATE";
                String currentStatus = null;
                String assignedProvost = null;

                try (PreparedStatement psLock = conn.prepareStatement(lockSql)) {
                    psLock.setInt(1, complaintId);
                    try (ResultSet rs = psLock.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Complaint #" + complaintId + " not found.");
                        }
                        assignedProvost = rs.getString("provost_id");
                        currentStatus = rs.getString("status");
                    }
                }

                if (!provostId.trim().equalsIgnoreCase(assignedProvost)) {
                    throw new SecurityException("Unauthorized: This complaint is assigned to another Provost.");
                }

                if (!"PENDING".equalsIgnoreCase(currentStatus)) {
                    throw new IllegalStateException("A response has already been submitted for this complaint or it was cancelled.");
                }

                String checkRespSql = "SELECT id FROM complaint_responses WHERE complaint_id = ?";
                try (PreparedStatement psResp = conn.prepareStatement(checkRespSql)) {
                    psResp.setInt(1, complaintId);
                    try (ResultSet rs = psResp.executeQuery()) {
                        if (rs.next()) {
                            throw new IllegalStateException("A response has already been submitted for this complaint.");
                        }
                    }
                }

                String updateCompSql = "UPDATE complaints SET status = ?, processed_at = NOW() WHERE id = ?";
                try (PreparedStatement psUpdate = conn.prepareStatement(updateCompSql)) {
                    psUpdate.setString(1, status.toUpperCase());
                    psUpdate.setInt(2, complaintId);
                    psUpdate.executeUpdate();
                }

                String insertRespSql =
                    "INSERT INTO complaint_responses (complaint_id, provost_id, response_message) VALUES (?, ?, ?)";
                try (PreparedStatement psIns = conn.prepareStatement(insertRespSql)) {
                    psIns.setInt(1, complaintId);
                    psIns.setString(2, provostId.trim());
                    psIns.setString(3, finalMsg);
                    psIns.executeUpdate();
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                if (e instanceof SQLException || e instanceof RuntimeException) throw e;
                throw new SQLException(e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public List<ProvostInfo> getProvostListWithHalls() throws SQLException {
        ensureSchema();

        List<ProvostInfo> list = new ArrayList<>();
        String sql =
            "SELECT p.user_id, p.name, p.phone, " +
            "       COALESCE(p.hall_id, 1) AS hall_id, " +
            "       COALESCE(h.hall_name, 'General Administration') AS hall_name, " +
            "       COALESCE(p.age, 45) AS age, " +
            "       COALESCE(p.email, CONCAT(p.user_id, '@ruet.ac.bd')) AS email, " +
            "       COALESCE(p.designation, 'Professor & Provost') AS designation, " +
            "       COALESCE(p.office_room, 'Provost Office') AS office_room " +
            "FROM provosts p " +
            "LEFT JOIN halls h ON p.hall_id = h.id " +
            "WHERE p.status = 'ACTIVE' " +
            "ORDER BY h.id ASC, p.name ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ProvostInfo p = new ProvostInfo(
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
                list.add(p);
            }
        }
        return list;
    }

    public int getPendingComplaintCountForProvost(String provostId) {
        if (provostId == null || provostId.trim().isEmpty()) return 0;
        ensureSchema();
        String sql = "SELECT COUNT(*) FROM complaints WHERE provost_id = ? AND status = 'PENDING'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, provostId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[ComplaintDAO] Failed to get pending complaint count: " + e.getMessage());
        }
        return 0;
    }

    public boolean markComplaintAsReviewedByStudent(int complaintId, String studentId) {
        if (studentId == null || studentId.trim().isEmpty()) return false;
        ensureSchema();
        String sql = "UPDATE complaints SET student_viewed_at = CURRENT_TIMESTAMP WHERE id = ? AND student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, complaintId);
            ps.setString(2, studentId.trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ComplaintDAO] Failed to mark complaint as reviewed: " + e.getMessage());
            return false;
        }
    }

    public int getActiveOrRespondedCountForStudent(String studentId) {
        if (studentId == null || studentId.trim().isEmpty()) return 0;
        ensureSchema();
        String sql = "SELECT COUNT(*) FROM complaints " +
                     "WHERE student_id = ? " +
                     "  AND status IN ('APPROVED', 'REJECTED') " +
                     "  AND (student_viewed_at IS NULL OR (processed_at IS NOT NULL AND student_viewed_at < processed_at))";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[ComplaintDAO] Failed to get student complaint badge count: " + e.getMessage());
        }
        return 0;
    }

    private Complaint mapComplaint(ResultSet rs) throws SQLException {
        Complaint c = new Complaint();
        c.setId(rs.getInt("id"));
        c.setStudentId(rs.getString("student_id"));
        c.setProvostId(rs.getString("provost_id"));
        c.setTitle(rs.getString("title"));
        c.setMessage(rs.getString("message"));

        Timestamp subTs = rs.getTimestamp("submitted_at");
        if (subTs != null) c.setSubmittedAt(subTs.toLocalDateTime());

        c.setStatus(rs.getString("status"));

        Timestamp procTs = rs.getTimestamp("processed_at");
        if (procTs != null) c.setProcessedAt(procTs.toLocalDateTime());

        try {
            Timestamp viewTs = rs.getTimestamp("student_viewed_at");
            if (viewTs != null) c.setStudentViewedAt(viewTs.toLocalDateTime());
        } catch (SQLException ignored) {}

        c.setStudentName(rs.getString("student_name"));
        c.setStudentDepartment(rs.getString("student_dept"));
        c.setStudentRoll(rs.getString("student_id"));
        c.setHallName(rs.getString("current_hall_name"));
        c.setRoomNumber(rs.getInt("current_room"));
        c.setSeatNumber(rs.getInt("current_seat"));

        c.setProvostName(rs.getString("provost_name"));
        c.setProvostHallName(rs.getString("provost_hall_name"));

        c.setResponseMessage(rs.getString("response_message"));
        Timestamp respTs = rs.getTimestamp("responded_at");
        if (respTs != null) c.setRespondedAt(respTs.toLocalDateTime());

        return c;
    }
}
