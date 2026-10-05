package com.hallmanagement;

import com.hallmanagement.dao.RoomChangeDAO;
import com.hallmanagement.database.DBConnection;
import com.hallmanagement.model.RoomChangeRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

public class RoomChangeSmokeTest {

    private static final RoomChangeDAO roomChangeDAO = new RoomChangeDAO();

    private static int totalPassed = 0;
    private static int totalFailed = 0;

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("  STARTING ROOM/SEAT CHANGE SYSTEM SMOKE TESTS (18 TEST CASES) ");
        System.out.println("===============================================================");

        try {
            setupTestData();

            runTest("Test 01: Schema and Table Initialization", RoomChangeSmokeTest::test01_SchemaInitialization);
            runTest("Test 02: Ineligible When Due > 0", RoomChangeSmokeTest::test02_IneligibleWhenDuePositive);
            runTest("Test 03: Ineligible When No Active Residence", RoomChangeSmokeTest::test03_IneligibleWhenNoActiveResidence);
            runTest("Test 04: Eligible When Active Residence and Due == 0", RoomChangeSmokeTest::test04_EligibleWhenActiveResidenceAndZeroDue);
            runTest("Test 05: Available Rooms Query Returns Valid Rooms", RoomChangeSmokeTest::test05_AvailableRoomsQuery);
            runTest("Test 06: Available Seats Query Excludes Current Seat", RoomChangeSmokeTest::test06_AvailableSeatsQuery);
            runTest("Test 07: Submission Blocked for Same Room and Seat", RoomChangeSmokeTest::test07_SubmissionBlockedWhenSelectingSameRoomAndSeat);
            runTest("Test 08: Submission Blocked When Reason is Empty", RoomChangeSmokeTest::test08_SubmissionBlockedWhenReasonEmpty);
            runTest("Test 09: Successful Request Submission (Status PENDING)", RoomChangeSmokeTest::test09_SuccessfulRequestSubmission);
            runTest("Test 10: Duplicate Pending Request Blocked", RoomChangeSmokeTest::test10_DuplicatePendingRequestBlocked);
            runTest("Test 11: Student Edit Pending Request", RoomChangeSmokeTest::test11_StudentEditPendingRequest);
            runTest("Test 12: Student Cancel Pending Request", RoomChangeSmokeTest::test12_StudentCancelPendingRequest);
            runTest("Test 13: Provost Reject Request With Mandatory Reason", RoomChangeSmokeTest::test13_ProvostRejectRequestWithReason);
            runTest("Test 14: Provost Atomic ACID Approval (Seat Swap & History)", RoomChangeSmokeTest::test14_ProvostApproveRequestACID);
            runTest("Test 15: Concurrency Protection (Seat Becomes Occupied)", RoomChangeSmokeTest::test15_ConcurrencyProtectionOnOccupiedSeat);
            runTest("Test 16: Security Protection (Due Incurred Before Approval)", RoomChangeSmokeTest::test16_SecurityProtectionOnNewDue);
            runTest("Test 17: Duplicate Approval Blocked on Processed Request", RoomChangeSmokeTest::test17_DuplicateApprovalBlocked);
            runTest("Test 18: Notification Badge Counter and Mark As Reviewed", RoomChangeSmokeTest::test18_NotificationBadgeAndMarkReviewed);

        } catch (Exception e) {
            System.err.println("FATAL TEST SETUP ERROR: " + e.getMessage());
            e.printStackTrace();
        } finally {
            cleanupTestData();
            System.out.println("===============================================================");
            System.out.println(String.format("  TEST SUMMARY: %d PASSED, %d FAILED (TOTAL %d)",
                    totalPassed, totalFailed, totalPassed + totalFailed));
            System.out.println("===============================================================");

            if (totalFailed > 0) {
                System.exit(1);
            }
        }
    }

    private static void runTest(String testName, TestRunnable test) {
        System.out.print(String.format("[RUNNING] %-60s ... ", testName));
        try {
            test.run();
            System.out.println("✅ PASS");
            totalPassed++;
        } catch (Throwable t) {
            System.out.println("❌ FAIL: " + t.getMessage());
            t.printStackTrace(System.out);
            totalFailed++;
        }
    }

    @FunctionalInterface
    interface TestRunnable {
        void run() throws Exception;
    }

    private static final String TEST_STUDENT_DUE   = "student_test_rc_due";
    private static final String TEST_STUDENT_NOHALL= "student_test_rc_nohall";
    private static final String TEST_STUDENT_OK    = "student_test_rc_ok";
    private static final String TEST_STUDENT_SWAP  = "student_test_rc_swap";
    private static final String TEST_PROVOST       = "provost_test_rc";

    private static int testHallId = 1;

    private static void setupTestData() throws Exception {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("INSERT IGNORE INTO halls (id, hall_name, hall_type) VALUES (1, 'Shahid Shahidul Islam Hall', 'MALE')");

            stmt.executeUpdate("INSERT INTO provosts (user_id, name, password, phone, age, email, designation, hall_id) " +
                    "VALUES ('" + TEST_PROVOST + "', 'Prof. Test Provost', 'pass123', '01711000000', 48, 'provost_rc@ruet.ac.bd', 'Provost', 1) " +
                    "ON DUPLICATE KEY UPDATE name='Prof. Test Provost', hall_id=1");

            stmt.executeUpdate("DELETE FROM room_change_requests WHERE student_id IN ('" +
                    TEST_STUDENT_DUE + "', '" + TEST_STUDENT_NOHALL + "', '" + TEST_STUDENT_OK + "', '" + TEST_STUDENT_SWAP + "')");
            stmt.executeUpdate("DELETE FROM bills WHERE student_id IN ('" +
                    TEST_STUDENT_DUE + "', '" + TEST_STUDENT_NOHALL + "', '" + TEST_STUDENT_OK + "', '" + TEST_STUDENT_SWAP + "')");
            stmt.executeUpdate("DELETE FROM student_hall_history WHERE student_id IN (" +
                    "SELECT id FROM students WHERE user_id IN ('" + TEST_STUDENT_DUE + "', '" + TEST_STUDENT_NOHALL + "', '" + TEST_STUDENT_OK + "', '" + TEST_STUDENT_SWAP + "'))");
            stmt.executeUpdate("DELETE FROM students WHERE user_id IN ('" +
                    TEST_STUDENT_DUE + "', '" + TEST_STUDENT_NOHALL + "', '" + TEST_STUDENT_OK + "', '" + TEST_STUDENT_SWAP + "')");

            for (int r = 801; r <= 804; r++) {
                stmt.executeUpdate("INSERT INTO rooms (hall_id, room_number, total_seats) VALUES (1, " + r + ", 4) " +
                        "ON DUPLICATE KEY UPDATE total_seats = 4");
                int roomId = -1;
                try (ResultSet rs = stmt.executeQuery("SELECT id FROM rooms WHERE hall_id = 1 AND room_number = " + r)) {
                    if (rs.next()) roomId = rs.getInt(1);
                }
                if (roomId != -1) {
                    for (int s = 1; s <= 4; s++) {
                        stmt.executeUpdate("INSERT IGNORE INTO seats (room_id, seat_number) VALUES (" + roomId + ", " + s + ")");
                    }
                }
            }

            stmt.executeUpdate("INSERT INTO students (user_id, name, password, department, phone, year, current_hall_id, current_room, current_seat) " +
                    "VALUES ('" + TEST_STUDENT_DUE + "', 'Student With Due', 'pass123', 'CSE', '01711111111', '4th Year', 1, 801, 1)");
            stmt.executeUpdate("INSERT INTO bills (student_id, hall_id, room_number, seat_number, billing_period, hall_charge, electricity_charge, water_charge, maintenance_charge, other_charge, total_amount, paid_amount, due_amount, status) " +
                    "VALUES ('" + TEST_STUDENT_DUE + "', 1, 801, 1, 'August 2026', 500, 200, 100, 100, 100, 1000, 200, 800.00, 'DUE')");

            stmt.executeUpdate("INSERT INTO students (user_id, name, password, department, phone, year, current_hall_id, current_room, current_seat) " +
                    "VALUES ('" + TEST_STUDENT_NOHALL + "', 'Student No Hall', 'pass123', 'EEE', '01722222222', '1st Year', NULL, NULL, NULL)");

            stmt.executeUpdate("INSERT INTO students (user_id, name, password, department, phone, year, current_hall_id, current_room, current_seat) " +
                    "VALUES ('" + TEST_STUDENT_OK + "', 'Student Eligible', 'pass123', 'ME', '01733333333', '3rd Year', 1, 801, 2)");
            stmt.executeUpdate("INSERT INTO bills (student_id, hall_id, room_number, seat_number, billing_period, hall_charge, electricity_charge, water_charge, maintenance_charge, other_charge, total_amount, paid_amount, due_amount, status) " +
                    "VALUES ('" + TEST_STUDENT_OK + "', 1, 801, 2, 'August 2026', 500, 200, 100, 100, 100, 1000, 1000, 0.00, 'PAID')");

            stmt.executeUpdate("INSERT INTO students (user_id, name, password, department, phone, year, current_hall_id, current_room, current_seat) " +
                    "VALUES ('" + TEST_STUDENT_SWAP + "', 'Student Swap Test', 'pass123', 'CE', '01744444444', '4th Year', 1, 802, 1)");
            stmt.executeUpdate("INSERT INTO bills (student_id, hall_id, room_number, seat_number, billing_period, hall_charge, electricity_charge, water_charge, maintenance_charge, other_charge, total_amount, paid_amount, due_amount, status) " +
                    "VALUES ('" + TEST_STUDENT_SWAP + "', 1, 802, 1, 'August 2026', 500, 200, 100, 100, 100, 1000, 1000, 0.00, 'PAID')");

            stmt.executeUpdate("INSERT INTO student_hall_history (student_id, hall_id, room_number, seat_number, start_date, end_date) " +
                    "VALUES ((SELECT id FROM students WHERE user_id = '" + TEST_STUDENT_SWAP + "'), 1, 802, 1, '2025-01-01', NULL)");
        }
    }

    private static void cleanupTestData() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM room_change_requests WHERE student_id IN ('" +
                    TEST_STUDENT_DUE + "', '" + TEST_STUDENT_NOHALL + "', '" + TEST_STUDENT_OK + "', '" + TEST_STUDENT_SWAP + "')");
            stmt.executeUpdate("DELETE FROM bills WHERE student_id IN ('" +
                    TEST_STUDENT_DUE + "', '" + TEST_STUDENT_NOHALL + "', '" + TEST_STUDENT_OK + "', '" + TEST_STUDENT_SWAP + "')");
            stmt.executeUpdate("DELETE FROM student_hall_history WHERE student_id IN (" +
                    "SELECT id FROM students WHERE user_id IN ('" + TEST_STUDENT_DUE + "', '" + TEST_STUDENT_NOHALL + "', '" + TEST_STUDENT_OK + "', '" + TEST_STUDENT_SWAP + "'))");
            stmt.executeUpdate("DELETE FROM students WHERE user_id IN ('" +
                    TEST_STUDENT_DUE + "', '" + TEST_STUDENT_NOHALL + "', '" + TEST_STUDENT_OK + "', '" + TEST_STUDENT_SWAP + "')");
            stmt.executeUpdate("DELETE FROM provosts WHERE user_id = '" + TEST_PROVOST + "'");
        } catch (Exception ignored) {}
    }

    private static void test01_SchemaInitialization() throws Exception {
        roomChangeDAO.ensureSchemaAndSeed();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM room_change_requests")) {
            if (!rs.next()) {
                throw new AssertionError("room_change_requests table could not be queried.");
            }
        }
    }

    private static void test02_IneligibleWhenDuePositive() throws Exception {
        RoomChangeDAO.EligibilityStatus status = roomChangeDAO.checkStudentEligibility(TEST_STUDENT_DUE);
        if (status.isEligible()) {
            throw new AssertionError("Student with due amount should NOT be eligible for Room/Seat Change.");
        }
        if (status.getTotalDue() <= 0.0) {
            throw new AssertionError("Total due should be > 0.");
        }
        if (!status.getMessage().contains("outstanding due")) {
            throw new AssertionError("Message should mention outstanding dues.");
        }
    }

    private static void test03_IneligibleWhenNoActiveResidence() throws Exception {
        RoomChangeDAO.EligibilityStatus status = roomChangeDAO.checkStudentEligibility(TEST_STUDENT_NOHALL);
        if (status.isEligible()) {
            throw new AssertionError("Student with no active residence should NOT be eligible.");
        }
        if (status.isHasActiveResidence()) {
            throw new AssertionError("hasActiveResidence should be false.");
        }
    }

    private static void test04_EligibleWhenActiveResidenceAndZeroDue() throws Exception {
        RoomChangeDAO.EligibilityStatus status = roomChangeDAO.checkStudentEligibility(TEST_STUDENT_OK);
        if (!status.isEligible()) {
            throw new AssertionError("Student with active residence and Tk. 0 due MUST be eligible.");
        }
        if (status.getTotalDue() != 0.0) {
            throw new AssertionError("Total due should be 0.0.");
        }
    }

    private static void test05_AvailableRoomsQuery() throws Exception {
        List<Integer> rooms = roomChangeDAO.getAvailableRoomsForHall(testHallId);
        if (rooms == null || rooms.isEmpty()) {
            throw new AssertionError("Expected available rooms for test hall " + testHallId);
        }
    }

    private static void test06_AvailableSeatsQuery() throws Exception {
        List<Integer> seats = roomChangeDAO.getAvailableSeatsForRoom(testHallId, 801, 801, 2);
        if (seats.contains(2)) {
            throw new AssertionError("Available seats query should exclude student's current seat (Seat 2) when querying the same room.");
        }
    }

    private static void test07_SubmissionBlockedWhenSelectingSameRoomAndSeat() {
        try {
            roomChangeDAO.createRoomChangeRequest(TEST_STUDENT_OK, testHallId, 801, 2, 801, 2, "Try to change to same seat");
            throw new AssertionError("Expected exception when selecting same room and seat!");
        } catch (IllegalArgumentException e) {
            if (!e.getMessage().contains("same as your current residence")) {
                throw new AssertionError("Expected error message about same residence, got: " + e.getMessage());
            }
        } catch (Exception ex) {
            throw new AssertionError("Expected IllegalArgumentException, got: " + ex.getClass().getName());
        }
    }

    private static void test08_SubmissionBlockedWhenReasonEmpty() {
        try {
            roomChangeDAO.createRoomChangeRequest(TEST_STUDENT_OK, testHallId, 801, 2, 802, 2, "   ");
            throw new AssertionError("Expected exception when reason is empty!");
        } catch (IllegalArgumentException e) {
            if (!e.getMessage().contains("cannot be empty")) {
                throw new AssertionError("Expected error message about empty reason, got: " + e.getMessage());
            }
        } catch (Exception ex) {
            throw new AssertionError("Expected IllegalArgumentException, got: " + ex.getClass().getName());
        }
    }

    private static void test09_SuccessfulRequestSubmission() throws Exception {

        List<Integer> rooms = roomChangeDAO.getAvailableRoomsForHall(testHallId);
        int targetRoom = rooms.get(0);
        List<Integer> seats = roomChangeDAO.getAvailableSeatsForRoom(testHallId, targetRoom, 801, 2);
        int targetSeat = seats.get(0);

        RoomChangeRequest req = roomChangeDAO.createRoomChangeRequest(
                TEST_STUDENT_OK, testHallId, 801, 2, targetRoom, targetSeat, "Need quieter study environment."
        );

        if (req == null || req.getId() <= 0) {
            throw new AssertionError("Failed to create room change request.");
        }
        if (!"PENDING".equalsIgnoreCase(req.getStatus())) {
            throw new AssertionError("Initial status must be PENDING, got: " + req.getStatus());
        }
        if (req.getRequestedRoom() != targetRoom || req.getRequestedSeat() != targetSeat) {
            throw new AssertionError("Requested room/seat mismatch.");
        }
    }

    private static void test10_DuplicatePendingRequestBlocked() throws Exception {
        RoomChangeDAO.EligibilityStatus status = roomChangeDAO.checkStudentEligibility(TEST_STUDENT_OK);
        if (status.isEligible()) {
            throw new AssertionError("Student with an active PENDING request must NOT be eligible to submit another.");
        }
        if (!status.isHasPendingRequest()) {
            throw new AssertionError("hasPendingRequest should be true.");
        }
    }

    private static void test11_StudentEditPendingRequest() throws Exception {
        RoomChangeDAO.EligibilityStatus status = roomChangeDAO.checkStudentEligibility(TEST_STUDENT_OK);
        RoomChangeRequest pending = status.getPendingRequest();
        if (pending == null) throw new AssertionError("Pending request not found.");

        List<Integer> rooms = roomChangeDAO.getAvailableRoomsForHall(testHallId);
        int newTargetRoom = rooms.get(rooms.size() - 1);
        List<Integer> seats = roomChangeDAO.getAvailableSeatsForRoom(testHallId, newTargetRoom, 801, 2);
        int newTargetSeat = seats.get(0);

        RoomChangeRequest updated = roomChangeDAO.updatePendingRoomChangeRequest(
                pending.getId(), TEST_STUDENT_OK, newTargetRoom, newTargetSeat, "Updated reason: Medical requirement."
        );

        if (updated.getRequestedRoom() != newTargetRoom || updated.getRequestedSeat() != newTargetSeat) {
            throw new AssertionError("Failed to update requested room and seat.");
        }
        if (!"Updated reason: Medical requirement.".equals(updated.getReason())) {
            throw new AssertionError("Failed to update reason.");
        }
    }

    private static void test12_StudentCancelPendingRequest() throws Exception {
        RoomChangeDAO.EligibilityStatus status = roomChangeDAO.checkStudentEligibility(TEST_STUDENT_OK);
        RoomChangeRequest pending = status.getPendingRequest();
        if (pending == null) throw new AssertionError("Pending request not found.");

        boolean cancelled = roomChangeDAO.cancelRoomChangeRequest(pending.getId(), TEST_STUDENT_OK);
        if (!cancelled) throw new AssertionError("Failed to cancel room change request.");

        RoomChangeRequest fetched = roomChangeDAO.getRequestById(pending.getId());
        if (!"CANCELLED".equalsIgnoreCase(fetched.getStatus())) {
            throw new AssertionError("Request status should be CANCELLED, got: " + fetched.getStatus());
        }

        RoomChangeDAO.EligibilityStatus rechecked = roomChangeDAO.checkStudentEligibility(TEST_STUDENT_OK);
        if (!rechecked.isEligible()) {
            throw new AssertionError("Student should be eligible again after cancelling pending request.");
        }
    }

    private static void test13_ProvostRejectRequestWithReason() throws Exception {
        List<Integer> rooms = roomChangeDAO.getAvailableRoomsForHall(testHallId);
        int room = rooms.get(0);
        int seat = roomChangeDAO.getAvailableSeatsForRoom(testHallId, room, 801, 2).get(0);

        RoomChangeRequest req = roomChangeDAO.createRoomChangeRequest(
                TEST_STUDENT_OK, testHallId, 801, 2, room, seat, "Request to move."
        );

        roomChangeDAO.rejectRoomChangeRequest(req.getId(), TEST_PROVOST, "Room is reserved for special batch.");

        RoomChangeRequest rejected = roomChangeDAO.getRequestById(req.getId());
        if (!"REJECTED".equalsIgnoreCase(rejected.getStatus())) {
            throw new AssertionError("Status should be REJECTED, got: " + rejected.getStatus());
        }
        if (!"Room is reserved for special batch.".equals(rejected.getRejectionReason())) {
            throw new AssertionError("Rejection reason not stored properly.");
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT current_room, current_seat FROM students WHERE user_id = ?")) {
            ps.setString(1, TEST_STUDENT_OK);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    if (rs.getInt("current_room") != 801 || rs.getInt("current_seat") != 2) {
                        throw new AssertionError("Student residence was modified during rejection!");
                    }
                }
            }
        }
    }

    private static void test14_ProvostApproveRequestACID() throws Exception {

        List<Integer> rooms = roomChangeDAO.getAvailableRoomsForHall(testHallId);
        int targetRoom = -1;
        int targetSeat = -1;

        for (int r : rooms) {
            List<Integer> free = roomChangeDAO.getAvailableSeatsForRoom(testHallId, r, 802, 1);
            if (!free.isEmpty()) {
                targetRoom = r;
                targetSeat = free.get(0);
                break;
            }
        }

        if (targetRoom == -1) throw new AssertionError("No target room found for approval test.");

        RoomChangeRequest req = roomChangeDAO.createRoomChangeRequest(
                TEST_STUDENT_SWAP, testHallId, 802, 1, targetRoom, targetSeat, "Transfer for final year thesis team."
        );

        roomChangeDAO.approveRoomChangeRequest(req.getId(), TEST_PROVOST);

        RoomChangeRequest approved = roomChangeDAO.getRequestById(req.getId());
        if (!"APPROVED".equalsIgnoreCase(approved.getStatus())) {
            throw new AssertionError("Request status should be APPROVED, got: " + approved.getStatus());
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT current_room, current_seat FROM students WHERE user_id = ?")) {
            ps.setString(1, TEST_STUDENT_SWAP);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    if (rs.getInt("current_room") != targetRoom || rs.getInt("current_seat") != targetSeat) {
                        throw new AssertionError("Student residence was not updated to new room/seat!");
                    }
                }
            }
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM student_hall_history WHERE student_id = (SELECT id FROM students WHERE user_id = ?) ORDER BY id ASC")) {
            ps.setString(1, TEST_STUDENT_SWAP);
            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) throw new AssertionError("History record 1 missing.");
                if (rs.getInt("room_number") != 802 || rs.getInt("seat_number") != 1 || rs.getDate("end_date") == null) {
                    throw new AssertionError("Old history record was not closed with end_date!");
                }

                if (!rs.next()) throw new AssertionError("History record 2 (new residence) missing.");
                if (rs.getInt("room_number") != targetRoom || rs.getInt("seat_number") != targetSeat || rs.getDate("end_date") != null) {
                    throw new AssertionError("New history record not active or incorrect room/seat!");
                }
            }
        }
    }

    private static void test15_ConcurrencyProtectionOnOccupiedSeat() throws Exception {

        RoomChangeRequest req = roomChangeDAO.createRoomChangeRequest(
                TEST_STUDENT_OK, testHallId, 801, 2, 803, 1, "Race condition test."
        );

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("UPDATE students SET current_room = 803, current_seat = 1 WHERE user_id = '" + TEST_STUDENT_DUE + "'");
        }

        try {
            roomChangeDAO.approveRoomChangeRequest(req.getId(), TEST_PROVOST);
            throw new AssertionError("Approval should have failed because seat is now occupied!");
        } catch (IllegalStateException ex) {
            if (!ex.getMessage().contains("no longer available")) {
                throw new AssertionError("Expected 'no longer available' error, got: " + ex.getMessage());
            }
        } finally {

            try (Connection conn = DBConnection.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE students SET current_room = 801, current_seat = 1 WHERE user_id = '" + TEST_STUDENT_DUE + "'");
            }
        }
    }

    private static void test16_SecurityProtectionOnNewDue() throws Exception {

        RoomChangeDAO.EligibilityStatus status = roomChangeDAO.checkStudentEligibility(TEST_STUDENT_OK);
        RoomChangeRequest pending = status.getPendingRequest();
        if (pending == null) throw new AssertionError("Pending request not found.");

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("INSERT INTO bills (student_id, hall_id, room_number, seat_number, billing_period, hall_charge, electricity_charge, water_charge, maintenance_charge, other_charge, total_amount, paid_amount, due_amount, status) " +
                    "VALUES ('" + TEST_STUDENT_OK + "', " + testHallId + ", 801, 2, 'September 2026', 500, 200, 100, 100, 100, 1000, 0, 1000.00, 'DUE')");
        }

        try {
            roomChangeDAO.approveRoomChangeRequest(pending.getId(), TEST_PROVOST);
            throw new AssertionError("Approval should have failed because student now has outstanding due!");
        } catch (IllegalStateException ex) {
            if (!ex.getMessage().contains("outstanding due")) {
                throw new AssertionError("Expected 'outstanding due' error, got: " + ex.getMessage());
            }
        } finally {

            try (Connection conn = DBConnection.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM bills WHERE student_id = '" + TEST_STUDENT_OK + "' AND billing_period = 'September 2026'");
            }
        }
    }

    private static void test17_DuplicateApprovalBlocked() throws Exception {

        RoomChangeDAO.EligibilityStatus status = roomChangeDAO.checkStudentEligibility(TEST_STUDENT_OK);
        if (status.getPendingRequest() != null) {
            roomChangeDAO.cancelRoomChangeRequest(status.getPendingRequest().getId(), TEST_STUDENT_OK);
        }

        List<Integer> rooms = roomChangeDAO.getAvailableRoomsForHall(testHallId);
        int room = rooms.get(0);
        int seat = roomChangeDAO.getAvailableSeatsForRoom(testHallId, room, 801, 2).get(0);

        RoomChangeRequest req = roomChangeDAO.createRoomChangeRequest(
                TEST_STUDENT_OK, testHallId, 801, 2, room, seat, "Duplicate approval test."
        );
        roomChangeDAO.approveRoomChangeRequest(req.getId(), TEST_PROVOST);

        try {
            roomChangeDAO.approveRoomChangeRequest(req.getId(), TEST_PROVOST);
            throw new AssertionError("Second approval should be rejected!");
        } catch (IllegalStateException ex) {
            if (!ex.getMessage().contains("already been processed")) {
                throw new AssertionError("Expected 'already been processed' error, got: " + ex.getMessage());
            }
        }
    }

    private static void test18_NotificationBadgeAndMarkReviewed() throws Exception {
        int unreviewedCount = roomChangeDAO.getUnreviewedProcessedCountForStudent(TEST_STUDENT_OK);
        if (unreviewedCount <= 0) {
            throw new AssertionError("Expected unreviewed processed count > 0 for student.");
        }

        List<RoomChangeRequest> studentRequests = roomChangeDAO.getRequestsForStudent(TEST_STUDENT_OK);
        RoomChangeRequest approvedReq = studentRequests.get(0);

        roomChangeDAO.markRequestAsReviewedByStudent(approvedReq.getId(), TEST_STUDENT_OK);

        int countAfterReview = roomChangeDAO.getUnreviewedProcessedCountForStudent(TEST_STUDENT_OK);
        if (countAfterReview != unreviewedCount - 1) {
            throw new AssertionError("Count after review should decrease by 1, expected " + (unreviewedCount - 1) + " but got " + countAfterReview);
        }
    }
}
