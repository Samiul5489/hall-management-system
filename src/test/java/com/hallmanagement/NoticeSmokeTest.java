package com.hallmanagement;

import com.hallmanagement.dao.NoticeDAO;
import com.hallmanagement.model.Notice;

import java.util.List;

public class NoticeSmokeTest {

    public static void main(String[] args) {
        System.out.println("=== Starting Notice Board Smoke Test ===");

        try {
            NoticeDAO noticeDAO = new NoticeDAO();
            noticeDAO.ensureNoticeSchemaAndSeed();

            List<Notice> initialList = noticeDAO.getAllNotices();
            System.out.println("1. Initial notices count: " + initialList.size());
            if (initialList.isEmpty()) {
                throw new AssertionError("Notices list should not be empty after seeding.");
            }

            for (Notice n : initialList) {
                System.out.println("   [" + n.getSerial() + "] " + n.getFormattedDate() + " | " + n.getTitle() + " | " + n.getContentPreview());
            }

            for (int i = 0; i < initialList.size() - 1; i++) {
                Notice cur = initialList.get(i);
                Notice next = initialList.get(i + 1);
                if (cur.getPostedDate().before(next.getPostedDate())) {
                    throw new AssertionError("Notices not sorted in newest-first order: " + cur.getFormattedDate() + " vs " + next.getFormattedDate());
                }
            }
            System.out.println("   PASSED: Newest notice first sorting validated.");

            String testTitle = "Urgent Water Pipeline Repair";
            String testContent = "Water supply will be temporarily suspended tomorrow from 10 AM to 2 PM for pipeline repairs.";
            int createdId = noticeDAO.createNotice(testTitle, testContent, "provost001");
            System.out.println("2. Provost created notice ID: " + createdId);
            if (createdId <= 0) {
                throw new AssertionError("Failed to create notice.");
            }

            List<Notice> afterCreateList = noticeDAO.getAllNotices();
            Notice topNotice = afterCreateList.get(0);
            if (!testTitle.equals(topNotice.getTitle()) || !"01".equals(topNotice.getSerial())) {
                throw new AssertionError("Newly posted notice is not at top serial 01. Got serial: " + topNotice.getSerial() + ", title: " + topNotice.getTitle());
            }
            System.out.println("   PASSED: New notice immediately appeared at serial 01: " + topNotice.getTitle());

            try {
                noticeDAO.createNotice("Hacked Notice", "Should fail", "2403001");
                throw new AssertionError("FAILED: Student was able to create notice!");
            } catch (SecurityException e) {
                System.out.println("4. PASSED: Student create notice successfully rejected: " + e.getMessage());
            }

            String updatedTitle = "Urgent Water Pipeline Repair [UPDATED TIME]";
            String updatedContent = "Water supply suspension revised from 11 AM to 1 PM. Please store sufficient water.";
            boolean edited = noticeDAO.updateNotice(createdId, updatedTitle, updatedContent, "provost001");
            if (!edited) {
                throw new AssertionError("Failed to edit notice.");
            }
            Notice updatedNotice = noticeDAO.getNoticeById(createdId);
            if (!updatedTitle.equals(updatedNotice.getTitle()) || !updatedContent.equals(updatedNotice.getContent())) {
                throw new AssertionError("Notice was not updated with new content.");
            }
            System.out.println("5. PASSED: Provost edited notice successfully: " + updatedNotice.getTitle());

            try {
                noticeDAO.updateNotice(createdId, "Student Hack Edit", "Should fail", "2403001");
                throw new AssertionError("FAILED: Student was able to edit notice!");
            } catch (SecurityException e) {
                System.out.println("6. PASSED: Student edit notice successfully rejected: " + e.getMessage());
            }

            try {
                noticeDAO.deleteNotice(createdId, "2403001");
                throw new AssertionError("FAILED: Student was able to delete notice!");
            } catch (SecurityException e) {
                System.out.println("7. PASSED: Student delete notice successfully rejected: " + e.getMessage());
            }

            boolean deleted = noticeDAO.deleteNotice(createdId, "provost001");
            if (!deleted) {
                throw new AssertionError("Failed to delete notice.");
            }
            List<Notice> afterDeleteList = noticeDAO.getAllNotices();
            for (Notice n : afterDeleteList) {
                if (n.getId() == createdId) {
                    throw new AssertionError("Deleted notice still present in database!");
                }
            }
            System.out.println("8. PASSED: Provost deleted notice successfully. Total remaining: " + afterDeleteList.size());
            System.out.println("   First notice serial is: " + afterDeleteList.get(0).getSerial() + " (" + afterDeleteList.get(0).getTitle() + ")");

            System.out.println("=== All Notice Board Smoke Tests Passed Successfully! ===");

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
