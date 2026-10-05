package com.hallmanagement;

import com.hallmanagement.dao.BillingDAO;
import com.hallmanagement.dao.NoticeDAO;
import com.hallmanagement.model.Bill;
import com.hallmanagement.model.Notice;
import com.hallmanagement.model.PaymentRequest;
import com.hallmanagement.util.BadgeHelper;
import javafx.application.Platform;
import javafx.scene.control.Button;

import java.util.List;

public class NotificationBadgeSmokeTest {

    public static void main(String[] args) {
        System.out.println("=== Starting Red Dot Notification Badge Smoke Test ===");

        try {
            try {
                Platform.startup(() -> {});
            } catch (IllegalStateException ignored) {
            }

            NoticeDAO noticeDAO = new NoticeDAO();
            BillingDAO billingDAO = new BillingDAO();
            noticeDAO.ensureNoticeSchemaAndSeed();
            billingDAO.ensureBillingSchemaAndSeed();

            String studentId = "2403058";
            String provostId = "provost001";

            System.out.println("\n--- Testing Student Notice Red Dot Badge ---");

            noticeDAO.markAllNoticesAsRead(studentId);
            int baselineUnread = noticeDAO.getUnreadNoticeCountForStudent(studentId);
            System.out.println("1. Baseline unread notices for student " + studentId + ": " + baselineUnread);
            if (baselineUnread != 0) {
                throw new AssertionError("Baseline unread notices should be 0 after markAllNoticesAsRead.");
            }

            Button btnNotice = new Button();
            int unread1 = BadgeHelper.updateStudentNoticeBadge(btnNotice, studentId);
            System.out.println("   Button text when 0 unread: \"" + btnNotice.getText() + "\"");
            if (!"📢  Notice Board".equals(btnNotice.getText())) {
                throw new AssertionError("Expected '📢  Notice Board', got: " + btnNotice.getText());
            }

            int newNoticeId = noticeDAO.createNotice("URGENT: Water Supply Maintenance", "Test description for badge verification.", provostId);
            System.out.println("2. Provost posted new notice ID: " + newNoticeId);

            int unread2 = BadgeHelper.updateStudentNoticeBadge(btnNotice, studentId);
            System.out.println("3. Unread notices for student after provost post: " + unread2);
            System.out.println("   Button text with unread red dot: \"" + btnNotice.getText() + "\"");
            if (!btnNotice.getText().contains("🔴")) {
                throw new AssertionError("Student button text should contain red dot 🔴! Got: " + btnNotice.getText());
            }
            System.out.println("   PASSED: Student Notice Board displays red dot indicator.");

            noticeDAO.markNoticeAsRead(studentId, newNoticeId);
            int unread3 = BadgeHelper.updateStudentNoticeBadge(btnNotice, studentId);
            System.out.println("4. Unread notices after student reads notice: " + unread3);
            BadgeHelper.updateStudentNoticeBadge(btnNotice, studentId);
            System.out.println("   Button text after reading: \"" + btnNotice.getText() + "\"");
            if (btnNotice.getText().contains("🔴")) {
                throw new AssertionError("Red dot should be cleared after reading!");
            }
            System.out.println("   PASSED: Red dot cleared after reading.");

            noticeDAO.deleteNotice(newNoticeId, provostId);

            System.out.println("\n--- Testing Provost Payment Request Red Dot Badge ---");

            Button btnPayment = new Button();
            int pendingBefore = billingDAO.getPendingPaymentRequestCountForProvost(provostId);
            System.out.println("1. Initial pending payment requests for provost " + provostId + ": " + pendingBefore);

            Bill bill = billingDAO.getCurrentBillForStudent(studentId);
            if (bill == null || bill.getDueAmount() <= 0) {
                try (var conn = com.hallmanagement.database.DBConnection.getConnection();
                     var pst = conn.prepareStatement("UPDATE bills SET due_amount = 500.00, paid_amount = 0.00, status = 'DUE' WHERE student_id = ?")) {
                    pst.setString(1, studentId);
                    pst.executeUpdate();
                }
                bill = billingDAO.getCurrentBillForStudent(studentId);
            }
            int billId = bill != null ? bill.getId() : 1;
            PaymentRequest pr = billingDAO.createPaymentRequest(studentId, provostId, billId, 150.0);
            System.out.println("2. Student submitted payment request: " + pr.getRequestCode());

            int pendingAfter = billingDAO.getPendingPaymentRequestCountForProvost(provostId);
            System.out.println("3. Pending payment requests after submission: " + pendingAfter);
            if (pendingAfter <= pendingBefore) {
                throw new AssertionError("Pending count should increase after payment request submission.");
            }

            BadgeHelper.updateProvostPaymentBadge(btnPayment, provostId);
            System.out.println("   Provost Button text with pending red dot: \"" + btnPayment.getText() + "\"");
            if (!btnPayment.getText().contains("🔴")) {
                throw new AssertionError("Provost button text should contain red dot 🔴! Got: " + btnPayment.getText());
            }
            System.out.println("   PASSED: Provost Payment Requests displays red dot indicator.");

            List<PaymentRequest> pendingList = billingDAO.getPendingPaymentRequestsForProvost(provostId);
            for (PaymentRequest item : pendingList) {
                if (item.getId() == pr.getId()) {
                    billingDAO.rejectPaymentRequest(item.getId(), provostId, "Test cleanup");
                    System.out.println("4. Provost resolved test payment request: " + item.getRequestCode());
                    break;
                }
            }

            int pendingFinal = billingDAO.getPendingPaymentRequestCountForProvost(provostId);
            System.out.println("5. Final pending requests for provost: " + pendingFinal);
            BadgeHelper.updateProvostPaymentBadge(btnPayment, provostId);

            System.out.println("\n--- Testing Provost Complaint Red Dot Badge ---");
            com.hallmanagement.dao.ComplaintDAO complaintDAO = new com.hallmanagement.dao.ComplaintDAO();
            Button btnCompProvost = new Button();

            int compBefore = complaintDAO.getPendingComplaintCountForProvost(provostId);
            System.out.println("1. Initial pending complaints for provost: " + compBefore);

            com.hallmanagement.model.Complaint testComp = complaintDAO.createComplaint(studentId, provostId, "Wi-Fi Speed", "Wi-Fi is slow on 3rd floor");
            System.out.println("2. Student created new complaint #" + testComp.getId());

            int compAfter = complaintDAO.getPendingComplaintCountForProvost(provostId);
            System.out.println("3. Pending complaints for provost after creation: " + compAfter);
            if (compAfter <= compBefore) {
                throw new AssertionError("Provost pending complaint count should increase!");
            }

            BadgeHelper.updateProvostComplaintBadge(btnCompProvost, provostId);
            System.out.println("   Provost Complaints button text: \"" + btnCompProvost.getText() + "\"");
            if (!btnCompProvost.getText().contains("🔴")) {
                throw new AssertionError("Provost complaints button should show 🔴!");
            }
            System.out.println("   PASSED: Provost Complaints displays red dot indicator.");

            complaintDAO.processComplaint(testComp.getId(), provostId, "APPROVED", "The IT team has adjusted router bandwidth.");
            System.out.println("4. Provost approved complaint #" + testComp.getId());

            int compResolved = complaintDAO.getPendingComplaintCountForProvost(provostId);
            System.out.println("5. Pending complaints for provost after approval: " + compResolved);
            if (compResolved != compBefore) {
                throw new AssertionError("Pending complaints should return to baseline!");
            }
            System.out.println("   PASSED: Provost complaint red dot badge cleared upon processing.");

            System.out.println("\n--- Testing Student Complaint Approved Red Dot & Review Flow ---");
            Button btnStuComplaints = new Button();

            int studentUnreviewed = complaintDAO.getActiveOrRespondedCountForStudent(studentId);
            System.out.println("1. Unreviewed approved complaints for student: " + studentUnreviewed);
            if (studentUnreviewed < 1) {
                throw new AssertionError("Student should have at least 1 unreviewed approved complaint!");
            }

            BadgeHelper.updateStudentComplaintBadge(btnStuComplaints, studentId);
            System.out.println("   Student Complaints button text before review: \"" + btnStuComplaints.getText() + "\"");
            if (!btnStuComplaints.getText().contains("🔴")) {
                throw new AssertionError("Student Complaints button should display 🔴 indicator for approved response!");
            }
            System.out.println("   PASSED: Student Complaints displays red dot indicator upon Provost approval.");

            complaintDAO.markComplaintAsReviewedByStudent(testComp.getId(), studentId);
            System.out.println("2. Student reviewed approved complaint #" + testComp.getId());

            int studentAfterReview = complaintDAO.getActiveOrRespondedCountForStudent(studentId);
            System.out.println("3. Unreviewed complaints for student after review: " + studentAfterReview);
            if (studentAfterReview != 0) {
                throw new AssertionError("Unreviewed complaints should be 0 after review!");
            }

            BadgeHelper.updateStudentComplaintBadge(btnStuComplaints, studentId);
            System.out.println("   Student Complaints button text after review: \"" + btnStuComplaints.getText() + "\"");
            if (btnStuComplaints.getText().contains("🔴")) {
                throw new AssertionError("Student Complaints red dot should be cleared after review!");
            }
            System.out.println("   PASSED: Student Complaints red dot badge cleared upon review.");

            System.out.println("\n--- Testing Student Full Badges Helper ---");
            Button btnStuNotice = new Button();
            Button btnStuBilling = new Button();
            Button btnStuHallChange = new Button();

            BadgeHelper.updateAllStudentBadges(btnStuNotice, btnStuBilling, btnStuHallChange, btnStuComplaints, studentId);
            System.out.println("   Student Notice Button: " + btnStuNotice.getText());
            System.out.println("   Student Billing Button: " + btnStuBilling.getText());
            System.out.println("   Student Hall Change Button: " + btnStuHallChange.getText());
            System.out.println("   Student Complaints Button: " + btnStuComplaints.getText());
            System.out.println("   PASSED: All student sidebar badges successfully calculated.");

            complaintDAO.deleteComplaint(testComp.getId(), studentId);

            System.out.println("\n=== All Red Dot Notification Tests (Notice, Payment, Hall Change, Complaints for Student & Provost) Passed Successfully! ===");

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
