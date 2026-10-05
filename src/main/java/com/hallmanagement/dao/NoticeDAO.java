package com.hallmanagement.dao;

import com.hallmanagement.model.Notice;
import com.hallmanagement.database.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NoticeDAO {

    public NoticeDAO() {
        try {
            ensureNoticeSchemaAndSeed();
        } catch (SQLException e) {
            System.err.println("[NoticeDAO] Failed to ensure schema: " + e.getMessage());
        }
    }

    public synchronized void ensureNoticeSchemaAndSeed() throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            String createTableSql =
                "CREATE TABLE IF NOT EXISTS notices (" +
                "    id           INT UNSIGNED    NOT NULL AUTO_INCREMENT," +
                "    title        VARCHAR(255)    NOT NULL," +
                "    content      TEXT            NOT NULL," +
                "    posted_date  DATE            NOT NULL," +
                "    posted_by    VARCHAR(50)     NOT NULL," +
                "    created_at   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "    updated_at   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "    PRIMARY KEY (id)," +
                "    FOREIGN KEY (posted_by) REFERENCES provosts(user_id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

            String createReadsTableSql =
                "CREATE TABLE IF NOT EXISTS student_notice_reads (" +
                "    id         INT UNSIGNED    NOT NULL AUTO_INCREMENT," +
                "    student_id VARCHAR(50)     NOT NULL," +
                "    notice_id  INT UNSIGNED    NOT NULL," +
                "    read_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "    PRIMARY KEY (id)," +
                "    UNIQUE KEY uq_student_notice (student_id, notice_id)," +
                "    FOREIGN KEY (student_id) REFERENCES students(user_id) ON DELETE CASCADE," +
                "    FOREIGN KEY (notice_id) REFERENCES notices(id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

            stmt.executeUpdate(createTableSql);
            stmt.executeUpdate(createReadsTableSql);

            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM notices");
            if (rs.next() && rs.getInt(1) == 0) {
                String seedSql =
                    "INSERT INTO notices (title, content, posted_date, posted_by) VALUES " +
                    "('Hall Seat Allocation 2026', 'Seat allocation results for the upcoming academic session have been published on the hall notice board and online portal. Selected resident students are advised to report to the provost office for document verification.', '2026-09-02', 'provost001'), " +
                    "('Monthly Hall & Dining Bill Notice', 'All residential students are instructed to clear their monthly hall charges, electricity and maintenance dues by the 15th of this month. Please submit the physical payment to the provost office and confirm your payment request.', '2026-09-01', 'provost001'), " +
                    "('General Hall Meeting', 'A general meeting with all hall residents will be held this Friday after Maghrib prayer in the central auditorium. Discussions will focus on dining facility improvements and hall cleanliness.', '2026-08-29', 'provost001'), " +
                    "('Hall Maintenance & Cleanliness Drive', 'A collective cleanliness drive will take place this Saturday across all blocks. All students are requested to cooperate with the hall staff and maintain hygiene standards in common corridors and washrooms.', '2026-08-25', 'provost001')";

                stmt.executeUpdate(seedSql);
            }
        }
    }

    public List<Notice> getAllNoticesForStudent(String studentUserId) throws SQLException {
        ensureNoticeSchemaAndSeed();
        List<Notice> list = new ArrayList<>();
        String sql =
            "SELECT n.*, u.name AS posted_by_name, " +
            "       (CASE WHEN snr.id IS NOT NULL THEN 1 ELSE 0 END) AS is_read " +
            "FROM notices n " +
            "LEFT JOIN provosts u ON u.user_id = n.posted_by " +
            "LEFT JOIN student_notice_reads snr ON snr.notice_id = n.id AND snr.student_id = ? " +
            "ORDER BY n.posted_date DESC, n.id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, studentUserId);
            try (ResultSet rs = ps.executeQuery()) {
                int index = 1;
                while (rs.next()) {
                    boolean isRead = rs.getInt("is_read") == 1;
                    Notice n = new Notice(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getDate("posted_date"),
                        rs.getString("posted_by"),
                        rs.getString("posted_by_name"),
                        rs.getTimestamp("created_at"),
                        rs.getTimestamp("updated_at"),
                        isRead
                    );
                    n.setSerial(String.format("%02d", index++));
                    list.add(n);
                }
            }
        }
        return list;
    }

    public int getUnreadNoticeCountForStudent(String studentUserId) {
        if (studentUserId == null || studentUserId.trim().isEmpty()) return 0;
        try {
            ensureNoticeSchemaAndSeed();
            String sql =
                "SELECT COUNT(*) FROM notices n " +
                "WHERE n.id NOT IN (SELECT notice_id FROM student_notice_reads WHERE student_id = ?)";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, studentUserId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[NoticeDAO] Failed to get unread notice count: " + e.getMessage());
        }
        return 0;
    }

    public void markNoticeAsRead(String studentUserId, int noticeId) {
        if (studentUserId == null || noticeId <= 0) return;
        try {
            ensureNoticeSchemaAndSeed();
            String sql = "INSERT IGNORE INTO student_notice_reads (student_id, notice_id) VALUES (?, ?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, studentUserId);
                ps.setInt(2, noticeId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("[NoticeDAO] Failed to mark notice read: " + e.getMessage());
        }
    }

    public void markAllNoticesAsRead(String studentUserId) {
        if (studentUserId == null || studentUserId.trim().isEmpty()) return;
        try {
            ensureNoticeSchemaAndSeed();
            String sql = "INSERT IGNORE INTO student_notice_reads (student_id, notice_id) SELECT ?, id FROM notices";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, studentUserId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("[NoticeDAO] Failed to mark all notices read: " + e.getMessage());
        }
    }

    public List<Notice> getAllNotices() throws SQLException {
        ensureNoticeSchemaAndSeed();
        List<Notice> list = new ArrayList<>();
        String sql =
            "SELECT n.*, u.name AS posted_by_name " +
            "FROM notices n " +
            "LEFT JOIN provosts u ON u.user_id = n.posted_by " +
            "ORDER BY n.posted_date DESC, n.id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            int index = 1;
            while (rs.next()) {
                Notice n = new Notice(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("content"),
                    rs.getDate("posted_date"),
                    rs.getString("posted_by"),
                    rs.getString("posted_by_name"),
                    rs.getTimestamp("created_at"),
                    rs.getTimestamp("updated_at")
                );

                n.setSerial(String.format("%02d", index++));
                list.add(n);
            }
        }
        return list;
    }

    public Notice getNoticeById(int id) throws SQLException {
        ensureNoticeSchemaAndSeed();
        String sql =
            "SELECT n.*, u.name AS posted_by_name " +
            "FROM notices n " +
            "LEFT JOIN provosts u ON u.user_id = n.posted_by " +
            "WHERE n.id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Notice(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getDate("posted_date"),
                        rs.getString("posted_by"),
                        rs.getString("posted_by_name"),
                        rs.getTimestamp("created_at"),
                        rs.getTimestamp("updated_at")
                    );
                }
            }
        }
        return null;
    }

    public int createNotice(String title, String content, String provostUserId) throws SQLException, SecurityException {
        ensureNoticeSchemaAndSeed();

        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Notice title cannot be empty.");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Notice content cannot be empty.");
        }

        verifyProvostAuthorization(provostUserId);

        String sql = "INSERT INTO notices (title, content, posted_date, posted_by) VALUES (?, ?, CURDATE(), ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, title.trim());
            ps.setString(2, content.trim());
            ps.setString(3, provostUserId);

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        }
        return -1;
    }

    public boolean updateNotice(int id, String title, String content, String provostUserId) throws SQLException, SecurityException {
        ensureNoticeSchemaAndSeed();

        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Notice title cannot be empty.");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Notice content cannot be empty.");
        }

        verifyProvostAuthorization(provostUserId);

        String sql = "UPDATE notices SET title = ?, content = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, title.trim());
            ps.setString(2, content.trim());
            ps.setInt(3, id);

            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteNotice(int id, String provostUserId) throws SQLException, SecurityException {
        ensureNoticeSchemaAndSeed();
        verifyProvostAuthorization(provostUserId);

        String sql = "DELETE FROM notices WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private void verifyProvostAuthorization(String userId) throws SQLException, SecurityException {
        String roleSql = "SELECT user_id FROM provosts WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(roleSql)) {

            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SecurityException("Unauthorized action: Only Provost / Admin authority can create, edit, or delete notices.");
                }
            }
        }
    }
}
