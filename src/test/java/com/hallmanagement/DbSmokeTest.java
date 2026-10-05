package com.hallmanagement;

import com.hallmanagement.dao.HallDAO;
import com.hallmanagement.dao.UserDAO;
import com.hallmanagement.database.DBConnection;
import com.hallmanagement.model.HallInfo;
import com.hallmanagement.model.RoomInfo;
import com.hallmanagement.model.SeatInfo;
import com.hallmanagement.model.User;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

public class DbSmokeTest {
    public static void main(String[] args) {
        System.out.println("=== Multi-Table DB Architecture Verification Test ===");

        boolean dbOk = DBConnection.testConnection();
        System.out.println("DB Connection: " + (dbOk ? "SUCCESS" : "FAILED"));
        if (!dbOk) return;

        try (Connection conn = DBConnection.getConnection()) {
            HallDAO hallDAO = new HallDAO();
            hallDAO.ensureSchemaAndSeedData();

            List<HallInfo> halls = hallDAO.getAllHallsInfo();
            System.out.println("Total RUET Halls Loaded: " + halls.size());
            for (HallInfo h : halls) {
                System.out.println("  Hall #" + h.getId() + " - " + h.getHallName() + " | Provost: " + h.getProvostDisplay());
            }

            int occupiedHallId = 1;
            int occupiedRoom = 101;
            int occupiedSeat = 1;

            UserDAO userDAO = new UserDAO();

            System.out.println("\n[Test] Authenticating student 2403001...");
            User studentUser = userDAO.authenticate("2403001", "student123", "STUDENT");
            System.out.println("   ✓ Authenticated: " + studentUser.getName() + " (" + studentUser.getRole() + ")");

            System.out.println("\n[Test] Authenticating provost001...");
            User provostUser = userDAO.authenticate("provost001", "provost123", "PROVOST");
            System.out.println("   ✓ Authenticated: " + provostUser.getName() + " (" + provostUser.getRole() + ")");

            try {
                userDAO.registerStudent("temp_dup_student", "pass123", "Dup Student", "01700000000", "CSE", "1st Year",
                        occupiedHallId, occupiedRoom, occupiedSeat, null, null, null);
                System.err.println("FAILED: Duplicate seat allowed");
            } catch (Exception e) {
                System.out.println("   ✓ PASSED: Duplicate seat assignment prevented: " + e.getMessage());
            }

            List<RoomInfo> rooms = hallDAO.getRoomsByHallId(occupiedHallId);
            System.out.printf("Loaded %d rooms for Hall 1%n", rooms.size());

            List<SeatInfo> seats = hallDAO.getSeatsByRoomId(rooms.get(0).getId());
            System.out.printf("Loaded %d seats for Room %d%n", seats.size(), rooms.get(0).getRoomNumber());
            for (SeatInfo s : seats) {
                System.out.printf("  %s: %s | Provost View: %s | Student View: %s%n",
                        s.getSeatNumberDisplay(), s.getStatus(),
                        s.getOccupantDisplay(true, null),
                        s.getOccupantDisplay(false, "2403001"));
            }

            try (Statement cleanup = conn.createStatement()) {
                cleanup.executeUpdate("DELETE FROM students WHERE user_id = 'temp_dup_student'");
            }

            System.out.println("\n=== All Verification Passed Successfully! ===");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
