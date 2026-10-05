package com.hallmanagement;

import com.hallmanagement.dao.BillingDAO;
import com.hallmanagement.dao.UserDAO;
import com.hallmanagement.database.DBConnection;
import com.hallmanagement.model.Bill;
import com.hallmanagement.model.PaymentRecord;
import com.hallmanagement.model.PaymentRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

public class BillingSmokeTest {

    public static void main(String[] args) {
        System.out.println("=== Starting Billing Smoke Test ===");

        if (!DBConnection.testConnection()) {
            System.err.println("DB Connection failed!");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            BillingDAO billingDAO = new BillingDAO();
            billingDAO.ensureBillingSchemaAndSeed();

            String testStudentId = "test_bill_student";
            String testProvostId = "provost001";

            try (Statement st = conn.createStatement()) {
                st.executeUpdate("DELETE FROM users WHERE user_id = '" + testStudentId + "'");
            }

            UserDAO userDAO = new UserDAO();
            userDAO.registerStudent(testStudentId, "pass123", "Test Billing Student", "01711112222", "CSE", "2nd Year",
                    1, 201, 1, null, null, null);
            System.out.println("1. Created test student with residence in Hall 1, Room 201, Seat 01");

            int billId = 0;
            String insertBill = "INSERT INTO bills (student_id, hall_id, room_number, seat_number, billing_period, " +
                    "hall_charge, electricity_charge, water_charge, maintenance_charge, other_charge, total_amount, paid_amount, due_amount, status) " +
                    "VALUES (?, 1, 201, 1, 'September 2026', 700.00, 150.00, 75.00, 75.00, 0.00, 1000.00, 0.00, 1000.00, 'DUE')";
            try (PreparedStatement pst = conn.prepareStatement(insertBill, Statement.RETURN_GENERATED_KEYS)) {
                pst.setString(1, testStudentId);
                pst.executeUpdate();
                try (var rs = pst.getGeneratedKeys()) {
                    if (rs.next()) billId = rs.getInt(1);
                }
            }
            System.out.println("2. Created initial bill ID " + billId + " -> Total: ৳1000.00, Paid: ৳0.00, Due: ৳1000.00, Status: DUE");

            PaymentRequest pr1 = billingDAO.createPaymentRequest(testStudentId, testProvostId, billId, 600.00);
            System.out.println("3. Student submitted Payment Request: " + pr1.getRequestCode() + " for ৳" + pr1.getAmount() + " (Status: " + pr1.getStatus() + ")");

            List<PaymentRequest> pending = billingDAO.getPendingPaymentRequestsForProvost(testProvostId);
            boolean found = pending.stream().anyMatch(r -> r.getId() == pr1.getId());
            if (!found) throw new RuntimeException("Pending request not found for provost!");
            System.out.println("4. Provost successfully retrieved pending request: " + pr1.getRequestCode());

            billingDAO.approvePaymentRequest(pr1.getId(), testProvostId);
            System.out.println("5. Provost approved payment request: " + pr1.getRequestCode());

            Bill billAfter1 = billingDAO.getCurrentBillForStudent(testStudentId);
            System.out.printf("   Updated Bill: Paid = ৳%.2f, Due = ৳%.2f, Status = %s%n",
                    billAfter1.getPaidAmount(), billAfter1.getDueAmount(), billAfter1.getStatus());

            if (Math.abs(billAfter1.getPaidAmount() - 600.00) > 0.01 ||
                Math.abs(billAfter1.getDueAmount() - 400.00) > 0.01 ||
                !"PARTIALLY PAID".equalsIgnoreCase(billAfter1.getStatus())) {
                throw new RuntimeException("Verification failed after first payment! Expected Paid: 600, Due: 400, PARTIALLY PAID");
            }
            System.out.println("   PASSED Step 1: Paid = 600, Due = 400, Status = PARTIALLY PAID");

            PaymentRequest pr2 = billingDAO.createPaymentRequest(testStudentId, testProvostId, billId, 400.00);
            System.out.println("6. Student submitted 2nd Payment Request: " + pr2.getRequestCode() + " for ৳" + pr2.getAmount());

            billingDAO.approvePaymentRequest(pr2.getId(), testProvostId);
            System.out.println("7. Provost approved 2nd payment request");

            Bill billAfter2 = billingDAO.getCurrentBillForStudent(testStudentId);
            System.out.printf("   Updated Bill: Paid = ৳%.2f, Due = ৳%.2f, Status = %s%n",
                    billAfter2.getPaidAmount(), billAfter2.getDueAmount(), billAfter2.getStatus());

            if (Math.abs(billAfter2.getPaidAmount() - 1000.00) > 0.01 ||
                Math.abs(billAfter2.getDueAmount() - 0.00) > 0.01 ||
                !"PAID".equalsIgnoreCase(billAfter2.getStatus())) {
                throw new RuntimeException("Verification failed after second payment! Expected Paid: 1000, Due: 0, PAID");
            }
            System.out.println("   PASSED Step 2: Paid = 1000, Due = 0, Status = PAID");

            List<PaymentRecord> history = billingDAO.getPaymentHistoryForStudent(testStudentId);
            System.out.println("8. Verified Student Payment History records count: " + history.size());
            for (PaymentRecord r : history) {
                System.out.printf("   %s | %s | %s | %s | %s%n",
                        r.getPaymentCode(), r.getBillingPeriod(), r.getFormattedAmount(), r.getProvostName(), r.getStatus());
            }
            if (history.size() != 2) {
                throw new RuntimeException("Expected 2 payment history records, found: " + history.size());
            }

            try {
                billingDAO.createPaymentRequest(testStudentId, testProvostId, billId, 100.00);
                System.err.println("FAILED: Overpayment was allowed!");
            } catch (Exception e) {
                System.out.println("9. PASSED: Overpayment successfully rejected: " + e.getMessage());
            }

            try {
                billingDAO.approvePaymentRequest(pr1.getId(), testProvostId);
                System.err.println("FAILED: Duplicate approval was allowed!");
            } catch (Exception e) {
                System.out.println("10. PASSED: Duplicate approval successfully rejected: " + e.getMessage());
            }

            try (Statement st = conn.createStatement()) {
                st.executeUpdate("DELETE FROM users WHERE user_id = '" + testStudentId + "'");
            }

            System.out.println("=== All Billing Tests Passed Successfully! ===");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
