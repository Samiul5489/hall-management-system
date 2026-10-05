package com.hallmanagement.util;

import com.hallmanagement.dao.BillingDAO;
import com.hallmanagement.dao.ComplaintDAO;
import com.hallmanagement.dao.HallChangeDAO;
import com.hallmanagement.dao.HallLeaveDAO;
import com.hallmanagement.dao.NoticeDAO;
import com.hallmanagement.dao.RoomChangeDAO;
import javafx.scene.control.Button;

public class BadgeHelper {

    private static final NoticeDAO noticeDAO = new NoticeDAO();
    private static final BillingDAO billingDAO = new BillingDAO();
    private static final HallChangeDAO hallChangeDAO = new HallChangeDAO();
    private static final RoomChangeDAO roomChangeDAO = new RoomChangeDAO();
    private static final ComplaintDAO complaintDAO = new ComplaintDAO();
    private static final HallLeaveDAO hallLeaveDAO = new HallLeaveDAO();

    public static int updateStudentNoticeBadge(Button btnNoticeBoard, String studentUserId) {
        if (btnNoticeBoard == null || studentUserId == null) return 0;
        int unreadCount = noticeDAO.getUnreadNoticeCountForStudent(studentUserId);
        if (unreadCount > 0) {
            btnNoticeBoard.setText("📢  Notice Board  🔴 (" + unreadCount + ")");
        } else {
            btnNoticeBoard.setText("📢  Notice Board");
        }
        return unreadCount;
    }

    public static int updateStudentComplaintBadge(Button btnComplaints, String studentUserId) {
        if (btnComplaints == null || studentUserId == null) return 0;
        int count = complaintDAO.getActiveOrRespondedCountForStudent(studentUserId);
        if (count > 0) {
            btnComplaints.setText("📝  Complaints  🔴 (" + count + ")");
        } else {
            btnComplaints.setText("📝  Complaints");
        }
        return count;
    }

    public static int updateStudentHallChangeBadge(Button btnHallChange, String studentUserId) {
        if (btnHallChange == null || studentUserId == null) return 0;
        int pendingCount = hallChangeDAO.getPendingRequestCountForStudent(studentUserId);
        if (pendingCount > 0) {
            btnHallChange.setText("🔄  Apply for New Hall  🔴 (" + pendingCount + ")");
        } else {
            btnHallChange.setText("🔄  Apply for New Hall");
        }
        return pendingCount;
    }

    public static int updateStudentRoomChangeBadge(Button btnRoomChange, String studentUserId) {
        if (btnRoomChange == null || studentUserId == null) return 0;
        int count = roomChangeDAO.getUnreviewedProcessedCountForStudent(studentUserId);
        if (count > 0) {
            btnRoomChange.setText("🛏  Room/Seat Change  🔴 (" + count + ")");
        } else {
            btnRoomChange.setText("🛏  Room/Seat Change");
        }
        return count;
    }

    public static int updateStudentHallLeaveBadge(Button btnHallLeave, String studentUserId) {
        if (btnHallLeave == null || studentUserId == null) return 0;
        int count = hallLeaveDAO.getUnreviewedProcessedCountForStudent(studentUserId);
        if (count > 0) {
            btnHallLeave.setText("🚪  Hall Leave  🔴 (" + count + ")");
        } else {
            btnHallLeave.setText("🚪  Hall Leave");
        }
        return count;
    }

    public static int updateStudentBillingBadge(Button btnBilling, String studentUserId) {
        if (btnBilling == null || studentUserId == null) return 0;
        int pendingReqCount = billingDAO.getPendingPaymentRequestCountForStudent(studentUserId);
        int unpaidBills = billingDAO.getUnpaidBillCountForStudent(studentUserId);
        int totalBadge = pendingReqCount + unpaidBills;
        if (totalBadge > 0) {
            btnBilling.setText("💳  Bill & Due  🔴 (" + totalBadge + ")");
        } else {
            btnBilling.setText("💳  Bill & Due");
        }
        return totalBadge;
    }

    public static void updateAllStudentBadges(Button btnNotice, Button btnBilling, Button btnHallChange, Button btnComplaints, String studentUserId) {
        updateAllStudentBadges(btnNotice, btnBilling, btnHallChange, null, btnComplaints, null, studentUserId);
    }

    public static void updateAllStudentBadges(Button btnNotice, Button btnBilling, Button btnHallChange, Button btnComplaints, Button btnHallLeave, String studentUserId) {
        updateAllStudentBadges(btnNotice, btnBilling, btnHallChange, null, btnComplaints, btnHallLeave, studentUserId);
    }

