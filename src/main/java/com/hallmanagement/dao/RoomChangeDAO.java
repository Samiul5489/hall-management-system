package com.hallmanagement.dao;

import com.hallmanagement.database.DBConnection;
import com.hallmanagement.model.RoomChangeRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RoomChangeDAO {

    public static class EligibilityStatus {
        private final double totalDue;
        private final boolean eligible;
        private final boolean hasActiveResidence;
        private final boolean hasPendingRequest;
        private final RoomChangeRequest pendingRequest;
        private final String message;
        private final Integer currentHallId;
        private final String currentHallName;
        private final Integer currentRoom;
        private final Integer currentSeat;

        public EligibilityStatus(double totalDue, boolean eligible, boolean hasActiveResidence,
                                 boolean hasPendingRequest, RoomChangeRequest pendingRequest,
                                 String message, Integer currentHallId, String currentHallName,
                                 Integer currentRoom, Integer currentSeat) {
            this.totalDue = totalDue;
            this.eligible = eligible;
            this.hasActiveResidence = hasActiveResidence;
            this.hasPendingRequest = hasPendingRequest;
            this.pendingRequest = pendingRequest;
            this.message = message;
            this.currentHallId = currentHallId;
            this.currentHallName = currentHallName;
            this.currentRoom = currentRoom;
            this.currentSeat = currentSeat;
        }

        public double getTotalDue() { return totalDue; }
        public boolean isEligible() { return eligible; }
        public boolean isHasActiveResidence() { return hasActiveResidence; }
        public boolean isHasPendingRequest() { return hasPendingRequest; }
        public RoomChangeRequest getPendingRequest() { return pendingRequest; }
        public String getMessage() { return message; }
        public Integer getCurrentHallId() { return currentHallId; }
        public String getCurrentHallName() { return currentHallName; }
        public Integer getCurrentRoom() { return currentRoom; }
        public Integer getCurrentSeat() { return currentSeat; }
    }

    public RoomChangeDAO() {
        try {
            ensureSchemaAndSeed();
        } catch (SQLException e) {
            System.err.println("[RoomChangeDAO] Schema initialization failed: " + e.getMessage());
        }
    }

    public synchronized void ensureSchemaAndSeed() throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            String createTableSql =
                "CREATE TABLE IF NOT EXISTS room_change_requests (" +
                "    id                 INT UNSIGNED    NOT NULL AUTO_INCREMENT," +
                "    student_id         VARCHAR(50)     NOT NULL," +
                "    current_hall_id    INT UNSIGNED    NOT NULL," +
                "    current_room       INT             NOT NULL," +
                "    current_seat       INT             NOT NULL," +
                "    requested_hall_id  INT UNSIGNED    NOT NULL," +
                "    requested_room     INT             NOT NULL," +
                "    requested_seat     INT             NOT NULL," +
                "    reason             TEXT            NOT NULL," +
                "    request_date       DATE            NOT NULL," +
                "    status             ENUM('PENDING','APPROVED','REJECTED','CANCELLED') NOT NULL DEFAULT 'PENDING'," +
                "    processed_by       VARCHAR(50)     DEFAULT NULL," +
                "    processed_date     TIMESTAMP       NULL DEFAULT NULL," +
                "    rejection_reason   TEXT            DEFAULT NULL," +
                "    student_viewed_at  TIMESTAMP       NULL DEFAULT NULL," +
                "    created_at         TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "    updated_at         TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "    PRIMARY KEY (id)," +
                "    KEY idx_rcr_student (student_id)," +
                "    KEY idx_rcr_cur_hall (current_hall_id)," +
                "    KEY idx_rcr_req_hall (requested_hall_id)," +
                "    KEY idx_rcr_status (status)," +
                "    FOREIGN KEY (student_id) REFERENCES students(user_id) ON DELETE CASCADE," +
                "    FOREIGN KEY (current_hall_id) REFERENCES halls(id) ON DELETE CASCADE," +
                "    FOREIGN KEY (requested_hall_id) REFERENCES halls(id) ON DELETE CASCADE," +
                "    FOREIGN KEY (processed_by) REFERENCES provosts(user_id) ON DELETE SET NULL" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

            stmt.executeUpdate(createTableSql);

            try (ResultSet rs = conn.getMetaData().getColumns(null, null, "room_change_requests", "student_viewed_at")) {
                if (!rs.next()) {
                    stmt.executeUpdate("ALTER TABLE room_change_requests ADD COLUMN student_viewed_at TIMESTAMP NULL DEFAULT NULL");
                }
            }
        }
    }

    public EligibilityStatus checkStudentEligibility(String studentUserId) throws SQLException {
        ensureSchemaAndSeed();

        Integer currentHallId = null;
        String currentHallName = null;
        Integer currentRoom = null;
        Integer currentSeat = null;

        String studentSql =
            "SELECT s.current_hall_id, s.current_room, s.current_seat, h.hall_name " +
            "FROM students s " +
            "LEFT JOIN halls h ON h.id = s.current_hall_id " +
            "WHERE s.user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(studentSql)) {
            ps.setString(1, studentUserId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Object hallObj = rs.getObject("current_hall_id");
                    currentHallId = (hallObj instanceof Number) ? ((Number) hallObj).intValue() : null;
                    currentHallName = rs.getString("hall_name");

                    Object roomObj = rs.getObject("current_room");
                    currentRoom = (roomObj instanceof Number) ? ((Number) roomObj).intValue() : null;

                    Object seatObj = rs.getObject("current_seat");
                    currentSeat = (seatObj instanceof Number) ? ((Number) seatObj).intValue() : null;
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
                "You do not currently have an active Hall/Room/Seat, so you cannot submit a Room/Seat Change Request.",
                null, "NONE", null, null
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
                String.format("You cannot apply for a Room/Seat Change while you have an outstanding due of Tk. %.2f. Please clear your due amount first.", totalDue),
                currentHallId, currentHallName, currentRoom, currentSeat
            );
        }

        RoomChangeRequest pendingReq = null;
        String pendingSql =
            "SELECT rcr.*, st.name AS student_name, st.department AS student_dept, " +
            "       h1.hall_name AS cur_hall_name, h2.hall_name AS req_hall_name, up.name AS processed_by_name " +
            "FROM room_change_requests rcr " +
            "JOIN students st ON st.user_id = rcr.student_id " +
            "JOIN halls h1 ON h1.id = rcr.current_hall_id " +
            "JOIN halls h2 ON h2.id = rcr.requested_hall_id " +
            "LEFT JOIN provosts up ON up.user_id = rcr.processed_by " +
            "WHERE rcr.student_id = ? AND rcr.status = 'PENDING' " +
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
                "You already have a pending Room/Seat Change Request (" + pendingReq.getRequestCode() + "). Please wait for the Provost to process it.",
                currentHallId, currentHallName, currentRoom, currentSeat
            );
        }

        return new EligibilityStatus(
            0.0,
            true,
            true,
            false,
            null,
            "You are eligible to apply for Room/Seat Change. Current Due: Tk. 0",
            currentHallId, currentHallName, currentRoom, currentSeat
        );
    }

    public List<Integer> getAvailableRoomsForHall(int hallId) throws SQLException {
        ensureSchemaAndSeed();
        List<Integer> availableRooms = new ArrayList<>();

        String sql =
            "SELECT r.room_number, COUNT(s.id) AS total_seats, COUNT(st.id) AS occupied_seats " +
            "FROM rooms r " +
            "LEFT JOIN seats s ON s.room_id = r.id " +
            "LEFT JOIN students st ON st.current_hall_id = r.hall_id " +
            "                     AND st.current_room = r.room_number " +
            "                     AND st.current_seat = s.seat_number " +
            "WHERE r.hall_id = ? " +
            "GROUP BY r.id, r.room_number " +
            "HAVING (COUNT(s.id) - COUNT(st.id)) > 0 " +
            "ORDER BY r.room_number ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hallId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    availableRooms.add(rs.getInt("room_number"));
                }
            }
        }
        return availableRooms;
    }

    public List<Integer> getAvailableSeatsForRoom(int hallId, int roomNumber, Integer currentRoom, Integer currentSeat) throws SQLException {
        ensureSchemaAndSeed();
        List<Integer> availableSeats = new ArrayList<>();

        String sql =
            "SELECT s.seat_number " +
            "FROM seats s " +
            "JOIN rooms r ON s.room_id = r.id " +
            "WHERE r.hall_id = ? AND r.room_number = ? " +
            "  AND NOT EXISTS ( " +
            "      SELECT 1 FROM students st " +
            "      WHERE st.current_hall_id = ? " +
            "        AND st.current_room = ? " +
            "        AND st.current_seat = s.seat_number " +
            "  ) " +
            "ORDER BY s.seat_number ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hallId);
            ps.setInt(2, roomNumber);
            ps.setInt(3, hallId);
            ps.setInt(4, roomNumber);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int seatNum = rs.getInt("seat_number");

                    if (currentRoom != null && currentSeat != null && currentRoom == roomNumber && currentSeat == seatNum) {
                        continue;
                    }
                    availableSeats.add(seatNum);
                }
            }
        }
        return availableSeats;
    }

    public RoomChangeRequest createRoomChangeRequest(String studentId, int currentHallId, int currentRoom, int currentSeat,
                                                     int requestedRoom, int requestedSeat, String reason) throws Exception {
        ensureSchemaAndSeed();

        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Reason for Room/Seat change cannot be empty.");
        }

        if (currentRoom == requestedRoom && currentSeat == requestedSeat) {
            throw new IllegalArgumentException("Requested room and seat cannot be the same as your current residence.");
        }

        EligibilityStatus eligibility = checkStudentEligibility(studentId);
        if (!eligibility.isEligible()) {
            throw new IllegalStateException(eligibility.getMessage());
        }

        List<Integer> freeSeats = getAvailableSeatsForRoom(currentHallId, requestedRoom, currentRoom, currentSeat);
        if (!freeSeats.contains(requestedSeat)) {
            throw new IllegalStateException("The selected Room " + requestedRoom + " / Seat " + requestedSeat + " is no longer available. Please refresh and select another seat.");
        }

        String insertSql =
            "INSERT INTO room_change_requests (student_id, current_hall_id, current_room, current_seat, " +
            "                                 requested_hall_id, requested_room, requested_seat, reason, request_date, status) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURDATE(), 'PENDING')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, studentId.trim());
            ps.setInt(2, currentHallId);
            ps.setInt(3, currentRoom);
            ps.setInt(4, currentSeat);
            ps.setInt(5, currentHallId);
            ps.setInt(6, requestedRoom);
            ps.setInt(7, requestedSeat);
            ps.setString(8, reason.trim());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Failed to create room change request.");
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    return getRequestById(id);
                }
            }
        }
        throw new SQLException("Could not retrieve created room change request.");
    }

    public RoomChangeRequest updatePendingRoomChangeRequest(int requestId, String studentId, int requestedRoom, int requestedSeat, String reason) throws Exception {
        ensureSchemaAndSeed();

        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Reason for Room/Seat change cannot be empty.");
        }

        RoomChangeRequest existing = getRequestById(requestId);
        if (existing == null) {
            throw new IllegalStateException("Room change request #" + requestId + " not found.");
        }
        if (!existing.getStudentId().equalsIgnoreCase(studentId.trim())) {
            throw new IllegalStateException("Unauthorized: You can only edit your own request.");
        }
        if (!existing.isPending()) {
            throw new IllegalStateException("Only PENDING requests can be edited. This request is " + existing.getStatus() + ".");
        }

        if (existing.getCurrentRoom() == requestedRoom && existing.getCurrentSeat() == requestedSeat) {
            throw new IllegalArgumentException("Requested room and seat cannot be the same as your current residence.");
        }

        List<Integer> freeSeats = getAvailableSeatsForRoom(existing.getCurrentHallId(), requestedRoom, existing.getCurrentRoom(), existing.getCurrentSeat());
        if (!freeSeats.contains(requestedSeat)) {
            throw new IllegalStateException("The selected Room " + requestedRoom + " / Seat " + requestedSeat + " is no longer available. Please choose another seat.");
        }

        String updateSql =
            "UPDATE room_change_requests " +
            "SET requested_room = ?, requested_seat = ?, reason = ? " +
            "WHERE id = ? AND student_id = ? AND status = 'PENDING'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setInt(1, requestedRoom);
            ps.setInt(2, requestedSeat);
            ps.setString(3, reason.trim());
            ps.setInt(4, requestId);
            ps.setString(5, studentId.trim());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new IllegalStateException("Could not update request. It may have already been processed.");
            }
        }

        return getRequestById(requestId);
    }

    public boolean cancelRoomChangeRequest(int requestId, String studentId) throws Exception {
        ensureSchemaAndSeed();
        String sql = "UPDATE room_change_requests SET status = 'CANCELLED' WHERE id = ? AND student_id = ? AND status = 'PENDING'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            ps.setString(2, studentId.trim());
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new IllegalStateException("Could not cancel request. It may already be processed or cancelled.");
            }
            return true;
        }
    }

    public RoomChangeRequest getRequestById(int id) throws SQLException {
        ensureSchemaAndSeed();
        String sql =
            "SELECT rcr.*, st.name AS student_name, st.department AS student_dept, " +
            "       h1.hall_name AS cur_hall_name, h2.hall_name AS req_hall_name, up.name AS processed_by_name " +
            "FROM room_change_requests rcr " +
            "JOIN students st ON st.user_id = rcr.student_id " +
            "JOIN halls h1 ON h1.id = rcr.current_hall_id " +
            "JOIN halls h2 ON h2.id = rcr.requested_hall_id " +
            "LEFT JOIN provosts up ON up.user_id = rcr.processed_by " +
            "WHERE rcr.id = ?";

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

    public List<RoomChangeRequest> getRequestsForStudent(String studentId) throws SQLException {
        ensureSchemaAndSeed();
        List<RoomChangeRequest> list = new ArrayList<>();
        String sql =
            "SELECT rcr.*, st.name AS student_name, st.department AS student_dept, " +
            "       h1.hall_name AS cur_hall_name, h2.hall_name AS req_hall_name, up.name AS processed_by_name " +
            "FROM room_change_requests rcr " +
            "JOIN students st ON st.user_id = rcr.student_id " +
            "JOIN halls h1 ON h1.id = rcr.current_hall_id " +
            "JOIN halls h2 ON h2.id = rcr.requested_hall_id " +
            "LEFT JOIN provosts up ON up.user_id = rcr.processed_by " +
            "WHERE rcr.student_id = ? " +
            "ORDER BY rcr.id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                int index = 1;
                while (rs.next()) {
                    RoomChangeRequest req = mapResultSetToRequest(rs);
                    req.setSerial(String.format("%02d", index++));
                    list.add(req);
                }
            }
        }
        return list;
    }

    public List<RoomChangeRequest> getRequestsForProvost(String provostUserId, String keyword) throws SQLException {
        ensureSchemaAndSeed();
        List<RoomChangeRequest> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
            "SELECT rcr.*, st.name AS student_name, st.department AS student_dept, " +
            "       h1.hall_name AS cur_hall_name, h2.hall_name AS req_hall_name, up.name AS processed_by_name " +
            "FROM room_change_requests rcr " +
            "JOIN students st ON st.user_id = rcr.student_id " +
            "JOIN halls h1 ON h1.id = rcr.current_hall_id " +
            "JOIN halls h2 ON h2.id = rcr.requested_hall_id " +
            "LEFT JOIN provosts up ON up.user_id = rcr.processed_by " +
            "WHERE (rcr.current_hall_id IN (SELECT hall_id FROM provosts WHERE user_id = ?) " +
            "       OR NOT EXISTS (SELECT 1 FROM provosts WHERE user_id = ?)) "
        );

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (rcr.student_id LIKE ? OR st.name LIKE ? OR st.department LIKE ? OR rcr.reason LIKE ?) ");
        }

        sql.append("ORDER BY CASE WHEN rcr.status = 'PENDING' THEN 0 ELSE 1 END ASC, ");
        sql.append("         CASE WHEN rcr.status = 'PENDING' THEN rcr.request_date END ASC, ");
        sql.append("         rcr.id DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

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
                    RoomChangeRequest req = mapResultSetToRequest(rs);
                    req.setSerial(String.format("%02d", index++));
                    list.add(req);
                }
            }
        }
        return list;
    }

    public void approveRoomChangeRequest(int requestId, String provostUserId) throws Exception {
        ensureSchemaAndSeed();
        verifyProvostAuthorization(provostUserId);

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {

                String queryReq = "SELECT * FROM room_change_requests WHERE id = ? FOR UPDATE";
                String studentId;
                int currentHallId;
                int currentRoom;
                int currentSeat;
                int requestedHallId;
                int requestedRoom;
                int requestedSeat;
                String status;

                try (PreparedStatement psReq = conn.prepareStatement(queryReq)) {
                    psReq.setInt(1, requestId);
                    try (ResultSet rs = psReq.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalStateException("Room change request #" + requestId + " does not exist.");
                        }
                        status = rs.getString("status");
                        if (!"PENDING".equalsIgnoreCase(status)) {
                            throw new IllegalStateException("This request has already been processed (Status: " + status + ").");
                        }
                        studentId = rs.getString("student_id");
                        currentHallId = rs.getInt("current_hall_id");
                        currentRoom = rs.getInt("current_room");
                        currentSeat = rs.getInt("current_seat");
                        requestedHallId = rs.getInt("requested_hall_id");
                        requestedRoom = rs.getInt("requested_room");
                        requestedSeat = rs.getInt("requested_seat");
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

                int studentDbId;
                String studentCheckSql = "SELECT id, current_hall_id, current_room, current_seat FROM students WHERE user_id = ?";
                try (PreparedStatement psStu = conn.prepareStatement(studentCheckSql)) {
                    psStu.setString(1, studentId);
                    try (ResultSet rs = psStu.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalStateException("Student record not found for user: " + studentId);
                        }
                        studentDbId = rs.getInt("id");
                        Object curHall = rs.getObject("current_hall_id");
                        Object curRoom = rs.getObject("current_room");
                        Object curSeat = rs.getObject("current_seat");

                        if (curHall == null || curRoom == null || curSeat == null) {
                            throw new IllegalStateException("Student has no active residence to transfer from.");
                        }
                        if (((Number) curHall).intValue() != currentHallId ||
                            ((Number) curRoom).intValue() != currentRoom ||
                            ((Number) curSeat).intValue() != currentSeat) {
                            throw new IllegalStateException("Student residence has changed since application submission.");
                        }
                    }
                }

                String seatCheckSql =
                    "SELECT COUNT(*) FROM students " +
                    "WHERE current_hall_id = ? AND current_room = ? AND current_seat = ?";
                try (PreparedStatement psSeat = conn.prepareStatement(seatCheckSql)) {
                    psSeat.setInt(1, requestedHallId);
                    psSeat.setInt(2, requestedRoom);
                    psSeat.setInt(3, requestedSeat);
                    try (ResultSet rs = psSeat.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            throw new IllegalStateException("This request cannot be approved because requested Room " + requestedRoom + " / Seat " + requestedSeat + " is no longer available.");
                        }
                    }
                }

                String updateStudentSql = "UPDATE students SET current_room = ?, current_seat = ? WHERE user_id = ?";
                try (PreparedStatement psUpStu = conn.prepareStatement(updateStudentSql)) {
                    psUpStu.setInt(1, requestedRoom);
                    psUpStu.setInt(2, requestedSeat);
                    psUpStu.setString(3, studentId);
                    psUpStu.executeUpdate();
                }

                String closeHistorySql = "UPDATE student_hall_history SET end_date = CURDATE() WHERE student_id = ? AND end_date IS NULL";
                try (PreparedStatement psClose = conn.prepareStatement(closeHistorySql)) {
                    psClose.setInt(1, studentDbId);
                    psClose.executeUpdate();
                }

                String insertHistorySql = "INSERT INTO student_hall_history (student_id, hall_id, room_number, seat_number, start_date, end_date) " +
                                          "VALUES (?, ?, ?, ?, CURDATE(), NULL)";
                try (PreparedStatement psIns = conn.prepareStatement(insertHistorySql)) {
                    psIns.setInt(1, studentDbId);
                    psIns.setInt(2, requestedHallId);
                    psIns.setInt(3, requestedRoom);
                    psIns.setInt(4, requestedSeat);
                    psIns.executeUpdate();
                }

                String updateReqSql = "UPDATE room_change_requests SET status = 'APPROVED', processed_by = ?, processed_date = CURRENT_TIMESTAMP WHERE id = ?";
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

    public void rejectRoomChangeRequest(int requestId, String provostUserId, String rejectionReason) throws Exception {
        ensureSchemaAndSeed();
        verifyProvostAuthorization(provostUserId);

        if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
            throw new IllegalArgumentException("Rejection reason cannot be empty.");
        }

        String sql = "UPDATE room_change_requests SET status = 'REJECTED', processed_by = ?, processed_date = CURRENT_TIMESTAMP, rejection_reason = ? " +
                     "WHERE id = ? AND status = 'PENDING'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, provostUserId);
            ps.setString(2, rejectionReason.trim());
            ps.setInt(3, requestId);

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new IllegalStateException("Could not reject request. It may have already been processed.");
            }
        }
    }

    public int getPendingCountForProvost(String provostUserId) {
        try {
            ensureSchemaAndSeed();
            String sql = "SELECT COUNT(*) FROM room_change_requests " +
                         "WHERE status = 'PENDING' " +
                         "  AND (current_hall_id IN (SELECT hall_id FROM provosts WHERE user_id = ?) " +
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
            System.err.println("[RoomChangeDAO] Error counting pending requests for provost: " + e.getMessage());
        }
        return 0;
    }

    public int getUnreviewedProcessedCountForStudent(String studentUserId) {
        try {
            ensureSchemaAndSeed();
            String sql = "SELECT COUNT(*) FROM room_change_requests " +
                         "WHERE student_id = ? " +
                         "  AND status IN ('APPROVED', 'REJECTED') " +
                         "  AND student_viewed_at IS NULL";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, studentUserId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[RoomChangeDAO] Error counting unreviewed requests for student: " + e.getMessage());
        }
        return 0;
    }

    public void markRequestAsReviewedByStudent(int requestId, String studentUserId) {
        try {
            ensureSchemaAndSeed();
            String sql = "UPDATE room_change_requests SET student_viewed_at = CURRENT_TIMESTAMP WHERE id = ? AND student_id = ? AND student_viewed_at IS NULL";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, requestId);
                ps.setString(2, studentUserId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("[RoomChangeDAO] Error marking request as reviewed: " + e.getMessage());
        }
    }

    private void verifyProvostAuthorization(String provostUserId) throws SQLException {
        if (provostUserId == null || provostUserId.trim().isEmpty()) {
            throw new IllegalArgumentException("Provost user ID is required.");
        }
    }

    private RoomChangeRequest mapResultSetToRequest(ResultSet rs) throws SQLException {
        RoomChangeRequest req = new RoomChangeRequest();
        req.setId(rs.getInt("id"));
        req.setStudentId(rs.getString("student_id"));
        req.setStudentName(rs.getString("student_name"));
        req.setStudentRoll(rs.getString("student_id"));
        req.setStudentDept(rs.getString("student_dept"));

        req.setCurrentHallId(rs.getInt("current_hall_id"));
        req.setCurrentHallName(rs.getString("cur_hall_name"));
        req.setCurrentRoom(rs.getInt("current_room"));
        req.setCurrentSeat(rs.getInt("current_seat"));

        req.setRequestedHallId(rs.getInt("requested_hall_id"));
        req.setRequestedHallName(rs.getString("req_hall_name"));
        req.setRequestedRoom(rs.getInt("requested_room"));
        req.setRequestedSeat(rs.getInt("requested_seat"));

        req.setReason(rs.getString("reason"));

        java.sql.Date reqDate = rs.getDate("request_date");
        if (reqDate != null) {
            req.setRequestDate(reqDate.toLocalDate());
        }

        req.setStatus(rs.getString("status"));
        req.setProcessedBy(rs.getString("processed_by"));
        req.setProcessedByName(rs.getString("processed_by_name"));

        java.sql.Timestamp procDate = rs.getTimestamp("processed_date");
        if (procDate != null) {
            req.setProcessedDate(procDate.toLocalDateTime());
        }

        req.setRejectionReason(rs.getString("rejection_reason"));

        java.sql.Timestamp viewedAt = rs.getTimestamp("student_viewed_at");
        if (viewedAt != null) {
            req.setStudentViewedAt(viewedAt.toLocalDateTime());
        }

        req.setCreatedAt(rs.getTimestamp("created_at"));
        req.setUpdatedAt(rs.getTimestamp("updated_at"));

        return req;
    }
}
