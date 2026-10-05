package com.hallmanagement.dao;

import com.hallmanagement.database.DBConnection;
import com.hallmanagement.model.HallLeaveRequest;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HallLeaveDAO {

    public static class EligibilityStatus {
        private final double totalDue;
        private final boolean isEligible;
        private final boolean hasActiveHall;
        private final boolean hasPendingRequest;
        private final HallLeaveRequest pendingRequest;
        private final String message;

        public EligibilityStatus(double totalDue, boolean isEligible, boolean hasActiveHall,
                                 boolean hasPendingRequest, HallLeaveRequest pendingRequest, String message) {
            this.totalDue = totalDue;
            this.isEligible = isEligible;
            this.hasActiveHall = hasActiveHall;
            this.hasPendingRequest = hasPendingRequest;
            this.pendingRequest = pendingRequest;
            this.message = message;
        }

        public double getTotalDue() { return totalDue; }
        public boolean isEligible() { return isEligible; }
        public boolean isHasActiveHall() { return hasActiveHall; }
        public boolean isHasPendingRequest() { return hasPendingRequest; }
        public HallLeaveRequest getPendingRequest() { return pendingRequest; }
        public String getMessage() { return message; }
    }

    public synchronized void ensureSchemaAndSeed() throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            String createTableSql =
                "CREATE TABLE IF NOT EXISTS hall_leave_requests (" +
                "    id                 INT UNSIGNED    NOT NULL AUTO_INCREMENT," +
                "    student_id         VARCHAR(50)     NOT NULL," +
                "    hall_id            INT UNSIGNED    NOT NULL," +
                "    room_number        INT             NOT NULL," +
                "    seat_number        INT             NOT NULL," +
                "    reason             TEXT            NOT NULL," +
                "    request_date       DATE            NOT NULL," +
                "    status             ENUM('PENDING','APPROVED','DECLINED','CANCELLED') NOT NULL DEFAULT 'PENDING'," +
                "    processed_by       VARCHAR(50)     DEFAULT NULL," +
                "    processed_date     TIMESTAMP       NULL DEFAULT NULL," +
                "    decline_reason     TEXT            DEFAULT NULL," +
                "    student_viewed_at  TIMESTAMP       NULL DEFAULT NULL," +
                "    created_at         TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "    updated_at         TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "    PRIMARY KEY (id)," +
                "    KEY idx_hlr_student (student_id)," +
                "    KEY idx_hlr_hall (hall_id)," +
                "    FOREIGN KEY (student_id) REFERENCES students(user_id) ON DELETE CASCADE," +
                "    FOREIGN KEY (hall_id) REFERENCES halls(id) ON DELETE CASCADE," +
                "    FOREIGN KEY (processed_by) REFERENCES provosts(user_id) ON DELETE SET NULL" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

            stmt.executeUpdate(createTableSql);

            try (ResultSet rs = conn.getMetaData().getColumns(null, null, "hall_leave_requests", "student_viewed_at")) {
                if (!rs.next()) {
                    stmt.executeUpdate("ALTER TABLE hall_leave_requests ADD COLUMN student_viewed_at TIMESTAMP NULL DEFAULT NULL");
                }
            }
        }
    }

    public EligibilityStatus checkStudentEligibility(String studentUserId) throws SQLException {
        ensureSchemaAndSeed();

        Integer currentHallId = null;
        Integer currentRoom = null;
        Integer currentSeat = null;

        String studentSql = "SELECT current_hall_id, current_room, current_seat FROM students WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(studentSql)) {
            ps.setString(1, studentUserId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Object hallObj = rs.getObject("current_hall_id");
                    if (hallObj instanceof Number) currentHallId = ((Number) hallObj).intValue();
                    Object roomObj = rs.getObject("current_room");
                    if (roomObj instanceof Number) currentRoom = ((Number) roomObj).intValue();
                    Object seatObj = rs.getObject("current_seat");
                    if (seatObj instanceof Number) currentSeat = ((Number) seatObj).intValue();
                }
            }
        }

        if (currentHallId == null || currentRoom == null || currentSeat == null) {
            return new EligibilityStatus(
                0.0,
                false,
                false,
                false,
                null,
                "You are not currently assigned to any hall. Hall leave application is not available."
            );
        }

        double totalDue = 0.0;
        String dueSql = "SELECT COALESCE(SUM(due_amount), 0.0) FROM bills WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(dueSql)) {
            ps.setString(1, studentUserId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    totalDue = rs.getDouble(1);
                }
            }
        }

        if (totalDue > 0.0) {
            return new EligibilityStatus(
                totalDue,
                false,
                true,
                false,
                null,
                String.format("You cannot apply for hall leave because you have an outstanding hall due of Tk. %.2f. Please clear your due first.", totalDue)
            );
        }

        HallLeaveRequest pendingReq = null;
        String pendingSql =
            "SELECT hlr.*, st.name AS student_name, st.department AS student_dept, h.hall_name, up.name AS processed_by_name " +
            "FROM hall_leave_requests hlr " +
            "JOIN students st ON st.user_id = hlr.student_id " +
            "JOIN halls h ON h.id = hlr.hall_id " +
            "LEFT JOIN provosts up ON up.user_id = hlr.processed_by " +
            "WHERE hlr.student_id = ? AND hlr.status = 'PENDING' " +
            "LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(pendingSql)) {
            ps.setString(1, studentUserId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    pendingReq = mapResultSetToRequest(rs);
                }
            }
        }

        if (pendingReq != null) {
            return new EligibilityStatus(
                0.0,
                false,
                true,
                true,
                pendingReq,
                "You already have a pending hall leave request (" + pendingReq.getRequestCode() + "). Please wait for the Provost to process it."
            );
        }

        return new EligibilityStatus(
            0.0,
            true,
            true,
            false,
            null,
            "You are eligible to apply for hall leave. Current Due: Tk. 0"
        );
    }

    public HallLeaveRequest createHallLeaveRequest(String studentId, int hallId, int roomNumber, int seatNumber, String reason) throws Exception {
        ensureSchemaAndSeed();

        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Reason for leaving cannot be empty.");
        }

        EligibilityStatus eligibility = checkStudentEligibility(studentId);
        if (!eligibility.isEligible()) {
            throw new IllegalStateException(eligibility.getMessage());
        }

        String insertSql =
            "INSERT INTO hall_leave_requests (student_id, hall_id, room_number, seat_number, reason, request_date, status) " +
            "VALUES (?, ?, ?, ?, ?, CURDATE(), 'PENDING')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, studentId.trim());
            ps.setInt(2, hallId);
            ps.setInt(3, roomNumber);
            ps.setInt(4, seatNumber);
            ps.setString(5, reason.trim());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Failed to create hall leave request.");
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    return getRequestById(id);
                }
            }
        }
        throw new SQLException("Could not retrieve generated leave request.");
    }

    public boolean cancelHallLeaveRequest(int requestId, String studentId) throws Exception {
        ensureSchemaAndSeed();
        String sql = "UPDATE hall_leave_requests SET status = 'CANCELLED' WHERE id = ? AND student_id = ? AND status = 'PENDING'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            ps.setString(2, studentId.trim());
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new IllegalStateException("Could not cancel request. It may already be processed or not found.");
            }
            return true;
        }
    }

    public HallLeaveRequest getRequestById(int id) throws SQLException {
        ensureSchemaAndSeed();
        String sql =
            "SELECT hlr.*, st.name AS student_name, st.department AS student_dept, h.hall_name, up.name AS processed_by_name " +
            "FROM hall_leave_requests hlr " +
            "JOIN students st ON st.user_id = hlr.student_id " +
            "JOIN halls h ON h.id = hlr.hall_id " +
            "LEFT JOIN provosts up ON up.user_id = hlr.processed_by " +
            "WHERE hlr.id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRequest(rs);
                }
            }
        }
        return null;
    }

    public List<HallLeaveRequest> getRequestsForStudent(String studentId) throws SQLException {
        ensureSchemaAndSeed();
        List<HallLeaveRequest> list = new ArrayList<>();
        String sql =
            "SELECT hlr.*, st.name AS student_name, st.department AS student_dept, h.hall_name, up.name AS processed_by_name " +
            "FROM hall_leave_requests hlr " +
            "JOIN students st ON st.user_id = hlr.student_id " +
            "JOIN halls h ON h.id = hlr.hall_id " +
            "LEFT JOIN provosts up ON up.user_id = hlr.processed_by " +
            "WHERE hlr.student_id = ? " +
            "ORDER BY hlr.id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                int index = 1;
                while (rs.next()) {
                    HallLeaveRequest req = mapResultSetToRequest(rs);
                    req.setSerial(String.format("%02d", index++));
                    list.add(req);
                }
            }
        }
        return list;
    }

    public List<HallLeaveRequest> getPendingRequestsForProvost(String provostUserId) throws SQLException {
        ensureSchemaAndSeed();
        List<HallLeaveRequest> list = new ArrayList<>();
        String sql =
            "SELECT hlr.*, st.name AS student_name, st.department AS student_dept, h.hall_name, up.name AS processed_by_name " +
            "FROM hall_leave_requests hlr " +
            "JOIN students st ON st.user_id = hlr.student_id " +
            "JOIN halls h ON h.id = hlr.hall_id " +
            "LEFT JOIN provosts up ON up.user_id = hlr.processed_by " +
            "WHERE hlr.status = 'PENDING' " +
            "  AND (hlr.hall_id IN (SELECT hall_id FROM provosts WHERE user_id = ?) " +
            "       OR NOT EXISTS (SELECT 1 FROM provosts WHERE user_id = ?)) " +
            "ORDER BY hlr.request_date ASC, hlr.id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, provostUserId);
            ps.setString(2, provostUserId);
            try (ResultSet rs = ps.executeQuery()) {
                int index = 1;
                while (rs.next()) {
                    HallLeaveRequest req = mapResultSetToRequest(rs);
                    req.setSerial(String.format("%02d", index++));
                    list.add(req);
                }
            }
        }
        return list;
    }

    public List<HallLeaveRequest> getAllRequestsForProvost(String provostUserId, String keyword) throws SQLException {
        ensureSchemaAndSeed();
        List<HallLeaveRequest> list = new ArrayList<>();
        String sql =
            "SELECT hlr.*, st.name AS student_name, st.department AS student_dept, h.hall_name, up.name AS processed_by_name " +
            "FROM hall_leave_requests hlr " +
            "JOIN students st ON st.user_id = hlr.student_id " +
            "JOIN halls h ON h.id = hlr.hall_id " +
            "LEFT JOIN provosts up ON up.user_id = hlr.processed_by " +
            "WHERE (hlr.hall_id IN (SELECT hall_id FROM provosts WHERE user_id = ?) " +
            "       OR NOT EXISTS (SELECT 1 FROM provosts WHERE user_id = ?)) ";

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql += " AND (hlr.id LIKE ? OR st.name LIKE ? OR hlr.student_id LIKE ? OR h.hall_name LIKE ?) ";
        }
        sql += " ORDER BY hlr.id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, provostUserId);
            ps.setString(2, provostUserId);

            if (keyword != null && !keyword.trim().isEmpty()) {
                String term = "%" + keyword.trim() + "%";
                ps.setString(3, term);
                ps.setString(4, term);
                ps.setString(5, term);
                ps.setString(6, term);
            }

            try (ResultSet rs = ps.executeQuery()) {
                int index = 1;
                while (rs.next()) {
                    HallLeaveRequest req = mapResultSetToRequest(rs);
                    req.setSerial(String.format("%02d", index++));
                    list.add(req);
                }
            }
        }
        return list;
    }

    public void approveHallLeaveRequest(int requestId, String provostUserId) throws Exception {
        ensureSchemaAndSeed();
        verifyProvostAuthorization(provostUserId);

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {

                String queryReq = "SELECT * FROM hall_leave_requests WHERE id = ? FOR UPDATE";
                String studentId;
                int hallId;
                int roomNumber;
                int seatNumber;
                String status;

                try (PreparedStatement psReq = conn.prepareStatement(queryReq)) {
                    psReq.setInt(1, requestId);
                    try (ResultSet rs = psReq.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalStateException("Hall leave request #" + requestId + " does not exist.");
                        }
                        status = rs.getString("status");
                        if (!"PENDING".equalsIgnoreCase(status)) {
                            throw new IllegalStateException("This request has already been processed (Status: " + status + ").");
                        }
                        studentId = rs.getString("student_id");
                        hallId = rs.getInt("hall_id");
                        roomNumber = rs.getInt("room_number");
                        seatNumber = rs.getInt("seat_number");
                    }
                }

                String dueSql = "SELECT COALESCE(SUM(due_amount), 0.0) FROM bills WHERE student_id = ?";
                try (PreparedStatement psDue = conn.prepareStatement(dueSql)) {
                    psDue.setString(1, studentId);
                    try (ResultSet rs = psDue.executeQuery()) {
                        if (rs.next()) {
                            double due = rs.getDouble(1);
                            if (due > 0.0) {
                                throw new IllegalStateException(String.format("Approval blocked. Student has an outstanding due of Tk. %.2f.", due));
                            }
                        }
                    }
                }

                String studentCheckSql = "SELECT current_hall_id, current_room, current_seat FROM students WHERE user_id = ?";
                try (PreparedStatement psStu = conn.prepareStatement(studentCheckSql)) {
                    psStu.setString(1, studentId);
                    try (ResultSet rs = psStu.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalStateException("Student record not found.");
                        }
                        Object curHall = rs.getObject("current_hall_id");
                        Object curRoom = rs.getObject("current_room");
                        Object curSeat = rs.getObject("current_seat");

                        if (curHall == null || curRoom == null || curSeat == null) {
                            throw new IllegalStateException("Student has no active residence to vacate.");
                        }
                        if (((Number) curHall).intValue() != hallId ||
                            ((Number) curRoom).intValue() != roomNumber ||
                            ((Number) curSeat).intValue() != seatNumber) {
                            throw new IllegalStateException("Student residence has changed since leave application was submitted.");
                        }
                    }
                }

                String updateStudentSql = "UPDATE students SET current_hall_id = NULL, current_room = NULL, current_seat = NULL WHERE user_id = ?";
                try (PreparedStatement psUpStudent = conn.prepareStatement(updateStudentSql)) {
                    psUpStudent.setString(1, studentId);
                    int affected = psUpStudent.executeUpdate();
                    if (affected <= 0) {
                        throw new SQLException("Failed to clear student residence record.");
                    }
                }

                String closeHistorySql =
                    "UPDATE student_hall_history SET end_date = CURDATE() " +
                    "WHERE student_id = (SELECT id FROM students WHERE user_id = ?) AND end_date IS NULL";
                try (PreparedStatement psCloseHist = conn.prepareStatement(closeHistorySql)) {
                    psCloseHist.setString(1, studentId);
                    psCloseHist.executeUpdate();
                }

                String updateReqSql =
                    "UPDATE hall_leave_requests " +
                    "SET status = 'APPROVED', processed_by = ?, processed_date = NOW(), student_viewed_at = NULL " +
                    "WHERE id = ?";
                try (PreparedStatement psUpReq = conn.prepareStatement(updateReqSql)) {
                    psUpReq.setString(1, provostUserId);
                    psUpReq.setInt(2, requestId);
                    psUpReq.executeUpdate();
                }

                conn.commit();

            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public void declineHallLeaveRequest(int requestId, String provostUserId, String declineReason) throws Exception {
        ensureSchemaAndSeed();
        verifyProvostAuthorization(provostUserId);

        if (declineReason == null || declineReason.trim().isEmpty()) {
            throw new IllegalArgumentException("Decline reason is required.");
        }

        String sql =
            "UPDATE hall_leave_requests " +
            "SET status = 'DECLINED', decline_reason = ?, processed_by = ?, processed_date = NOW(), student_viewed_at = NULL " +
            "WHERE id = ? AND status = 'PENDING'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, declineReason.trim());
            ps.setString(2, provostUserId);
            ps.setInt(3, requestId);
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new IllegalStateException("Could not decline request. It may have already been processed.");
            }
        }
    }

    public boolean markLeaveRequestAsReviewedByStudent(int requestId, String studentId) {
        if (studentId == null || studentId.trim().isEmpty()) return false;
        try {
            ensureSchemaAndSeed();
            String sql = "UPDATE hall_leave_requests SET student_viewed_at = CURRENT_TIMESTAMP WHERE id = ? AND student_id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, requestId);
                ps.setString(2, studentId.trim());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("[HallLeaveDAO] Failed to mark leave request as reviewed: " + e.getMessage());
            return false;
        }
    }

    public int getPendingCountForProvost(String provostUserId) {
        if (provostUserId == null || provostUserId.trim().isEmpty()) return 0;
        try {
            ensureSchemaAndSeed();
            String sql =
                "SELECT COUNT(*) FROM hall_leave_requests hlr " +
                "WHERE hlr.status = 'PENDING' " +
                "  AND (hlr.hall_id IN (SELECT hall_id FROM provosts WHERE user_id = ?) " +
                "       OR NOT EXISTS (SELECT 1 FROM provosts WHERE user_id = ?))";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, provostUserId);
                ps.setString(2, provostUserId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[HallLeaveDAO] Failed to get pending count: " + e.getMessage());
        }
        return 0;
    }

    public int getUnreviewedProcessedCountForStudent(String studentUserId) {
        if (studentUserId == null || studentUserId.trim().isEmpty()) return 0;
        try {
            ensureSchemaAndSeed();
            String sql =
                "SELECT COUNT(*) FROM hall_leave_requests " +
                "WHERE student_id = ? " +
                "  AND status IN ('APPROVED', 'DECLINED') " +
                "  AND (student_viewed_at IS NULL OR (processed_date IS NOT NULL AND student_viewed_at < processed_date))";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, studentUserId.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[HallLeaveDAO] Failed to get student unreviewed count: " + e.getMessage());
        }
        return 0;
    }

    private void verifyProvostAuthorization(String provostUserId) throws SQLException {
        String provostCheck = "SELECT 1 FROM provosts WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(provostCheck)) {
            ps.setString(1, provostUserId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SecurityException("Unauthorized: Only verified Provost accounts can process leave requests.");
                }
            }
        }
    }

    private HallLeaveRequest mapResultSetToRequest(ResultSet rs) throws SQLException {
        HallLeaveRequest req = new HallLeaveRequest();
        req.setId(rs.getInt("id"));
        req.setStudentId(rs.getString("student_id"));
        req.setStudentName(rs.getString("student_name"));
        req.setStudentRoll(rs.getString("student_id"));
        req.setStudentDept(rs.getString("student_dept"));

        req.setHallId(rs.getInt("hall_id"));
        req.setHallName(rs.getString("hall_name"));
        req.setRoomNumber(rs.getInt("room_number"));
        req.setSeatNumber(rs.getInt("seat_number"));

        req.setReason(rs.getString("reason"));

        Date reqDate = rs.getDate("request_date");
        if (reqDate != null) req.setRequestDate(reqDate.toLocalDate());

        req.setStatus(rs.getString("status"));
        req.setProcessedBy(rs.getString("processed_by"));
        req.setProcessedByName(rs.getString("processed_by_name"));

        Timestamp procTs = rs.getTimestamp("processed_date");
        if (procTs != null) req.setProcessedDate(procTs.toLocalDateTime());

        req.setDeclineReason(rs.getString("decline_reason"));

        try {
            Timestamp viewTs = rs.getTimestamp("student_viewed_at");
            if (viewTs != null) req.setStudentViewedAt(viewTs.toLocalDateTime());
        } catch (SQLException ignored) {}

        return req;
    }
}
