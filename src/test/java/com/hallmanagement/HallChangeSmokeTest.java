package com.hallmanagement;

import com.hallmanagement.dao.BillingDAO;
import com.hallmanagement.dao.HallChangeDAO;
import com.hallmanagement.dao.HallDAO;
import com.hallmanagement.dao.UserDAO;
import com.hallmanagement.database.DBConnection;
import com.hallmanagement.model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

public class HallChangeSmokeTest {

    public static void main(String[] args) {
        System.out.println("=== Starting Hall Change / Transfer Smoke Test ===");

        try (Connection conn = DBConnection.getConnection()) {
            HallChangeDAO changeDAO = new HallChangeDAO();
            HallDAO hallDAO = new HallDAO();
            BillingDAO billingDAO = new BillingDAO();
            UserDAO userDAO = new UserDAO();

            changeDAO.ensureSchemaAndSeed();
            hallDAO.ensureSchemaAndSeedData();
            billingDAO.ensureBillingSchemaAndSeed();

            String provostId = "provost001";
            String studentWithDue = "test_student_due";
            String studentEligible = "test_student_free";
            String studentOther = "test_student_other";

            try (Statement st = conn.createStatement()) {
                st.executeUpdate("DELETE FROM bills WHERE student_id IN ('" + studentWithDue + "', '" + studentEligible + "', '" + studentOther + "')");
                st.executeUpdate("DELETE FROM hall_change_requests WHERE student_id IN ('" + studentWithDue + "', '" + studentEligible + "', '" + studentOther + "')");
                st.executeUpdate("DELETE FROM student_hall_history WHERE student_id IN (SELECT id FROM students WHERE user_id IN ('" + studentWithDue + "', '" + studentEligible + "', '" + studentOther + "'))");
                st.executeUpdate("DELETE FROM students WHERE user_id IN ('" + studentWithDue + "', '" + studentEligible + "', '" + studentOther + "')");
            }

            userDAO.registerStudent(studentWithDue, "pass123", "Due Student", "01711110001", "CSE", "1st Year", 1, 102, 1, null, null, null);
            String insertBill1 = "INSERT INTO bills (student_id, hall_id, room_number, seat_number, billing_period, total_amount, paid_amount, due_amount, status) VALUES (?, 1, 102, 1, 'Sept 2026', 500.0, 0.0, 500.0, 'DUE')";
            try (PreparedStatement ps = conn.prepareStatement(insertBill1)) {
                ps.setString(1, studentWithDue);
                ps.executeUpdate();
            }

            userDAO.registerStudent(studentEligible, "pass123", "Eligible Student", "01711110002", "EEE", "2nd Year", 1, 102, 2, null, null, null);
            String insertBill2 = "INSERT INTO bills (student_id, hall_id, room_number, seat_number, billing_period, total_amount, paid_amount, due_amount, status) VALUES (?, 1, 102, 2, 'Sept 2026', 500.0, 500.0, 0.0, 'PAID')";
            try (PreparedStatement ps = conn.prepareStatement(insertBill2)) {
                ps.setString(1, studentEligible);
                ps.executeUpdate();
            }

            userDAO.registerStudent(studentOther, "pass123", "Other Student", "01711110003", "ME", "3rd Year", 2, 101, 1, null, null, null);

            System.out.println("\n[Test 1] Checking eligibility for student with outstanding due...");
            HallChangeDAO.EligibilityStatus status1 = changeDAO.checkStudentEligibility(studentWithDue);
            System.out.println("   Due: Tk. " + status1.getTotalDue() + " | Eligible: " + status1.isEligible() + " | Message: " + status1.getMessage());
            if (status1.isEligible() || status1.getTotalDue() <= 0) {
                throw new AssertionError("Test 1 Failed: Student with due was marked eligible!");
            }
            try {
                changeDAO.createHallChangeRequest(studentWithDue, 2, 101, 2);
                throw new AssertionError("Test 1 Failed: createHallChangeRequest did not throw exception for student with due!");
            } catch (IllegalStateException e) {
                System.out.println("   PASSED: Submission blocked due to outstanding balance (" + e.getMessage() + ")");
            }

            System.out.println("\n[Test 2] Checking eligibility for student with due == 0...");
            HallChangeDAO.EligibilityStatus status2 = changeDAO.checkStudentEligibility(studentEligible);
            System.out.println("   Due: Tk. " + status2.getTotalDue() + " | Eligible: " + status2.isEligible() + " | Message: " + status2.getMessage());
            if (!status2.isEligible()) {
                throw new AssertionError("Test 2 Failed: Student with due 0 was not marked eligible!");
            }
            System.out.println("   PASSED: Student with 0 due is eligible.");

            System.out.println("\n[Test 3] Validating cascading room & seat availability...");
            List<RoomInfo> rooms = hallDAO.getRoomsByHallId(2);
            RoomInfo room101 = rooms.stream().filter(r -> r.getRoomNumber() == 101).findFirst().orElseThrow();
            System.out.println("   Hall 2, Room 101: Total Seats = " + room101.getTotalSeats() + ", Occupied = " + room101.getOccupiedSeats() + ", Available = " + room101.getAvailableSeats());
            List<SeatInfo> seats = hallDAO.getSeatsByRoomId(room101.getRoomId());
            for (SeatInfo s : seats) {
                System.out.println("     " + s.getSeatNumberDisplay() + ": " + (s.isOccupied() ? "OCCUPIED (" + s.getStudentName() + ")" : "AVAILABLE"));
            }
            SeatInfo seat1 = seats.stream().filter(s -> s.getSeatNumber() == 1).findFirst().orElseThrow();
            SeatInfo seat2 = seats.stream().filter(s -> s.getSeatNumber() == 2).findFirst().orElseThrow();
            if (!seat1.isOccupied() || seat2.isOccupied()) {
                throw new AssertionError("Test 3 Failed: Seat 1 should be occupied by other student, Seat 2 should be available!");
            }
            System.out.println("   PASSED: Available seat filtering validated.");

            HallChangeRequest createdReq = changeDAO.createHallChangeRequest(studentEligible, 2, 101, 2);
            System.out.println("   Created Request: " + createdReq.getRequestCode() + " | Status: " + createdReq.getStatus());
            if (!"PENDING".equals(createdReq.getStatus()) || createdReq.getRequestedSeat() != 2) {
                throw new AssertionError("Test 3 Failed: Hall change request creation failed!");
            }

            HallChangeDAO.EligibilityStatus duplicateCheck = changeDAO.checkStudentEligibility(studentEligible);
            if (duplicateCheck.isEligible() || !duplicateCheck.isHasPendingRequest()) {
                throw new AssertionError("Test 3 Failed: Duplicate request guard failed!");
            }
            System.out.println("   PASSED: Duplicate request blocked while pending.");

            System.out.println("\n[Test 4] Testing concurrency conflict protection...");

            String stealSeatSql = "UPDATE students SET current_hall_id = 2, current_room = 101, current_seat = 2 WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(stealSeatSql)) {
                ps.setString(1, studentOther);
                ps.executeUpdate();
            }

            try {
                changeDAO.approveHallChangeRequest(createdReq.getId(), provostId);
                throw new AssertionError("Test 4 Failed: Approval should have failed because seat became occupied!");
            } catch (IllegalStateException e) {
                System.out.println("   PASSED: Concurrency check prevented double allocation (" + e.getMessage() + ")");
            }

            String freeSeatSql = "UPDATE students SET current_hall_id = 2, current_room = 101, current_seat = 1 WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(freeSeatSql)) {
                ps.setString(1, studentOther);
                ps.executeUpdate();
            }

