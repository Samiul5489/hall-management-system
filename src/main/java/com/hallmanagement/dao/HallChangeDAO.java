package com.hallmanagement.dao;

import com.hallmanagement.database.DBConnection;
import com.hallmanagement.model.HallChangeRequest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HallChangeDAO {

    public static class EligibilityStatus {
        private final double totalDue;
        private final boolean hasPendingRequest;
        private final HallChangeRequest pendingRequest;
        private final boolean eligible;
        private final String message;

        public EligibilityStatus(double totalDue, boolean hasPendingRequest, HallChangeRequest pendingRequest, boolean eligible, String message) {
            this.totalDue = totalDue;
            this.hasPendingRequest = hasPendingRequest;
            this.pendingRequest = pendingRequest;
            this.eligible = eligible;
            this.message = message;
        }

        public double getTotalDue() {
            return totalDue;
        }

        public boolean isHasPendingRequest() {
            return hasPendingRequest;
        }

        public HallChangeRequest getPendingRequest() {
            return pendingRequest;
        }

        public boolean isEligible() {
            return eligible;
        }

        public String getMessage() {
            return message;
        }
    }

    public HallChangeDAO() {
        try {
            ensureSchemaAndSeed();
        } catch (SQLException e) {
            System.err.println("[HallChangeDAO] Failed to ensure schema: " + e.getMessage());
        }
    }

    public synchronized void ensureSchemaAndSeed() throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            String createTableSql =
                "CREATE TABLE IF NOT EXISTS hall_change_requests (" +
                "    id                 INT UNSIGNED    NOT NULL AUTO_INCREMENT," +
                "    request_code       VARCHAR(20)     NOT NULL," +
                "    student_id         VARCHAR(50)     NOT NULL," +
                "    current_hall_id    INT UNSIGNED    NOT NULL," +
                "    current_room       INT             NOT NULL," +
                "    current_seat       INT             NOT NULL," +
                "    requested_hall_id  INT UNSIGNED    NOT NULL," +
                "    requested_room     INT             NOT NULL," +
                "    requested_seat     INT             NOT NULL," +
                "    request_date       DATE            NOT NULL," +
                "    status             ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING'," +
                "    processed_by       VARCHAR(50)     DEFAULT NULL," +
                "    processed_date     TIMESTAMP       NULL DEFAULT NULL," +
                "    rejection_reason   TEXT            DEFAULT NULL," +
                "    created_at         TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "    updated_at         TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "    PRIMARY KEY (id)," +
                "    UNIQUE KEY uq_change_request_code (request_code)," +
                "    FOREIGN KEY (student_id) REFERENCES students(user_id) ON DELETE CASCADE," +
                "    FOREIGN KEY (current_hall_id) REFERENCES halls(id) ON DELETE CASCADE," +
                "    FOREIGN KEY (requested_hall_id) REFERENCES halls(id) ON DELETE CASCADE," +
                "    FOREIGN KEY (processed_by) REFERENCES provosts(user_id) ON DELETE SET NULL" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

            stmt.executeUpdate(createTableSql);
        }
    }

    public EligibilityStatus checkStudentEligibility(String studentUserId) throws SQLException {
        ensureSchemaAndSeed();

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
                null,
                false,
                String.format("You cannot apply for a hall change because you have an outstanding due of Tk. %.2f. Please clear your due first.", totalDue)
            );
        }

        HallChangeRequest pendingReq = null;
        String pendingSql =
            "SELECT hcr.*, st.name AS student_name, st.department AS student_dept, " +
            "       h1.hall_name AS current_hall_name, h2.hall_name AS requested_hall_name, " +
            "       up.name AS processed_by_name " +
            "FROM hall_change_requests hcr " +
            "JOIN students st ON st.user_id = hcr.student_id " +
            "JOIN halls h1 ON h1.id = hcr.current_hall_id " +
            "JOIN halls h2 ON h2.id = hcr.requested_hall_id " +
            "LEFT JOIN provosts up ON up.user_id = hcr.processed_by " +
            "WHERE hcr.student_id = ? AND hcr.status = 'PENDING' " +
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
                true,
                pendingReq,
                false,
                "You already have a pending hall change request (" + pendingReq.getRequestCode() + "). Please wait for the Provost to process it."
            );
        }

        return new EligibilityStatus(
            0.0,
            false,
            null,
            true,
            "You are eligible to apply for a hall change."
        );
    }

    public HallChangeRequest createHallChangeRequest(String studentUserId, int requestedHallId, int requestedRoom, int requestedSeat) throws Exception {
        ensureSchemaAndSeed();

        EligibilityStatus status = checkStudentEligibility(studentUserId);
        if (!status.isEligible()) {
            throw new IllegalStateException(status.getMessage());
        }

        int curHallId;
        int curRoom;
        int curSeat;
        String curSql = "SELECT current_hall_id, current_room, current_seat FROM students WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(curSql)) {
            ps.setString(1, studentUserId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || rs.getObject("current_hall_id") == null) {
                    throw new IllegalStateException("Student has no active residence assignment to transfer from.");
                }
                curHallId = rs.getInt("current_hall_id");
                curRoom = rs.getInt("current_room");
                curSeat = rs.getInt("current_seat");
            }
        }

        if (curHallId == requestedHallId && curRoom == requestedRoom && curSeat == requestedSeat) {
            throw new IllegalArgumentException("Requested residence cannot be the same as your current residence.");
        }

        String seatSql = "SELECT COUNT(*) FROM students WHERE current_hall_id = ? AND current_room = ? AND current_seat = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(seatSql)) {
            ps.setInt(1, requestedHallId);
            ps.setInt(2, requestedRoom);
            ps.setInt(3, requestedSeat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new IllegalStateException("The selected seat (Room " + requestedRoom + ", Seat " + requestedSeat + ") is no longer available.");
                }
            }
        }

        String reqCode = generateNextRequestCode();

        String insertSql =
            "INSERT INTO hall_change_requests " +
            "(request_code, student_id, current_hall_id, current_room, current_seat, requested_hall_id, requested_room, requested_seat, request_date, status) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURDATE(), 'PENDING')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, reqCode);
            ps.setString(2, studentUserId);
            ps.setInt(3, curHallId);
            ps.setInt(4, curRoom);
            ps.setInt(5, curSeat);
            ps.setInt(6, requestedHallId);
            ps.setInt(7, requestedRoom);
            ps.setInt(8, requestedSeat);

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        return getRequestById(id);
                    }
                }
            }
        }

        throw new SQLException("Failed to create hall change request.");
    }

    public void cancelHallChangeRequest(int requestId, String studentUserId) throws Exception {
        ensureSchemaAndSeed();
        String sql = "DELETE FROM hall_change_requests WHERE id = ? AND student_id = ? AND status = 'PENDING'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            ps.setString(2, studentUserId);
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new IllegalStateException("Could not cancel request. It may have already been processed or does not belong to you.");
            }
        }
    }

    public HallChangeRequest updatePendingHallChangeRequest(int requestId, String studentUserId, int newRequestedHallId, int newRequestedRoom, int newRequestedSeat) throws Exception {
        ensureSchemaAndSeed();

        HallChangeRequest existing = getRequestById(requestId);
        if (existing == null || !existing.getStudentId().equals(studentUserId)) {
            throw new IllegalStateException("Hall change request not found.");
        }
        if (!existing.isPending()) {
            throw new IllegalStateException("Cannot edit request. It has already been " + existing.getStatus() + ".");
        }

        if (existing.getCurrentHallId() == newRequestedHallId && existing.getCurrentRoom() == newRequestedRoom && existing.getCurrentSeat() == newRequestedSeat) {
            throw new IllegalArgumentException("Requested residence cannot be the same as your current residence.");
        }

        String seatSql = "SELECT COUNT(*) FROM students WHERE current_hall_id = ? AND current_room = ? AND current_seat = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(seatSql)) {
            ps.setInt(1, newRequestedHallId);
            ps.setInt(2, newRequestedRoom);
            ps.setInt(3, newRequestedSeat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new IllegalStateException("The selected seat (Room " + newRequestedRoom + ", Seat " + newRequestedSeat + ") is already occupied.");
                }
            }
        }

        String updateSql =
            "UPDATE hall_change_requests " +
            "SET requested_hall_id = ?, requested_room = ?, requested_seat = ?, updated_at = NOW() " +
            "WHERE id = ? AND student_id = ? AND status = 'PENDING'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setInt(1, newRequestedHallId);
            ps.setInt(2, newRequestedRoom);
            ps.setInt(3, newRequestedSeat);
            ps.setInt(4, requestId);
            ps.setString(5, studentUserId);

            int affected = ps.executeUpdate();
            if (affected > 0) {
                return getRequestById(requestId);
            }
        }

        throw new SQLException("Failed to update hall change request.");
    }

    public List<HallChangeRequest> getRequestsForStudent(String studentUserId) throws SQLException {
        ensureSchemaAndSeed();
        List<HallChangeRequest> list = new ArrayList<>();
        String sql =
            "SELECT hcr.*, st.name AS student_name, st.department AS student_dept, " +
            "       h1.hall_name AS current_hall_name, h2.hall_name AS requested_hall_name, " +
            "       up.name AS processed_by_name " +
            "FROM hall_change_requests hcr " +
            "JOIN students st ON st.user_id = hcr.student_id " +
            "JOIN halls h1 ON h1.id = hcr.current_hall_id " +
            "JOIN halls h2 ON h2.id = hcr.requested_hall_id " +
            "LEFT JOIN provosts up ON up.user_id = hcr.processed_by " +
            "WHERE hcr.student_id = ? " +
            "ORDER BY hcr.id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, studentUserId);
            try (ResultSet rs = ps.executeQuery()) {
                int index = 1;
                while (rs.next()) {
                    HallChangeRequest req = mapResultSetToRequest(rs);
                    req.setSerial(String.format("%02d", index++));
                    list.add(req);
                }
            }
        }
        return list;
    }

    public List<HallChangeRequest> getPendingRequestsForProvost(String provostUserId) throws SQLException {
        ensureSchemaAndSeed();
        List<HallChangeRequest> list = new ArrayList<>();
        String sql =
            "SELECT hcr.*, st.name AS student_name, st.department AS student_dept, " +
            "       h1.hall_name AS current_hall_name, h2.hall_name AS requested_hall_name, " +
            "       up.name AS processed_by_name " +
            "FROM hall_change_requests hcr " +
            "JOIN students st ON st.user_id = hcr.student_id " +
            "JOIN halls h1 ON h1.id = hcr.current_hall_id " +
            "JOIN halls h2 ON h2.id = hcr.requested_hall_id " +
            "LEFT JOIN provosts up ON up.user_id = hcr.processed_by " +
            "WHERE hcr.status = 'PENDING' " +
            "  AND (hcr.requested_hall_id IN (SELECT hall_id FROM provosts WHERE user_id = ?) " +
            "       OR hcr.current_hall_id IN (SELECT hall_id FROM provosts WHERE user_id = ?) " +
            "       OR NOT EXISTS (SELECT 1 FROM provosts WHERE user_id = ?)) " +
            "ORDER BY hcr.id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, provostUserId);
            ps.setString(2, provostUserId);
            ps.setString(3, provostUserId);
            try (ResultSet rs = ps.executeQuery()) {
                int index = 1;
                while (rs.next()) {
                    HallChangeRequest req = mapResultSetToRequest(rs);
                    req.setSerial(String.format("%02d", index++));
                    list.add(req);
                }
            }
        }
        return list;
    }

    public List<HallChangeRequest> getAllRequestsForProvost(String provostUserId, String keyword) throws SQLException {
        ensureSchemaAndSeed();
        List<HallChangeRequest> list = new ArrayList<>();
        String sql =
            "SELECT hcr.*, st.name AS student_name, st.department AS student_dept, " +
            "       h1.hall_name AS current_hall_name, h2.hall_name AS requested_hall_name, " +
            "       up.name AS processed_by_name " +
            "FROM hall_change_requests hcr " +
            "JOIN students st ON st.user_id = hcr.student_id " +
            "JOIN halls h1 ON h1.id = hcr.current_hall_id " +
            "JOIN halls h2 ON h2.id = hcr.requested_hall_id " +
            "LEFT JOIN provosts up ON up.user_id = hcr.processed_by " +
            "WHERE (hcr.requested_hall_id IN (SELECT hall_id FROM provosts WHERE user_id = ?) " +
            "       OR hcr.current_hall_id IN (SELECT hall_id FROM provosts WHERE user_id = ?) " +
            "       OR NOT EXISTS (SELECT 1 FROM provosts WHERE user_id = ?)) ";

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql += " AND (hcr.request_code LIKE ? OR st.name LIKE ? OR hcr.student_id LIKE ? OR h2.hall_name LIKE ?) ";
        }
        sql += " ORDER BY hcr.id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, provostUserId);
            ps.setString(2, provostUserId);
            ps.setString(3, provostUserId);

            if (keyword != null && !keyword.trim().isEmpty()) {
                String term = "%" + keyword.trim() + "%";
                ps.setString(4, term);
                ps.setString(5, term);
                ps.setString(6, term);
                ps.setString(7, term);
            }

            try (ResultSet rs = ps.executeQuery()) {
                int index = 1;
                while (rs.next()) {
                    HallChangeRequest req = mapResultSetToRequest(rs);
                    req.setSerial(String.format("%02d", index++));
                    list.add(req);
                }
            }
        }
        return list;
    }

    public int getPendingRequestCountForProvost(String provostUserId) {
        if (provostUserId == null || provostUserId.trim().isEmpty()) return 0;
        try {
            ensureSchemaAndSeed();
            String sql =
                "SELECT COUNT(*) FROM hall_change_requests hcr " +
                "WHERE hcr.status = 'PENDING' " +
                "  AND (hcr.requested_hall_id IN (SELECT hall_id FROM provosts WHERE user_id = ?) " +
                "       OR hcr.current_hall_id IN (SELECT hall_id FROM provosts WHERE user_id = ?) " +
                "       OR NOT EXISTS (SELECT 1 FROM provosts WHERE user_id = ?))";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, provostUserId);
                ps.setString(2, provostUserId);
                ps.setString(3, provostUserId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[HallChangeDAO] Failed to get pending count: " + e.getMessage());
        }
        return 0;
    }

    public int getPendingRequestCountForStudent(String studentUserId) {
        if (studentUserId == null || studentUserId.trim().isEmpty()) return 0;
        try {
            ensureSchemaAndSeed();
            String sql = "SELECT COUNT(*) FROM hall_change_requests WHERE student_id = ? AND status = 'PENDING'";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, studentUserId.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[HallChangeDAO] Failed to get student pending count: " + e.getMessage());
        }
        return 0;
    }

    public void approveHallChangeRequest(int requestId, String provostUserId) throws Exception {
        ensureSchemaAndSeed();
        verifyProvostAuthorization(provostUserId);

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {

                String queryReq = "SELECT * FROM hall_change_requests WHERE id = ? FOR UPDATE";
                String studentId;
                int reqHallId;
                int reqRoom;
                int reqSeat;
                String status;

                try (PreparedStatement psReq = conn.prepareStatement(queryReq)) {
                    psReq.setInt(1, requestId);
                    try (ResultSet rs = psReq.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalStateException("Hall change request #" + requestId + " does not exist.");
                        }
                        status = rs.getString("status");
                        if (!"PENDING".equalsIgnoreCase(status)) {
                            throw new IllegalStateException("Cannot approve request. Current status is already " + status + ".");
                        }
                        studentId = rs.getString("student_id");
                        reqHallId = rs.getInt("requested_hall_id");
                        reqRoom = rs.getInt("requested_room");
                        reqSeat = rs.getInt("requested_seat");
                    }
                }

                String dueSql = "SELECT COALESCE(SUM(due_amount), 0.0) FROM bills WHERE student_id = ?";
                try (PreparedStatement psDue = conn.prepareStatement(dueSql)) {
                    psDue.setString(1, studentId);
                    try (ResultSet rs = psDue.executeQuery()) {
                        if (rs.next()) {
                            double due = rs.getDouble(1);
                            if (due > 0.0) {
                                throw new IllegalStateException(String.format("Cannot approve this hall change. Student has an outstanding due of Tk. %.2f.", due));
                            }
                        }
                    }
                }

                String seatCheckSql = "SELECT COUNT(*) FROM students WHERE current_hall_id = ? AND current_room = ? AND current_seat = ?";
                try (PreparedStatement psSeat = conn.prepareStatement(seatCheckSql)) {
                    psSeat.setInt(1, reqHallId);
                    psSeat.setInt(2, reqRoom);
                    psSeat.setInt(3, reqSeat);
                    try (ResultSet rs = psSeat.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            throw new IllegalStateException("Cannot approve. The requested seat (Room " + reqRoom + ", Seat " + reqSeat + ") is no longer available.");
                        }
                    }
                }

                String updateStudentSql = "UPDATE students SET current_hall_id = ?, current_room = ?, current_seat = ? WHERE user_id = ?";
                try (PreparedStatement psUpStudent = conn.prepareStatement(updateStudentSql)) {
                    psUpStudent.setInt(1, reqHallId);
                    psUpStudent.setInt(2, reqRoom);
                    psUpStudent.setInt(3, reqSeat);
                    psUpStudent.setString(4, studentId);
                    int affected = psUpStudent.executeUpdate();
                    if (affected <= 0) {
                        throw new SQLException("Failed to update student residence record.");
                    }
                }

                String closeHistorySql =
                    "UPDATE student_hall_history SET end_date = CURDATE() " +
                    "WHERE student_id = (SELECT id FROM students WHERE user_id = ?) AND end_date IS NULL";
                try (PreparedStatement psCloseHist = conn.prepareStatement(closeHistorySql)) {
                    psCloseHist.setString(1, studentId);
                    psCloseHist.executeUpdate();
                }

                String insertHistorySql =
                    "INSERT INTO student_hall_history (student_id, hall_id, room_number, seat_number, start_date, end_date) " +
                    "VALUES ((SELECT id FROM students WHERE user_id = ?), ?, ?, ?, CURDATE(), NULL)";
                try (PreparedStatement psNewHist = conn.prepareStatement(insertHistorySql)) {
                    psNewHist.setString(1, studentId);
                    psNewHist.setInt(2, reqHallId);
                    psNewHist.setInt(3, reqRoom);
                    psNewHist.setInt(4, reqSeat);
                    psNewHist.executeUpdate();
                }

                String updateReqSql =
                    "UPDATE hall_change_requests " +
                    "SET status = 'APPROVED', processed_by = ?, processed_date = NOW() " +
                    "WHERE id = ?";
                try (PreparedStatement psUpReq = conn.prepareStatement(updateReqSql)) {
                    psUpReq.setString(1, provostUserId);
                    psUpReq.setInt(2, requestId);
                    psUpReq.executeUpdate();
                }

                conn.commit();

            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public void rejectHallChangeRequest(int requestId, String provostUserId, String reason) throws Exception {
        ensureSchemaAndSeed();
        verifyProvostAuthorization(provostUserId);

        String sql =
            "UPDATE hall_change_requests " +
            "SET status = 'REJECTED', processed_by = ?, processed_date = NOW(), rejection_reason = ? " +
            "WHERE id = ? AND status = 'PENDING'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, provostUserId);
            ps.setString(2, (reason != null && !reason.trim().isEmpty()) ? reason.trim() : "Seat transfer application rejected by Provost.");
            ps.setInt(3, requestId);

            int affected = ps.executeUpdate();
            if (affected <= 0) {
                throw new IllegalStateException("Failed to reject request. It may have already been processed.");
            }
        }
    }

    public HallChangeRequest getRequestById(int id) throws SQLException {
        ensureSchemaAndSeed();
        String sql =
            "SELECT hcr.*, st.name AS student_name, st.department AS student_dept, " +
            "       h1.hall_name AS current_hall_name, h2.hall_name AS requested_hall_name, " +
            "       up.name AS processed_by_name " +
            "FROM hall_change_requests hcr " +
            "JOIN students st ON st.user_id = hcr.student_id " +
            "JOIN halls h1 ON h1.id = hcr.current_hall_id " +
            "JOIN halls h2 ON h2.id = hcr.requested_hall_id " +
            "LEFT JOIN provosts up ON up.user_id = hcr.processed_by " +
            "WHERE hcr.id = ?";

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

    private String generateNextRequestCode() throws SQLException {
        String sql = "SELECT MAX(id) FROM hall_change_requests";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            int nextId = 1;
            if (rs.next()) {
                nextId = rs.getInt(1) + 1;
            }
            return String.format("HCR-%03d", nextId);
        }
    }

    private HallChangeRequest mapResultSetToRequest(ResultSet rs) throws SQLException {
        return new HallChangeRequest(
            rs.getInt("id"),
            rs.getString("request_code"),
            rs.getString("student_id"),
            rs.getString("student_name"),
            rs.getString("student_id"),
            rs.getString("student_dept"),
            rs.getInt("current_hall_id"),
            rs.getString("current_hall_name"),
            rs.getInt("current_room"),
            rs.getInt("current_seat"),
            rs.getInt("requested_hall_id"),
            rs.getString("requested_hall_name"),
            rs.getInt("requested_room"),
            rs.getInt("requested_seat"),
            rs.getDate("request_date"),
            rs.getString("status"),
            rs.getString("processed_by"),
            rs.getString("processed_by_name"),
            rs.getTimestamp("processed_date"),
            rs.getString("rejection_reason"),
            rs.getTimestamp("created_at"),
            rs.getTimestamp("updated_at")
        );
    }

    private void verifyProvostAuthorization(String userId) throws SQLException, SecurityException {
        String roleSql = "SELECT user_id FROM provosts WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(roleSql)) {

            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SecurityException("Unauthorized action: Only Provost / Admin authority can process hall change requests.");
                }
            }
        }
    }
}