    public static void updateAllStudentBadges(Button btnNotice, Button btnBilling, Button btnHallChange, Button btnRoomChange, Button btnComplaints, Button btnHallLeave, String studentUserId) {
        if (studentUserId == null) return;
        updateStudentNoticeBadge(btnNotice, studentUserId);
        updateStudentBillingBadge(btnBilling, studentUserId);
        updateStudentHallChangeBadge(btnHallChange, studentUserId);
        updateStudentRoomChangeBadge(btnRoomChange, studentUserId);
        updateStudentComplaintBadge(btnComplaints, studentUserId);
        updateStudentHallLeaveBadge(btnHallLeave, studentUserId);
    }

    public static int updateProvostPaymentBadge(Button btnPaymentRequests, String provostUserId) {
        if (btnPaymentRequests == null || provostUserId == null) return 0;
        int pendingCount = billingDAO.getPendingPaymentRequestCountForProvost(provostUserId);
        if (pendingCount > 0) {
            btnPaymentRequests.setText("💳  Payment Requests  🔴 (" + pendingCount + ")");
        } else {
            btnPaymentRequests.setText("💳  Payment Requests");
        }
        return pendingCount;
    }

    public static int updateProvostHallChangeBadge(Button btnHallChangeRequests, String provostUserId) {
        if (btnHallChangeRequests == null || provostUserId == null) return 0;
        int pendingCount = hallChangeDAO.getPendingRequestCountForProvost(provostUserId);
        if (pendingCount > 0) {
            btnHallChangeRequests.setText("🔄  Hall Change Requests  🔴 (" + pendingCount + ")");
        } else {
            btnHallChangeRequests.setText("🔄  Hall Change Requests");
        }
        return pendingCount;
    }

    public static int updateProvostRoomChangeBadge(Button btnRoomChangeRequests, String provostUserId) {
        if (btnRoomChangeRequests == null || provostUserId == null) return 0;
        int pendingCount = roomChangeDAO.getPendingCountForProvost(provostUserId);
        if (pendingCount > 0) {
            btnRoomChangeRequests.setText("🛏  Room/Seat Change Requests  🔴 (" + pendingCount + ")");
        } else {
            btnRoomChangeRequests.setText("🛏  Room/Seat Change Requests");
        }
        return pendingCount;
    }

    public static int updateProvostComplaintBadge(Button btnComplaints, String provostUserId) {
        if (btnComplaints == null || provostUserId == null) return 0;
        int pendingCount = complaintDAO.getPendingComplaintCountForProvost(provostUserId);
        if (pendingCount > 0) {
            btnComplaints.setText("📝  Complaints  🔴 (" + pendingCount + ")");
        } else {
            btnComplaints.setText("📝  Complaints");
        }
        return pendingCount;
    }

    public static int updateProvostHallLeaveBadge(Button btnHallLeaveRequests, String provostUserId) {
        if (btnHallLeaveRequests == null || provostUserId == null) return 0;
        int pendingCount = hallLeaveDAO.getPendingCountForProvost(provostUserId);
        if (pendingCount > 0) {
            btnHallLeaveRequests.setText("🚪  Hall Leave Requests  🔴 (" + pendingCount + ")");
        } else {
            btnHallLeaveRequests.setText("🚪  Hall Leave Requests");
        }
        return pendingCount;
    }

    public static void updateAllProvostBadges(Button btnPayment, Button btnHallChange, Button btnComplaints, String provostUserId) {
        updateAllProvostBadges(btnPayment, btnHallChange, null, btnComplaints, null, provostUserId);
    }

    public static void updateAllProvostBadges(Button btnPayment, Button btnHallChange, Button btnComplaints, Button btnHallLeave, String provostUserId) {
        updateAllProvostBadges(btnPayment, btnHallChange, null, btnComplaints, btnHallLeave, provostUserId);
    }

    public static void updateAllProvostBadges(Button btnPayment, Button btnHallChange, Button btnRoomChange, Button btnComplaints, Button btnHallLeave, String provostUserId) {
        if (provostUserId == null) return;
        updateProvostPaymentBadge(btnPayment, provostUserId);
        updateProvostHallChangeBadge(btnHallChange, provostUserId);
        updateProvostRoomChangeBadge(btnRoomChange, provostUserId);
        updateProvostComplaintBadge(btnComplaints, provostUserId);
        updateProvostHallLeaveBadge(btnHallLeave, provostUserId);
    }
}
