package com.hallmanagement;

import com.hallmanagement.dao.*;
import com.hallmanagement.database.DBConnection;
import com.hallmanagement.model.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

public class HallLeaveSmokeTest {

    private static final HallLeaveDAO hallLeaveDAO = new HallLeaveDAO();
    private static final StudentDAO studentDAO = new StudentDAO();
    private static final HallDAO hallDAO = new HallDAO();
    private static final BillingDAO billingDAO = new BillingDAO();

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("   PROMPT 8: HALL LEAVE & CLEARANCE MODULE SMOKE TEST SUITE    ");
        System.out.println("===============================================================");

        int passed = 0;
        int total = 0;

        try {

            System.out.print("[TEST 1] Schema & Table creation (hall_leave_requests)... ");
            total++;
            hallLeaveDAO.ensureSchemaAndSeed();
            System.out.println("PASSED");
            passed++;

            setupTestData();

            System.out.print("[TEST 2] Ineligibility check: Student with outstanding Due > 0... ");
            total++;
            HallLeaveDAO.EligibilityStatus eligDue = hallLeaveDAO.checkStudentEligibility("student_test_leave_due");
            if (!eligDue.isEligible() && eligDue.getTotalDue() > 0) {
                System.out.println("PASSED (Blocked: Due = " + eligDue.getTotalDue() + ")");
                passed++;
            } else {
                System.err.println("FAILED: Expected ineligible with due > 0, got eligible=" + eligDue.isEligible());
            }

            System.out.print("[TEST 3] Ineligibility check: Student with no active hall assignment... ");
            total++;
            HallLeaveDAO.EligibilityStatus eligNoHall = hallLeaveDAO.checkStudentEligibility("student_test_leave_nohall");
            if (!eligNoHall.isEligible() && !eligNoHall.isHasActiveHall()) {
                System.out.println("PASSED (Blocked: No active hall assignment)");
                passed++;
            } else {
                System.err.println("FAILED: Expected ineligible without active hall, got: " + eligNoHall.getMessage());
            }

            System.out.print("[TEST 4] Eligibility check: Student with Due == 0 and active hall... ");
            total++;
            HallLeaveDAO.EligibilityStatus eligClean = hallLeaveDAO.checkStudentEligibility("student_test_leave_clean");
            if (eligClean.isEligible() && eligClean.getTotalDue() == 0.0 && eligClean.isHasActiveHall()) {
                System.out.println("PASSED (Eligible to apply)");
                passed++;
            } else {
                System.err.println("FAILED: Expected eligible, got: " + eligClean.getMessage());
            }

            System.out.print("[TEST 5] Submitting Hall Leave Request... ");
            total++;
            HallLeaveRequest createdReq = hallLeaveDAO.createHallLeaveRequest("student_test_leave_clean", 1, 901, 1, "Graduating RUET in session 2025-2026. Requesting final hall clearance.");
            if (createdReq != null && createdReq.isPending() && "PENDING".equals(createdReq.getStatus())) {
                int reqId1 = createdReq.getId();
                System.out.println("PASSED (Created Request ID=" + reqId1 + ", Code=" + createdReq.getRequestCode() + ")");
                passed++;

                System.out.print("[TEST 6] Duplicate pending leave request prevention... ");
                total++;
                try {
                    hallLeaveDAO.createHallLeaveRequest("student_test_leave_clean", 1, 901, 1, "Another attempt while first is pending");
                    System.err.println("FAILED: Duplicate request was allowed");
                } catch (IllegalStateException e) {
                    System.out.println("PASSED (Correctly rejected: " + e.getMessage() + ")");
                    passed++;
                }

                System.out.print("[TEST 7] Student cancelling pending request... ");
                total++;
                boolean cancelled = hallLeaveDAO.cancelHallLeaveRequest(reqId1, "student_test_leave_clean");
                HallLeaveRequest reqAfterCancel = hallLeaveDAO.getRequestById(reqId1);
                if (cancelled && reqAfterCancel != null && reqAfterCancel.isCancelled()) {
                    System.out.println("PASSED (Status is now CANCELLED)");
                    passed++;
                } else {
                    System.err.println("FAILED: Cancellation failed");
                }
            } else {
                System.err.println("FAILED: Could not create leave request");
            }

            System.out.print("[TEST 8] Provost declining leave request with reason... ");
            total++;
            HallLeaveRequest req2 = hallLeaveDAO.createHallLeaveRequest("student_test_leave_clean", 1, 901, 1, "Moving to off-campus residence.");
            int reqId2 = req2.getId();
            hallLeaveDAO.declineHallLeaveRequest(reqId2, "provost_test_leave", "Please return your room key and library books to the hall office before clearance.");
            HallLeaveRequest reqAfterDecline = hallLeaveDAO.getRequestById(reqId2);
            Student studentAfterDecline = studentDAO.getStudentByUserId("student_test_leave_clean");

            if (reqAfterDecline != null && reqAfterDecline.isDeclined()
                    && reqAfterDecline.getDeclineReason().contains("library books")
                    && studentAfterDecline.getCurrentHall() != null) {
                System.out.println("PASSED (Status is DECLINED, reason recorded, student residence unchanged)");
                passed++;
            } else {
                System.err.println("FAILED: Decline verification failed");
            }

            System.out.print("[TEST 9] Provost ACID Approval Transaction execution... ");
            total++;
            HallLeaveRequest req3 = hallLeaveDAO.createHallLeaveRequest("student_test_leave_clean", 1, 901, 1, "Final semester completed. Official clearance.");
            int reqId3 = req3.getId();
            hallLeaveDAO.approveHallLeaveRequest(reqId3, "provost_test_leave");
            HallLeaveRequest reqAfterApprove = hallLeaveDAO.getRequestById(reqId3);
            Student studentAfterApprove = studentDAO.getStudentByUserId("student_test_leave_clean");

            boolean residenceCleared = studentAfterApprove.getCurrentHall() == null
                    && studentAfterApprove.getCurrentRoom() == null
                    && studentAfterApprove.getCurrentSeat() == null;

            boolean seatAvailable = isSeatAvailable(1, 901, 1);

            boolean historyClosed = isHistoryClosed(studentAfterApprove.getDbId(), 1);

            if (reqAfterApprove != null && reqAfterApprove.isApproved() && residenceCleared && seatAvailable && historyClosed) {
                System.out.println("PASSED (ACID transaction: Residence cleared to NONE, History closed, Seat released)");
                passed++;
            } else {
                System.err.println("FAILED: Approval transaction incomplete. Cleared=" + residenceCleared + ", SeatAvail=" + seatAvailable + ", HistClosed=" + historyClosed);
            }

            System.out.print("[TEST 10] ACID Security: Approval blocked if Due > 0 at approval time... ");
            total++;

            HallLeaveRequest req4 = hallLeaveDAO.createHallLeaveRequest("student_test_leave_2", 1, 902, 1, "Moving out.");
            int reqId4 = req4.getId();

            injectDue("student_test_leave_2", new BigDecimal("1500.00"));
            try {
                hallLeaveDAO.approveHallLeaveRequest(reqId4, "provost_test_leave");
                System.err.println("FAILED: Approval should have thrown IllegalStateException for outstanding due");
            } catch (IllegalStateException e) {
                System.out.println("PASSED (Correctly rejected approval due to: " + e.getMessage() + ")");
                passed++;
            }

            System.out.print("[TEST 11] Duplicate approval rejection on already approved request... ");
            total++;
            try {
                hallLeaveDAO.approveHallLeaveRequest(reqId3, "provost_test_leave");
                System.err.println("FAILED: Duplicate approval was allowed");
            } catch (IllegalStateException e) {
                System.out.println("PASSED (Correctly rejected duplicate approval: " + e.getMessage() + ")");
                passed++;
            }

            System.out.print("[TEST 12] Notification badge counts & mark-as-reviewed lifecycle... ");
            total++;
            int provostPending = hallLeaveDAO.getPendingCountForProvost("provost_test_leave");
            int studentUnreviewed = hallLeaveDAO.getUnreviewedProcessedCountForStudent("student_test_leave_clean");

            hallLeaveDAO.markLeaveRequestAsReviewedByStudent(reqId3, "student_test_leave_clean");
            hallLeaveDAO.markLeaveRequestAsReviewedByStudent(reqId2, "student_test_leave_clean");
            int studentUnreviewedAfter = hallLeaveDAO.getUnreviewedProcessedCountForStudent("student_test_leave_clean");

            if (provostPending >= 1 && studentUnreviewed >= 1 && studentUnreviewedAfter == 0) {
                System.out.println("PASSED (Provost pending=" + provostPending + ", Student unreviewed before=" + studentUnreviewed + ", after=" + studentUnreviewedAfter + ")");
                passed++;
            } else {
                System.err.println("FAILED: Badge verification mismatch: Prov=" + provostPending + ", StudBefore=" + studentUnreviewed + ", StudAfter=" + studentUnreviewedAfter);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("===============================================================");
        System.out.println("   RESULTS: " + passed + " / " + total + " TESTS PASSED (" + (passed == total ? "100% SUCCESS" : "FAILURES DETECTED") + ")");
        System.out.println("===============================================================");
    }

    private static void setupTestData() throws Exception {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("INSERT IGNORE INTO halls (id, hall_name, hall_type) " +
                    "VALUES (1, 'Shahid Shahidul Islam Hall', 'MALE')");

            stmt.executeUpdate("INSERT INTO provosts (user_id, name, password, phone, age, email, designation, hall_id) " +
                    "VALUES ('provost_test_leave', 'Prof. Test Provost', 'pass123', '01711000000', 48, 'provost_leave@ruet.ac.bd', 'Provost', 1) " +
                    "ON DUPLICATE KEY UPDATE name='Prof. Test Provost', hall_id=1");

            stmt.executeUpdate("DELETE FROM hall_leave_requests WHERE student_id IN ('student_test_leave_clean', 'student_test_leave_due', 'student_test_leave_nohall', 'student_test_leave_2')");

            stmt.executeUpdate("DELETE FROM bills WHERE student_id IN ('student_test_leave_clean', 'student_test_leave_due', 'student_test_leave_nohall', 'student_test_leave_2')");

            stmt.executeUpdate("DELETE FROM student_hall_history WHERE student_id IN (SELECT id FROM students WHERE user_id IN ('student_test_leave_clean', 'student_test_leave_due', 'student_test_leave_nohall', 'student_test_leave_2'))");
            stmt.executeUpdate("DELETE FROM students WHERE user_id IN ('student_test_leave_clean', 'student_test_leave_due', 'student_test_leave_nohall', 'student_test_leave_2')");

            stmt.executeUpdate("INSERT INTO students (user_id, name, password, department, phone, year, current_hall_id, current_room, current_seat) " +
                    "VALUES ('student_test_leave_clean', 'Clean Student', 'pass123', 'CSE', '01711111111', '4th Year', 1, 901, 1)");

            stmt.executeUpdate("INSERT INTO students (user_id, name, password, department, phone, year, current_hall_id, current_room, current_seat) " +
                    "VALUES ('student_test_leave_due', 'Due Student', 'pass123', 'EEE', '01722222222', '4th Year', 1, 901, 2)");

            stmt.executeUpdate("INSERT INTO students (user_id, name, password, department, phone, year, current_hall_id, current_room, current_seat) " +
                    "VALUES ('student_test_leave_nohall', 'No Hall Student', 'pass123', 'ME', '01733333333', '1st Year', NULL, NULL, NULL)");

            stmt.executeUpdate("INSERT INTO students (user_id, name, password, department, phone, year, current_hall_id, current_room, current_seat) " +
                    "VALUES ('student_test_leave_2', 'Student Two', 'pass123', 'CE', '01744444444', '3rd Year', 1, 902, 1)");

            stmt.executeUpdate("INSERT INTO bills (student_id, hall_id, room_number, seat_number, billing_period, hall_charge, electricity_charge, water_charge, maintenance_charge, other_charge, total_amount, paid_amount, due_amount, status) " +
                    "VALUES ('student_test_leave_clean', 1, 901, 1, 'August 2026', 500, 200, 100, 100, 100, 1000, 1000, 0.00, 'PAID')");

            stmt.executeUpdate("INSERT INTO bills (student_id, hall_id, room_number, seat_number, billing_period, hall_charge, electricity_charge, water_charge, maintenance_charge, other_charge, total_amount, paid_amount, due_amount, status) " +
                    "VALUES ('student_test_leave_due', 1, 901, 2, 'August 2026', 500, 200, 100, 100, 100, 1000, 200, 800.00, 'DUE')");

            stmt.executeUpdate("INSERT INTO bills (student_id, hall_id, room_number, seat_number, billing_period, hall_charge, electricity_charge, water_charge, maintenance_charge, other_charge, total_amount, paid_amount, due_amount, status) " +
                    "VALUES ('student_test_leave_2', 1, 902, 1, 'August 2026', 500, 200, 100, 100, 100, 1000, 1000, 0.00, 'PAID')");

            Student st = studentDAO.getStudentByUserId("student_test_leave_clean");
            stmt.executeUpdate("INSERT INTO student_hall_history (student_id, hall_id, room_number, seat_number, start_date, end_date) " +
                    "VALUES (" + st.getDbId() + ", 1, 901, 1, '2023-01-01', NULL)");
        }
    }

    private static void injectDue(String studentId, BigDecimal due) throws Exception {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE bills SET due_amount = ?, status='DUE' WHERE student_id = ?")) {
            ps.setBigDecimal(1, due);
            ps.setString(2, studentId);
            ps.executeUpdate();
        }
    }

    private static boolean isSeatAvailable(int hallId, int room, int seat) throws Exception {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) FROM students WHERE current_hall_id = ? AND current_room = ? AND current_seat = ?")) {
            ps.setInt(1, hallId);
            ps.setInt(2, room);
            ps.setInt(3, seat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        }
        return false;
    }

    private static boolean isHistoryClosed(int studentDbId, int hallId) throws Exception {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT end_date FROM student_hall_history WHERE student_id = ? AND hall_id = ? ORDER BY id DESC LIMIT 1")) {
            ps.setInt(1, studentDbId);
            ps.setInt(2, hallId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDate("end_date") != null;
                }
            }
        }
        return false;
    }
}
