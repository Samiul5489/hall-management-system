package com.hallmanagement;

import com.hallmanagement.dao.HallChangeDAO;
import com.hallmanagement.dao.ProvostDAO;
import com.hallmanagement.dao.StudentDAO;
import com.hallmanagement.database.DBConnection;
import com.hallmanagement.model.HallChangeRequest;
import com.hallmanagement.model.ProvostInfo;
import com.hallmanagement.model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class ProvostProfileAndRequestEditSmokeTest {

    public static void main(String[] args) {
        try {
            System.out.println("=== Starting Provost Profile & Hall Change Request Edit/Cancel Smoke Test ===");

            cleanupTestData();
            setupTestData();

            testProvostProfileOperations();
            testStudentRequestEditAndCancelFlow();

            System.out.println("\n=== All Provost Profile & Request Edit/Cancel Smoke Tests Passed Successfully! ===");

        } catch (Throwable t) {
            System.err.println("\n❌ TEST FAILED: " + t.getMessage());
            t.printStackTrace();
            System.exit(1);
        }
    }

    private static void cleanupTestData() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM hall_change_requests WHERE student_id IN ('test_stu_edit_cancel')");
            stmt.executeUpdate("DELETE FROM student_hall_history WHERE student_id IN (SELECT id FROM students WHERE user_id = 'test_stu_edit_cancel')");
            stmt.executeUpdate("DELETE FROM bills WHERE student_id IN ('test_stu_edit_cancel')");
            stmt.executeUpdate("DELETE FROM students WHERE user_id = 'test_stu_edit_cancel'");
            stmt.executeUpdate("DELETE FROM provosts WHERE user_id = 'test_provost_edit'");
        } catch (Exception ignored) {}
    }

    private static void setupTestData() throws Exception {
        new ProvostDAO().ensureSchema();
        try (Connection conn = DBConnection.getConnection()) {

            String sqlProvost = "INSERT INTO provosts (user_id, password, name, phone, hall_id, age, email, designation, office_room, status) VALUES (?, 'pass123', ?, ?, 1, 48, 'rahman@ruet.ac.bd', 'Professor & Provost', 'Room 101, Main Office', 'ACTIVE')";
            try (PreparedStatement ps = conn.prepareStatement(sqlProvost)) {
                ps.setString(1, "test_provost_edit");
                ps.setString(2, "Prof. Dr. M. Rahman");
                ps.setString(3, "01711223344");
                ps.executeUpdate();
            }

            String sqlStu = "INSERT INTO students (user_id, password, name, phone, department, current_hall_id, current_room, current_seat, status) VALUES (?, 'pass123', ?, ?, 'CSE', 1, 104, 4, 'ACTIVE')";
            try (PreparedStatement ps = conn.prepareStatement(sqlStu)) {
                ps.setString(1, "test_stu_edit_cancel");
                ps.setString(2, "Tanvir Hasan");
                ps.setString(3, "01811223344");
                ps.executeUpdate();
            }
        }
    }

    private static void testProvostProfileOperations() throws Exception {
        System.out.println("\n[Test 1] Fetching and Updating Provost Profile Details...");
        ProvostDAO provostDAO = new ProvostDAO();

        ProvostInfo initial = provostDAO.getProvostProfile("test_provost_edit");
        if (initial == null) throw new AssertionError("Provost profile not found for test_provost_edit");
        System.out.println("   ✓ Initial Profile loaded: " + initial.getName() + " | Age: " + initial.getAge() + " | " + initial.getEmail());

        provostDAO.updateProvostProfile(
                "test_provost_edit",
                "Prof. Dr. Md. M. Rahman",
                "01799887766",
                50,
                "rahman.head@ruet.ac.bd",
                "Professor & Head Provost",
                "Office Room 204",
                2
        );

        ProvostInfo updated = provostDAO.getProvostProfile("test_provost_edit");
        if (updated == null) throw new AssertionError("Updated provost profile not found");
        if (!"Prof. Dr. Md. M. Rahman".equals(updated.getName())) throw new AssertionError("Name did not update");
        if (!"01799887766".equals(updated.getPhone())) throw new AssertionError("Phone did not update");
        if (updated.getAge() != 50) throw new AssertionError("Age did not update");
        if (updated.getHallId() != 2) throw new AssertionError("Assigned Hall ID did not update to 2");

        System.out.println("   ✓ Updated Profile: " + updated.getName() + " | Age: " + updated.getAge() + " | Phone: " + updated.getPhone() + " | Hall: " + updated.getHallName());
        System.out.println("   PASSED: Provost profile fetch and update (including assigned hall) verified.");
    }

    private static void testStudentRequestEditAndCancelFlow() throws Exception {
        System.out.println("\n[Test 2] Testing Student Hall Change Request Submit, Edit, and Cancel Flow...");
        HallChangeDAO hallChangeDAO = new HallChangeDAO();

        HallChangeDAO.EligibilityStatus status1 = hallChangeDAO.checkStudentEligibility("test_stu_edit_cancel");
        if (!status1.isEligible()) throw new AssertionError("Student should be initially eligible: " + status1.getMessage());
        System.out.println("   ✓ Student is initially eligible: " + status1.getMessage());

        HallChangeRequest req1 = hallChangeDAO.createHallChangeRequest("test_stu_edit_cancel", 1, 104, 1);
        System.out.println("   ✓ Request submitted: " + req1.getRequestCode() + " for " + req1.getRequestedResidenceDisplay());

        HallChangeDAO.EligibilityStatus statusPending = hallChangeDAO.checkStudentEligibility("test_stu_edit_cancel");
        if (statusPending.isEligible() || !statusPending.isHasPendingRequest()) {
            throw new AssertionError("Student should have active pending request");
        }
        System.out.println("   ✓ Eligibility blocked due to pending request: " + statusPending.getMessage());

        HallChangeRequest editedReq = hallChangeDAO.updatePendingHallChangeRequest(
                req1.getId(),
                "test_stu_edit_cancel",
                1,
                104,
                2
        );
        if (editedReq.getRequestedSeat() != 2) throw new AssertionError("Edited seat should be 2, but was: " + editedReq.getRequestedSeat());
        System.out.println("   ✓ Request successfully edited before approval: " + editedReq.getRequestedResidenceDisplay());

        hallChangeDAO.cancelHallChangeRequest(req1.getId(), "test_stu_edit_cancel");
        System.out.println("   ✓ Request " + req1.getRequestCode() + " successfully cancelled by student.");

        HallChangeDAO.EligibilityStatus statusAfterCancel = hallChangeDAO.checkStudentEligibility("test_stu_edit_cancel");
        if (!statusAfterCancel.isEligible()) throw new AssertionError("Student should be eligible again after cancellation");
        System.out.println("   ✓ Student is eligible again after cancellation: " + statusAfterCancel.getMessage());

        HallChangeRequest newReq = hallChangeDAO.createHallChangeRequest("test_stu_edit_cancel", 1, 104, 3);
        System.out.println("   ✓ New Request submitted: " + newReq.getRequestCode() + " for " + newReq.getRequestedResidenceDisplay());

        hallChangeDAO.approveHallChangeRequest(newReq.getId(), "test_provost_edit");
        System.out.println("   ✓ Provost approved new request: " + newReq.getRequestCode());

        StudentDAO studentDAO = new StudentDAO();
        Student student = studentDAO.getStudentByUserId("test_stu_edit_cancel");
        if (student.getCurrentRoom() != 104 || student.getCurrentSeat() != 3) {
            throw new AssertionError("Student residence not updated correctly: Room " + student.getCurrentRoom() + ", Seat " + student.getCurrentSeat());
        }
        System.out.println("   ✓ Verified Student current residence updated to: Room " + student.getCurrentRoom() + ", Seat " + student.getCurrentSeat());

        System.out.println("   PASSED: Full request submit, edit, cancel, re-submit, and approve cycle verified!");
    }
}
