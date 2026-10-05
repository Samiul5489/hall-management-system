package com.hallmanagement.dao;

import com.hallmanagement.database.DBConnection;
import com.hallmanagement.model.Bill;
import com.hallmanagement.model.PaymentRecord;
import com.hallmanagement.model.PaymentRequest;
import com.hallmanagement.model.ProvostInfo;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BillingDAO {

    public BillingDAO() {
        ensureBillingSchemaAndSeed();
    }

    public void ensureBillingSchemaAndSeed() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            String createFeeStruct = "CREATE TABLE IF NOT EXISTS hall_fee_structures (" +
                    "    id INT UNSIGNED NOT NULL AUTO_INCREMENT," +
                    "    hall_id INT UNSIGNED NOT NULL," +
                    "    hall_charge DECIMAL(10,2) NOT NULL DEFAULT 500.00," +
                    "    electricity_charge DECIMAL(10,2) NOT NULL DEFAULT 100.00," +
                    "    water_charge DECIMAL(10,2) NOT NULL DEFAULT 50.00," +
                    "    maintenance_charge DECIMAL(10,2) NOT NULL DEFAULT 50.00," +
                    "    other_charge DECIMAL(10,2) NOT NULL DEFAULT 0.00," +
                    "    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "    PRIMARY KEY (id)," +
                    "    UNIQUE KEY uq_hall_fee (hall_id)," +
                    "    FOREIGN KEY (hall_id) REFERENCES halls(id) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";
            stmt.executeUpdate(createFeeStruct);

            String createBills = "CREATE TABLE IF NOT EXISTS bills (" +
                    "    id INT UNSIGNED NOT NULL AUTO_INCREMENT," +
                    "    student_id VARCHAR(50) NOT NULL," +
                    "    hall_id INT UNSIGNED NOT NULL," +
                    "    room_number INT NOT NULL," +
                    "    seat_number INT NOT NULL," +
                    "    billing_period VARCHAR(50) NOT NULL," +
                    "    hall_charge DECIMAL(10,2) NOT NULL DEFAULT 500.00," +
                    "    electricity_charge DECIMAL(10,2) NOT NULL DEFAULT 100.00," +
                    "    water_charge DECIMAL(10,2) NOT NULL DEFAULT 50.00," +
                    "    maintenance_charge DECIMAL(10,2) NOT NULL DEFAULT 50.00," +
                    "    other_charge DECIMAL(10,2) NOT NULL DEFAULT 0.00," +
                    "    total_amount DECIMAL(10,2) NOT NULL," +
                    "    paid_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00," +
                    "    due_amount DECIMAL(10,2) NOT NULL," +
                    "    status ENUM('DUE', 'PARTIALLY PAID', 'PAID') NOT NULL DEFAULT 'DUE'," +
                    "    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "    PRIMARY KEY (id)," +
                    "    UNIQUE KEY uq_student_bill_period (student_id, billing_period)," +
                    "    FOREIGN KEY (student_id) REFERENCES students(user_id) ON DELETE CASCADE," +
                    "    FOREIGN KEY (hall_id) REFERENCES halls(id) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";
            stmt.executeUpdate(createBills);

            String createPaymentRequests = "CREATE TABLE IF NOT EXISTS payment_requests (" +
                    "    id INT UNSIGNED NOT NULL AUTO_INCREMENT," +
                    "    request_code VARCHAR(20) NOT NULL," +
                    "    student_id VARCHAR(50) NOT NULL," +
                    "    provost_id VARCHAR(50) NOT NULL," +
                    "    bill_id INT UNSIGNED NOT NULL," +
                    "    amount DECIMAL(10,2) NOT NULL," +
                    "    request_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "    status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING'," +
                    "    rejection_reason VARCHAR(255) DEFAULT NULL," +
                    "    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "    PRIMARY KEY (id)," +
                    "    UNIQUE KEY uq_request_code (request_code)," +
                    "    FOREIGN KEY (student_id) REFERENCES students(user_id) ON DELETE CASCADE," +
                    "    FOREIGN KEY (provost_id) REFERENCES provosts(user_id) ON DELETE CASCADE," +
                    "    FOREIGN KEY (bill_id) REFERENCES bills(id) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";
            stmt.executeUpdate(createPaymentRequests);

            String createPayments = "CREATE TABLE IF NOT EXISTS payments (" +
                    "    id INT UNSIGNED NOT NULL AUTO_INCREMENT," +
                    "    payment_code VARCHAR(20) NOT NULL," +
                    "    payment_request_id INT UNSIGNED DEFAULT NULL," +
                    "    student_id VARCHAR(50) NOT NULL," +
                    "    provost_id VARCHAR(50) NOT NULL," +
                    "    bill_id INT UNSIGNED NOT NULL," +
                    "    amount DECIMAL(10,2) NOT NULL," +
                    "    payment_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "    status ENUM('APPROVED') NOT NULL DEFAULT 'APPROVED'," +
                    "    PRIMARY KEY (id)," +
                    "    UNIQUE KEY uq_payment_code (payment_code)," +
                    "    FOREIGN KEY (payment_request_id) REFERENCES payment_requests(id) ON DELETE SET NULL," +
                    "    FOREIGN KEY (student_id) REFERENCES students(user_id) ON DELETE CASCADE," +
                    "    FOREIGN KEY (provost_id) REFERENCES provosts(user_id) ON DELETE CASCADE," +
                    "    FOREIGN KEY (bill_id) REFERENCES bills(id) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";
            stmt.executeUpdate(createPayments);

            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM hall_fee_structures")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    stmt.executeUpdate("INSERT IGNORE INTO hall_fee_structures (hall_id, hall_charge, electricity_charge, water_charge, maintenance_charge, other_charge) VALUES " +
                            "(1, 500.00, 100.00, 50.00, 50.00, 0.00), " +
                            "(2, 500.00, 100.00, 50.00, 50.00, 0.00), " +
                            "(3, 450.00, 100.00, 50.00, 50.00, 0.00), " +
                            "(4, 400.00, 80.00, 40.00, 40.00, 0.00), " +
                            "(5, 550.00, 120.00, 60.00, 50.00, 0.00), " +
                            "(6, 500.00, 100.00, 50.00, 50.00, 0.00), " +
                            "(7, 500.00, 100.00, 50.00, 50.00, 0.00), " +
                            "(8, 500.00, 100.00, 50.00, 50.00, 0.00), " +
                            "(9, 500.00, 100.00, 50.00, 50.00, 0.00), " +
                            "(10, 500.00, 100.00, 50.00, 50.00, 0.00), " +
                            "(11, 500.00, 100.00, 50.00, 50.00, 0.00), " +
                            "(12, 500.00, 100.00, 50.00, 50.00, 0.00)");
                }
            }

            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM bills")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    stmt.executeUpdate("INSERT IGNORE INTO bills (student_id, hall_id, room_number, seat_number, billing_period, hall_charge, electricity_charge, water_charge, maintenance_charge, other_charge, total_amount, paid_amount, due_amount, status) VALUES " +
                            "('2403001', 1, 101, 1, 'August 2026', 500.00, 100.00, 50.00, 50.00, 0.00, 700.00, 0.00, 700.00, 'DUE'), " +
                            "('2403015', 1, 101, 3, 'August 2026', 500.00, 100.00, 50.00, 50.00, 0.00, 700.00, 200.00, 500.00, 'PARTIALLY PAID'), " +
                            "('2403020', 1, 101, 4, 'August 2026', 500.00, 100.00, 50.00, 50.00, 0.00, 700.00, 700.00, 0.00, 'PAID')");
                }
            }

        } catch (SQLException e) {
            System.err.println("[BillingDAO] Error ensuring billing schema: " + e.getMessage());
        }
    }

    public Bill getCurrentBillForStudent(String studentUserId) throws SQLException {
        String sql = "SELECT b.*, h.hall_name " +
                "FROM bills b " +
                "JOIN halls h ON b.hall_id = h.id " +
                "WHERE b.student_id = ? " +
                "ORDER BY b.id DESC LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, studentUserId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBill(rs);
                }
            }
        }

        return generateBillForStudentIfEligible(studentUserId, "August 2026");
    }

    public Bill generateBillForStudentIfEligible(String studentUserId, String billingPeriod) throws SQLException {
        String queryStudent = "SELECT s.current_hall_id, s.current_room, s.current_seat, h.hall_name " +
                "FROM students s " +
                "LEFT JOIN halls h ON s.current_hall_id = h.id " +
                "WHERE s.user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(queryStudent)) {
            pst.setString(1, studentUserId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    int hallId = rs.getInt("current_hall_id");
                    if (rs.wasNull() || hallId == 0) {
                        return null;
                    }
                    int roomNumber = rs.getInt("current_room");
                    int seatNumber = rs.getInt("current_seat");

                    double hallCharge = 500.0;
                    double electCharge = 100.0;
                    double waterCharge = 50.0;
                    double maintCharge = 50.0;
                    double otherCharge = 0.0;

                    String queryFee = "SELECT * FROM hall_fee_structures WHERE hall_id = ?";
                    try (PreparedStatement pstFee = conn.prepareStatement(queryFee)) {
                        pstFee.setInt(1, hallId);
                        try (ResultSet rsFee = pstFee.executeQuery()) {
                            if (rsFee.next()) {
                                hallCharge = rsFee.getDouble("hall_charge");
                                electCharge = rsFee.getDouble("electricity_charge");
                                waterCharge = rsFee.getDouble("water_charge");
                                maintCharge = rsFee.getDouble("maintenance_charge");
                                otherCharge = rsFee.getDouble("other_charge");
                            }
                        }
                    }

                    double total = hallCharge + electCharge + waterCharge + maintCharge + otherCharge;
                    double paid = 0.0;
                    double due = total;

                    String insertBill = "INSERT INTO bills (student_id, hall_id, room_number, seat_number, billing_period, " +
                            "hall_charge, electricity_charge, water_charge, maintenance_charge, other_charge, total_amount, paid_amount, due_amount, status) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'DUE') " +
                            "ON DUPLICATE KEY UPDATE total_amount = total_amount";

                    try (PreparedStatement pstInsert = conn.prepareStatement(insertBill, Statement.RETURN_GENERATED_KEYS)) {
                        pstInsert.setString(1, studentUserId);
                        pstInsert.setInt(2, hallId);
                        pstInsert.setInt(3, roomNumber);
                        pstInsert.setInt(4, seatNumber);
                        pstInsert.setString(5, billingPeriod);
                        pstInsert.setDouble(6, hallCharge);
                        pstInsert.setDouble(7, electCharge);
                        pstInsert.setDouble(8, waterCharge);
                        pstInsert.setDouble(9, maintCharge);
                        pstInsert.setDouble(10, otherCharge);
                        pstInsert.setDouble(11, total);
                        pstInsert.setDouble(12, paid);
                        pstInsert.setDouble(13, due);
                        pstInsert.executeUpdate();
                    }

                    return getCurrentBillForStudent(studentUserId);
                }
            }
        }
        return null;
    }

    public List<ProvostInfo> getProvostsForHall(int hallId) throws SQLException {
        List<ProvostInfo> provosts = new ArrayList<>();
        String sql = "SELECT p.user_id, p.name, p.phone, p.hall_id, h.hall_name " +
                "FROM provosts p " +
                "JOIN halls h ON p.hall_id = h.id " +
                "WHERE p.hall_id = ? " +
                "ORDER BY p.name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, hallId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    provosts.add(new ProvostInfo(
                            rs.getString("user_id"),
                            rs.getString("name"),
                            rs.getString("phone"),
                            rs.getInt("hall_id"),
                            rs.getString("hall_name")
                    ));
                }
            }
        }

        if (provosts.isEmpty()) {
            String fallbackSql = "SELECT p.user_id, p.name, p.phone, p.hall_id, COALESCE(h.hall_name, 'General') AS hall_name " +
                    "FROM provosts p " +
                    "LEFT JOIN halls h ON p.hall_id = h.id " +
                    "ORDER BY p.name";
            try (Connection conn = DBConnection.getConnection();
                 Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(fallbackSql)) {
                while (rs.next()) {
                    provosts.add(new ProvostInfo(
                            rs.getString("user_id"),
                            rs.getString("name"),
                            rs.getString("phone"),
                            rs.getInt("hall_id"),
                            rs.getString("hall_name")
                    ));
                }
            }
        }

        return provosts;
    }

    public PaymentRequest createPaymentRequest(String studentId, String provostId, int billId, double amount) throws Exception {
        if (amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero.");
        }

        try (Connection conn = DBConnection.getConnection()) {

            String checkBill = "SELECT due_amount, total_amount, paid_amount FROM bills WHERE id = ? AND student_id = ?";
            try (PreparedStatement pst = conn.prepareStatement(checkBill)) {
                pst.setInt(1, billId);
                pst.setString(2, studentId);
                try (ResultSet rs = pst.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalArgumentException("Invalid bill selected.");
                    }
                    double due = rs.getDouble("due_amount");
                    if (amount > due) {
                        throw new IllegalArgumentException(String.format("Payment amount (৳%.2f) cannot exceed current due amount (৳%.2f).", amount, due));
                    }
                }
            }

            int nextSerial = 1;
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) + 1 FROM payment_requests")) {
                if (rs.next()) {
                    nextSerial = rs.getInt(1);
                }
            }
            String requestCode = String.format("PR-%03d", nextSerial);

            String insert = "INSERT INTO payment_requests (request_code, student_id, provost_id, bill_id, amount, status) " +
                    "VALUES (?, ?, ?, ?, ?, 'PENDING')";

            try (PreparedStatement pstInsert = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                pstInsert.setString(1, requestCode);
                pstInsert.setString(2, studentId);
                pstInsert.setString(3, provostId);
                pstInsert.setInt(4, billId);
                pstInsert.setDouble(5, amount);
                pstInsert.executeUpdate();
            }

            return getPaymentRequestByCode(requestCode);
        }
    }

    public PaymentRequest getPaymentRequestByCode(String requestCode) throws SQLException {
        String sql = "SELECT pr.*, st.name AS student_name, st.department, " +
                "b.hall_id, h.hall_name, b.room_number, b.seat_number, b.billing_period, " +
                "b.total_amount, b.paid_amount, b.due_amount, up.name AS provost_name " +
                "FROM payment_requests pr " +
                "JOIN students st ON pr.student_id = st.user_id " +
                "JOIN bills b ON pr.bill_id = b.id " +
                "JOIN halls h ON b.hall_id = h.id " +
                "LEFT JOIN provosts up ON pr.provost_id = up.user_id " +
                "WHERE pr.request_code = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, requestCode);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPaymentRequest(rs);
                }
            }
        }
        return null;
    }

    public List<PaymentRequest> getPaymentRequestsByStudent(String studentUserId) throws SQLException {
        List<PaymentRequest> requests = new ArrayList<>();
        String sql = "SELECT pr.*, st.name AS student_name, st.department, " +
                "b.hall_id, h.hall_name, b.room_number, b.seat_number, b.billing_period, " +
                "b.total_amount, b.paid_amount, b.due_amount, up.name AS provost_name " +
                "FROM payment_requests pr " +
                "JOIN students st ON pr.student_id = st.user_id " +
                "JOIN bills b ON pr.bill_id = b.id " +
                "JOIN halls h ON b.hall_id = h.id " +
                "LEFT JOIN provosts up ON pr.provost_id = up.user_id " +
                "WHERE pr.student_id = ? " +
                "ORDER BY pr.id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, studentUserId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    requests.add(mapResultSetToPaymentRequest(rs));
                }
            }
        }
        return requests;
    }

    public List<PaymentRequest> getPendingPaymentRequestsForProvost(String provostUserId) throws SQLException {
        List<PaymentRequest> requests = new ArrayList<>();
        String sql = "SELECT pr.*, st.name AS student_name, st.department, " +
                "b.hall_id, h.hall_name, b.room_number, b.seat_number, b.billing_period, " +
                "b.total_amount, b.paid_amount, b.due_amount, up.name AS provost_name " +
                "FROM payment_requests pr " +
                "JOIN students st ON pr.student_id = st.user_id " +
                "JOIN bills b ON pr.bill_id = b.id " +
                "JOIN halls h ON b.hall_id = h.id " +
                "LEFT JOIN provosts up ON pr.provost_id = up.user_id " +
                "WHERE (pr.provost_id = ? OR b.hall_id IN (SELECT hall_id FROM provosts WHERE user_id = ?)) " +
                "  AND pr.status = 'PENDING' " +
                "ORDER BY pr.id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, provostUserId);
            pst.setString(2, provostUserId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    requests.add(mapResultSetToPaymentRequest(rs));
                }
            }
        }
        return requests;
    }

    public int getPendingPaymentRequestCountForProvost(String provostUserId) {
        if (provostUserId == null || provostUserId.trim().isEmpty()) return 0;
        String sql = "SELECT COUNT(*) FROM payment_requests pr " +
                "JOIN bills b ON pr.bill_id = b.id " +
                "WHERE (pr.provost_id = ? OR b.hall_id IN (SELECT hall_id FROM provosts WHERE user_id = ?)) " +
                "  AND pr.status = 'PENDING'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, provostUserId);
            pst.setString(2, provostUserId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[BillingDAO] Failed to get pending payment request count: " + e.getMessage());
        }
        return 0;
    }

    public int getPendingPaymentRequestCountForStudent(String studentUserId) {
        if (studentUserId == null || studentUserId.trim().isEmpty()) return 0;
        String sql = "SELECT COUNT(*) FROM payment_requests WHERE student_id = ? AND status = 'PENDING'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, studentUserId.trim());
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[BillingDAO] Failed to get student pending payment request count: " + e.getMessage());
        }
        return 0;
    }

    public int getUnpaidBillCountForStudent(String studentUserId) {
        if (studentUserId == null || studentUserId.trim().isEmpty()) return 0;
        String sql = "SELECT COUNT(*) FROM bills WHERE student_id = ? AND status != 'PAID'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, studentUserId.trim());
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[BillingDAO] Failed to get student unpaid bill count: " + e.getMessage());
        }
        return 0;
    }

    public void approvePaymentRequest(int requestId, String provostUserId) throws Exception {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {

                String queryReq = "SELECT pr.*, b.total_amount, b.paid_amount, b.due_amount " +
                        "FROM payment_requests pr " +
                        "JOIN bills b ON pr.bill_id = b.id " +
                        "WHERE pr.id = ? FOR UPDATE";

                double requestAmount = 0;
                int billId = 0;
                String studentId = "";
                double totalBill = 0;
                double prevPaid = 0;
                double currentDue = 0;

                try (PreparedStatement pst = conn.prepareStatement(queryReq)) {
                    pst.setInt(1, requestId);
                    try (ResultSet rs = pst.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("Payment request not found.");
                        }
                        String status = rs.getString("status");
                        if (!"PENDING".equalsIgnoreCase(status)) {
                            throw new IllegalStateException("Payment request is not pending (Current status: " + status + ").");
                        }
                        requestAmount = rs.getDouble("amount");
                        billId = rs.getInt("bill_id");
                        studentId = rs.getString("student_id");
                        totalBill = rs.getDouble("total_amount");
                        prevPaid = rs.getDouble("paid_amount");
                        currentDue = rs.getDouble("due_amount");
                    }
                }

                if (requestAmount > currentDue + 0.001) {
                    throw new IllegalStateException(String.format("Payment amount (৳%.2f) exceeds remaining due (৳%.2f).", requestAmount, currentDue));
                }

                int paymentCount = 1;
                try (Statement st = conn.createStatement();
                     ResultSet rs = st.executeQuery("SELECT COUNT(*) + 1 FROM payments")) {
                    if (rs.next()) {
                        paymentCount = rs.getInt(1);
                    }
                }
                String paymentCode = String.format("PAY-%03d", paymentCount);

                String insertPay = "INSERT INTO payments (payment_code, payment_request_id, student_id, provost_id, bill_id, amount, status) " +
                        "VALUES (?, ?, ?, ?, ?, ?, 'APPROVED')";
                try (PreparedStatement pstPay = conn.prepareStatement(insertPay)) {
                    pstPay.setString(1, paymentCode);
                    pstPay.setInt(2, requestId);
                    pstPay.setString(3, studentId);
                    pstPay.setString(4, provostUserId);
                    pstPay.setInt(5, billId);
                    pstPay.setDouble(6, requestAmount);
                    pstPay.executeUpdate();
                }

                double newPaid = prevPaid + requestAmount;
                double newDue = Math.max(0.0, totalBill - newPaid);
                String newStatus = (newDue <= 0.001) ? "PAID" : "PARTIALLY PAID";

                String updateBill = "UPDATE bills SET paid_amount = ?, due_amount = ?, status = ? WHERE id = ?";
                try (PreparedStatement pstBill = conn.prepareStatement(updateBill)) {
                    pstBill.setDouble(1, newPaid);
                    pstBill.setDouble(2, newDue);
                    pstBill.setString(3, newStatus);
                    pstBill.setInt(4, billId);
                    pstBill.executeUpdate();
                }

                String updateReq = "UPDATE payment_requests SET status = 'APPROVED' WHERE id = ?";
                try (PreparedStatement pstReq = conn.prepareStatement(updateReq)) {
                    pstReq.setInt(1, requestId);
                    pstReq.executeUpdate();
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

    public void rejectPaymentRequest(int requestId, String provostUserId, String reason) throws Exception {
        String sql = "UPDATE payment_requests SET status = 'REJECTED', rejection_reason = ? WHERE id = ? AND status = 'PENDING'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, reason != null ? reason.trim() : "Rejected by Provost");
            pst.setInt(2, requestId);
            int rows = pst.executeUpdate();
            if (rows == 0) {
                throw new IllegalStateException("Payment request not found or already processed.");
            }
        }
    }

    public List<PaymentRecord> getPaymentHistoryForStudent(String studentUserId) throws SQLException {
        List<PaymentRecord> records = new ArrayList<>();
        String sql = "SELECT p.*, st.name AS student_name, up.name AS provost_name, " +
                "b.billing_period, h.hall_name, b.room_number, b.seat_number " +
                "FROM payments p " +
                "JOIN students st ON p.student_id = st.user_id " +
                "LEFT JOIN provosts up ON p.provost_id = up.user_id " +
                "JOIN bills b ON p.bill_id = b.id " +
                "JOIN halls h ON b.hall_id = h.id " +
                "WHERE p.student_id = ? " +
                "ORDER BY p.id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, studentUserId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    records.add(mapResultSetToPaymentRecord(rs));
                }
            }
        }
        return records;
    }

    public List<PaymentRecord> getPaymentHistoryForProvost(String provostUserId, String searchKeyword) throws SQLException {
        List<PaymentRecord> records = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT p.*, st.name AS student_name, up.name AS provost_name, " +
                "b.billing_period, h.hall_name, b.room_number, b.seat_number " +
                "FROM payments p " +
                "JOIN students st ON p.student_id = st.user_id " +
                "LEFT JOIN provosts up ON p.provost_id = up.user_id " +
                "JOIN bills b ON p.bill_id = b.id " +
                "JOIN halls h ON b.hall_id = h.id " +
                "WHERE (p.provost_id = ? OR b.hall_id IN (SELECT hall_id FROM provosts WHERE user_id = ?)) ");

        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            sql.append("AND (st.name LIKE ? OR p.student_id LIKE ? OR b.billing_period LIKE ? OR h.hall_name LIKE ? OR CAST(b.room_number AS CHAR) LIKE ?) ");
        }
        sql.append("ORDER BY p.id DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql.toString())) {
            pst.setString(1, provostUserId);
            pst.setString(2, provostUserId);
            if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                String kw = "%" + searchKeyword.trim() + "%";
                pst.setString(3, kw);
                pst.setString(4, kw);
                pst.setString(5, kw);
                pst.setString(6, kw);
                pst.setString(7, kw);
            }
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    records.add(mapResultSetToPaymentRecord(rs));
                }
            }
        }
        return records;
    }

    private Bill mapResultSetToBill(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("created_at");
        LocalDateTime created = (ts != null) ? ts.toLocalDateTime() : null;

        return new Bill(
                rs.getInt("id"),
                rs.getString("student_id"),
                rs.getInt("hall_id"),
                rs.getString("hall_name"),
                rs.getInt("room_number"),
                rs.getInt("seat_number"),
                rs.getString("billing_period"),
                rs.getDouble("hall_charge"),
                rs.getDouble("electricity_charge"),
                rs.getDouble("water_charge"),
                rs.getDouble("maintenance_charge"),
                rs.getDouble("other_charge"),
                rs.getDouble("total_amount"),
                rs.getDouble("paid_amount"),
                rs.getDouble("due_amount"),
                rs.getString("status"),
                created
        );
    }

    private PaymentRequest mapResultSetToPaymentRequest(ResultSet rs) throws SQLException {
        PaymentRequest pr = new PaymentRequest();
        pr.setId(rs.getInt("id"));
        pr.setRequestCode(rs.getString("request_code"));
        pr.setStudentId(rs.getString("student_id"));
        pr.setStudentName(rs.getString("student_name"));
        pr.setStudentRoll(rs.getString("student_id"));
        pr.setStudentDept(rs.getString("department"));
        pr.setHallId(rs.getInt("hall_id"));
        pr.setHallName(rs.getString("hall_name"));
        pr.setRoomNumber(rs.getInt("room_number"));
        pr.setSeatNumber(rs.getInt("seat_number"));
        pr.setProvostId(rs.getString("provost_id"));
        pr.setProvostName(rs.getString("provost_name"));
        pr.setBillId(rs.getInt("bill_id"));
        pr.setBillingPeriod(rs.getString("billing_period"));
        pr.setTotalBill(rs.getDouble("total_amount"));
        pr.setPreviouslyPaid(rs.getDouble("paid_amount"));
        pr.setCurrentDue(rs.getDouble("due_amount"));
        pr.setAmount(rs.getDouble("amount"));

        Timestamp ts = rs.getTimestamp("request_date");
        pr.setRequestDate(ts != null ? ts.toLocalDateTime() : null);

        pr.setStatus(rs.getString("status"));
        pr.setRejectionReason(rs.getString("rejection_reason"));
        return pr;
    }

    private PaymentRecord mapResultSetToPaymentRecord(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("payment_date");
        LocalDateTime payDate = (ts != null) ? ts.toLocalDateTime() : null;

        return new PaymentRecord(
                rs.getInt("id"),
                rs.getString("payment_code"),
                rs.getInt("payment_request_id"),
                rs.getString("student_id"),
                rs.getString("student_name"),
                rs.getString("student_id"),
                rs.getString("provost_id"),
                rs.getString("provost_name"),
                rs.getInt("bill_id"),
                rs.getString("billing_period"),
                rs.getString("hall_name"),
                rs.getInt("room_number"),
                rs.getInt("seat_number"),
                rs.getDouble("amount"),
                payDate,
                rs.getString("status")
        );
    }
}
