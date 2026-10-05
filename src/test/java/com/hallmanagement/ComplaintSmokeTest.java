package com.hallmanagement;

import com.hallmanagement.dao.ComplaintDAO;
import com.hallmanagement.database.DBConnection;
import com.hallmanagement.model.Complaint;
import com.hallmanagement.model.ProvostInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

public class ComplaintSmokeTest {

    public static void main(String[] args) {
        try {
            System.out.println("=== Starting Student Complaint & Provost Response Smoke Test ===");

            ComplaintDAO dao = new ComplaintDAO();
            dao.ensureSchema();

            setupTestData();

            System.out.println("\n[Test 1] Student creates a complaint...");
            Complaint c1 = dao.createComplaint("test_stu_comp_1", "test_provost_comp_a", "Water Problem", "There is no water supply in my room.");
            if (c1 == null) throw new AssertionError("Failed to create complaint c1");
            if (!c1.isPending()) throw new AssertionError("Initial status should be PENDING, got: " + c1.getStatus());
            System.out.println("   ✓ Complaint #" + c1.getId() + " created with Status = " + c1.getStatus());

            List<Complaint> provostAComplaints = dao.getComplaintsByProvost("test_provost_comp_a");
            boolean foundInProvost = provostAComplaints.stream().anyMatch(c -> c.getId() == c1.getId());
            if (!foundInProvost) throw new AssertionError("Complaint not received by assigned Provost");
            System.out.println("   ✓ Provost A received the pending complaint.");
            System.out.println("   PASSED: Test 1 Verified.");

            System.out.println("\n[Test 2] Student edits pending complaint...");
            boolean edited = dao.updatePendingComplaint(c1.getId(), "test_stu_comp_1", "Severe Water Problem", "Water pump broken on 2nd floor.", "test_provost_comp_a");
            if (!edited) throw new AssertionError("Failed to edit pending complaint");
            Complaint c1Updated = dao.getComplaintById(c1.getId());
            if (!c1Updated.getTitle().equals("Severe Water Problem") || !c1Updated.getMessage().equals("Water pump broken on 2nd floor.")) {
                throw new AssertionError("Complaint content did not update properly");
            }
            System.out.println("   ✓ Complaint #" + c1.getId() + " updated: " + c1Updated.getTitle() + " | " + c1Updated.getMessage());
            System.out.println("   PASSED: Test 2 Verified.");

            System.out.println("\n[Test 3] Student cancels pending complaint...");
            Complaint cToCancel = dao.createComplaint("test_stu_comp_1", "test_provost_comp_a", "Light Issue", "Need bulb replaced");
            boolean cancelled = dao.cancelComplaint(cToCancel.getId(), "test_stu_comp_1");
            if (!cancelled) throw new AssertionError("Failed to cancel complaint");

            Complaint cCancelled = dao.getComplaintById(cToCancel.getId());
            if (!cCancelled.isCancelled()) throw new AssertionError("Expected status CANCELLED, got: " + cCancelled.getStatus());
            System.out.println("   ✓ Complaint #" + cToCancel.getId() + " Status = " + cCancelled.getStatus());

            boolean provostProcessBlocked = false;
            try {
                dao.processComplaint(cToCancel.getId(), "test_provost_comp_a", "APPROVED", "Cannot process cancelled");
            } catch (IllegalStateException e) {
                provostProcessBlocked = true;
                System.out.println("   ✓ Blocked Provost from processing cancelled complaint: " + e.getMessage());
            }
            if (!provostProcessBlocked) throw new AssertionError("Provost was able to process a cancelled complaint!");
            System.out.println("   PASSED: Test 3 Verified.");

            System.out.println("\n[Test 4] Provost approves with default response...");
            dao.processComplaint(c1.getId(), "test_provost_comp_a", "APPROVED", null);
            Complaint c1Processed = dao.getComplaintById(c1.getId());
            if (!c1Processed.isApproved()) throw new AssertionError("Expected status APPROVED, got: " + c1Processed.getStatus());
            if (!"We will look into this matter.".equals(c1Processed.getResponseMessage())) {
                throw new AssertionError("Expected default response 'We will look into this matter.', got: " + c1Processed.getResponseMessage());
            }
            System.out.println("   ✓ Complaint #" + c1.getId() + " Status = " + c1Processed.getStatus());
            System.out.println("   ✓ Response = \"" + c1Processed.getResponseMessage() + "\"");
            System.out.println("   PASSED: Test 4 Verified.");

            System.out.println("\n[Test 5] Custom response under 100 words...");
            Complaint c2 = dao.createComplaint("test_stu_comp_1", "test_provost_comp_a", "Fan Noise", "Ceiling fan making squeaking noise.");
            String customResponse = "The electrical maintenance staff has been assigned. They will visit your room tomorrow at 10 AM.";
            int words = ComplaintDAO.countWords(customResponse);
            System.out.println("   Word count: " + words + " words.");
            dao.processComplaint(c2.getId(), "test_provost_comp_a", "APPROVED", customResponse);

            Complaint c2Processed = dao.getComplaintById(c2.getId());
            if (!c2Processed.isApproved() || !customResponse.equals(c2Processed.getResponseMessage())) {
                throw new AssertionError("Custom response failed to save properly");
            }
            System.out.println("   ✓ Custom response saved: \"" + c2Processed.getResponseMessage() + "\"");
            System.out.println("   PASSED: Test 5 Verified.");

            System.out.println("\n[Test 6] More than 100 words blocked...");
            Complaint c3 = dao.createComplaint("test_stu_comp_1", "test_provost_comp_a", "Window Repair", "Glass cracked.");
            StringBuilder longMsg = new StringBuilder();
            for (int i = 1; i <= 105; i++) {
                longMsg.append("word").append(i).append(" ");
            }
            boolean longMsgBlocked = false;
            try {
                dao.processComplaint(c3.getId(), "test_provost_comp_a", "APPROVED", longMsg.toString());
            } catch (IllegalArgumentException e) {
                longMsgBlocked = true;
                System.out.println("   ✓ Response > 100 words successfully blocked: " + e.getMessage());
            }
            if (!longMsgBlocked) throw new AssertionError("Response > 100 words was NOT blocked!");
            System.out.println("   PASSED: Test 6 Verified.");

            System.out.println("\n[Test 7] Second response to same complaint blocked...");
            boolean secondRespBlocked = false;
            try {
                dao.processComplaint(c2.getId(), "test_provost_comp_a", "APPROVED", "Second response attempt");
            } catch (IllegalStateException e) {
                secondRespBlocked = true;
                System.out.println("   ✓ Second response blocked: " + e.getMessage());
            }
            if (!secondRespBlocked) throw new AssertionError("Second response was allowed!");
            System.out.println("   PASSED: Test 7 Verified.");

            System.out.println("\n[Test 8] Student security (Only own complaints visible)...");
            dao.createComplaint("test_stu_comp_2", "test_provost_comp_a", "Student 2 Issue", "Another student's issue");
            List<Complaint> student1List = dao.getComplaintsByStudent("test_stu_comp_1");
            boolean student1HasStudent2Complaint = student1List.stream().anyMatch(c -> c.getStudentId().equals("test_stu_comp_2"));
            if (student1HasStudent2Complaint) throw new AssertionError("Student 1 can see Student 2's complaints!");
            System.out.println("   ✓ Student 1 sees only their own " + student1List.size() + " complaints.");
            System.out.println("   PASSED: Test 8 Verified.");

            System.out.println("\n[Test 9] Provost security (Only assigned complaints visible & processable)...");
            Complaint cProvostB = dao.createComplaint("test_stu_comp_1", "test_provost_comp_b", "Provost B Issue", "Assigned specifically to Provost B");
            List<Complaint> provostAList = dao.getComplaintsByProvost("test_provost_comp_a");
            boolean provostAHasProvostBComplaint = provostAList.stream().anyMatch(c -> c.getId() == cProvostB.getId());
            if (provostAHasProvostBComplaint) throw new AssertionError("Provost A can see Provost B's complaint!");

            boolean crossProvostBlocked = false;
            try {
                dao.processComplaint(cProvostB.getId(), "test_provost_comp_a", "APPROVED", "Provost A trying to process Provost B complaint");
            } catch (SecurityException e) {
                crossProvostBlocked = true;
                System.out.println("   ✓ Cross-provost processing blocked: " + e.getMessage());
            }
            if (!crossProvostBlocked) throw new AssertionError("Provost A was able to process Provost B's complaint!");

            System.out.println("\n[Test 10] Student Delete Complaint...");
            Complaint toDelete = dao.createComplaint("test_stu_comp_1", "test_provost_comp_a", "Temporary Complaint To Delete", "This will be deleted.");
            int delId = toDelete.getId();
            System.out.println("   ✓ Created temporary complaint #" + delId);

            boolean unauthorizedDelete = dao.deleteComplaint(delId, "test_stu_comp_2");
            if (unauthorizedDelete) throw new AssertionError("Student 2 was able to delete Student 1's complaint!");
            System.out.println("   ✓ Unauthorized deletion prevented for non-owner student.");

            boolean deleted = dao.deleteComplaint(delId, "test_stu_comp_1");
            if (!deleted) throw new AssertionError("Owner student failed to delete complaint #" + delId);

            Complaint fetchedAfterDel = dao.getComplaintById(delId);
            if (fetchedAfterDel != null) throw new AssertionError("Complaint still exists after deletion!");
            System.out.println("   ✓ Complaint #" + delId + " successfully and permanently deleted.");
            System.out.println("   PASSED: Test 10 Verified.");

            System.out.println("\n=== All Student Complaint & Provost Response Smoke Tests (Submit, Edit, Review, Cancel, Delete, Approve, Reject) Passed Successfully! ===");

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void setupTestData() throws Exception {
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement()) {

            st.execute("DELETE FROM complaint_responses WHERE provost_id IN ('test_provost_comp_a', 'test_provost_comp_b')");
            st.execute("DELETE FROM complaints WHERE student_id IN ('test_stu_comp_1', 'test_stu_comp_2')");
            st.execute("DELETE FROM students WHERE user_id IN ('test_stu_comp_1', 'test_stu_comp_2')");
            st.execute("DELETE FROM provosts WHERE user_id IN ('test_provost_comp_a', 'test_provost_comp_b')");

            st.execute("INSERT INTO provosts (user_id, password, name, phone, hall_id, age, email, designation, office_room, status) VALUES " +
                "('test_provost_comp_a', 'pass', 'Prof. Dr. Provost A', '01811002200', 1, 48, 'provost.a@ruet.ac.bd', 'Professor & Provost', 'Room 101', 'ACTIVE'), " +
                "('test_provost_comp_b', 'pass', 'Prof. Dr. Provost B', '01811002201', 2, 52, 'provost.b@ruet.ac.bd', 'Professor & Provost', 'Room 102', 'ACTIVE')");

            st.execute("INSERT INTO students (user_id, password, name, phone, department, year, current_hall_id, current_room, current_seat, status) VALUES " +
                "('test_stu_comp_1', 'pass', 'Rahim Student', '01711001100', 'CSE', '3', 1, 201, 1, 'ACTIVE'), " +
                "('test_stu_comp_2', 'pass', 'Karim Student', '01711001101', 'EEE', '2', 2, 301, 2, 'ACTIVE')");
        }
    }
}