            System.out.println("\n[Test 5] Testing successful hall change approval...");
            changeDAO.approveHallChangeRequest(createdReq.getId(), provostId);
            System.out.println("   Provost approved request: " + createdReq.getRequestCode());

            String checkStudentSql = "SELECT current_hall_id, current_room, current_seat FROM students WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkStudentSql)) {
                ps.setString(1, studentEligible);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int h = rs.getInt("current_hall_id");
                        int r = rs.getInt("current_room");
                        int s = rs.getInt("current_seat");
                        System.out.println("   Updated Student Residence: Hall " + h + ", Room " + r + ", Seat " + s);
                        if (h != 2 || r != 101 || s != 2) {
                            throw new AssertionError("Test 5 Failed: Student residence not updated to new hall/room/seat!");
                        }
                    }
                }
            }

            String checkHistSql = "SELECT * FROM student_hall_history WHERE student_id = (SELECT id FROM students WHERE user_id = ?) ORDER BY id ASC";
            try (PreparedStatement ps = conn.prepareStatement(checkHistSql)) {
                ps.setString(1, studentEligible);
                try (var rs = ps.executeQuery()) {
                    int count = 0;
                    while (rs.next()) {
                        count++;
                        System.out.println("   History #" + count + ": Hall " + rs.getInt("hall_id") + ", Room " + rs.getInt("room_number") + ", Seat " + rs.getInt("seat_number") + " | From: " + rs.getDate("start_date") + " To: " + rs.getDate("end_date"));
                    }
                    if (count < 2) {
                        throw new AssertionError("Test 5 Failed: History records count should be at least 2!");
                    }
                }
            }
            System.out.println("   PASSED: Student active residence and history transaction succeeded.");

            System.out.println("\n[Test 6] Testing rejection flow...");

            HallChangeRequest req2 = changeDAO.createHallChangeRequest(studentEligible, 1, 102, 4);
            changeDAO.rejectHallChangeRequest(req2.getId(), provostId, "Insufficient documentation submitted.");

            HallChangeRequest rejectedReq = changeDAO.getRequestById(req2.getId());
            System.out.println("   Request " + rejectedReq.getRequestCode() + " Status: " + rejectedReq.getStatus() + " | Reason: " + rejectedReq.getRejectionReason());
            if (!"REJECTED".equals(rejectedReq.getStatus()) || rejectedReq.getRejectionReason() == null) {
                throw new AssertionError("Test 6 Failed: Request rejection was not recorded!");
            }
            System.out.println("   PASSED: Rejection recorded with reason.");

            System.out.println("\n[Test 7] Verifying occupied seat basic information display...");
            List<SeatInfo> updatedSeats = hallDAO.getSeatsByRoomId(room101.getRoomId());
            SeatInfo occupiedSeat2 = updatedSeats.stream().filter(s -> s.getSeatNumber() == 2).findFirst().orElseThrow();
            System.out.println("   Occupied Seat Basic Info:\n" + occupiedSeat2.getOccupantBasicInfo());
            if (!occupiedSeat2.isOccupied() || !studentEligible.equals(occupiedSeat2.getStudentUserId())) {
                throw new AssertionError("Test 7 Failed: Seat 2 not occupied by eligible student!");
            }
            System.out.println("   PASSED: Occupied seat shows only basic occupant details.");

            try (Statement st = conn.createStatement()) {
                st.executeUpdate("DELETE FROM bills WHERE student_id IN ('" + studentWithDue + "', '" + studentEligible + "', '" + studentOther + "')");
                st.executeUpdate("DELETE FROM hall_change_requests WHERE student_id IN ('" + studentWithDue + "', '" + studentEligible + "', '" + studentOther + "')");
                st.executeUpdate("DELETE FROM student_hall_history WHERE student_id IN (SELECT id FROM students WHERE user_id IN ('" + studentWithDue + "', '" + studentEligible + "', '" + studentOther + "'))");
                st.executeUpdate("DELETE FROM students WHERE user_id IN ('" + studentWithDue + "', '" + studentEligible + "', '" + studentOther + "')");
            }

            System.out.println("\n=== All Hall Change / Transfer Smoke Tests Passed Successfully! ===");

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
