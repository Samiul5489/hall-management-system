package com.hallmanagement;

import com.hallmanagement.dao.HallDAO;
import com.hallmanagement.model.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class HallInformationSmokeTest {

    private static int testsRun = 0;
    private static int testsPassed = 0;

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("          STARTING COMPREHENSIVE HALL INFORMATION SMOKE TEST SUITE              ");
        System.out.println("================================================================================");

        HallDAO hallDAO = new HallDAO();

        try {

            test("1. Detailed Infrastructure Schema Initialization & Seeding", () -> {
                hallDAO.ensureSchemaAndSeedData();
                HallSummaryStats stats = hallDAO.getHallSummaryStats(1);
                assertNotNull(stats, "Stats should not be null");
                assertTrue(stats.getTotalRooms() > 0, "Total rooms must be > 0");
                assertTrue(stats.getTotalSeats() > 0, "Total seats must be > 0");
            });

            test("2. Dynamic Live Rollup Calculation (AC, Fans, Lights, Washrooms, CCTV, Fire Safety)", () -> {
                HallSummaryStats stats = hallDAO.getHallSummaryStats(1);
                System.out.println("   [DEBUG] Hall 1 Stats -> Rooms: " + stats.getTotalRooms() +
                                   ", Seats: " + stats.getTotalSeats() +
                                   ", Occupied: " + stats.getOccupiedSeats() +
                                   ", Vacant: " + stats.getVacantSeats() +
                                   ", AC: " + stats.getTotalAc() +
                                   ", Fans: " + stats.getTotalFans() +
                                   ", Lights: " + stats.getTotalLights() +
                                   ", Washrooms: " + stats.getTotalWashrooms() +
                                   ", CCTV: " + stats.getTotalCctv() +
                                   ", Fire Safety: " + stats.getTotalFireSafety() +
                                   ", Pending Mnt: " + stats.getPendingMaintenance());

                assertTrue(stats.getTotalAc() >= 3, "Dynamic total AC rollup should be >= 3");
                assertTrue(stats.getTotalFans() >= 20, "Dynamic total Fans rollup should be >= 20");
                assertTrue(stats.getTotalLights() >= 40, "Dynamic total Lights rollup should be >= 40");
                assertTrue(stats.getTotalWashrooms() >= 4, "Total washrooms should be >= 4");
                assertTrue(stats.getTotalCctv() >= 4, "Total CCTV cameras should be >= 4");
                assertTrue(stats.getTotalFireSafety() >= 3, "Total Fire safety units should be >= 3");
            });

            test("3. Institutional Profile & Contact Metadata", () -> {
                HallDetail detail = hallDAO.getHallDetail(1);
                assertNotNull(detail, "HallDetail should not be null");
                assertEquals("HALL-01", detail.getHallCode(), "Hall code should match");
                assertNotNull(detail.getProvostName(), "Provost name should be present");
                assertNotNull(detail.getOfficeContact(), "Office contact should be present");
                assertNotNull(detail.getEmergencyContact(), "Emergency contact should be present");
                assertNotNull(detail.getEmail(), "Official email should be present");
            });

            test("4. Floor-wise Statistics & Fixtures", () -> {
                List<FloorInfo> floors = hallDAO.getFloorsByHallId(1);
                assertTrue(floors.size() >= 4, "Should have at least 4 floors");
                FloorInfo f1 = floors.get(0);
                assertEquals(1, f1.getFloorNumber(), "First floor should have number 1");
                assertTrue(f1.getTotalRooms() > 0, "Floor 1 should have rooms");
                assertTrue(f1.getWaterTaps() > 0, "Floor 1 should have water taps");
                assertTrue(f1.getEmergencyExits() > 0, "Floor 1 should have emergency exits");
            });

            test("5. Room-wise Full Profiles & Occupancy", () -> {
                List<RoomFullProfile> rooms = hallDAO.getRoomFullProfiles(1, null, null);
                assertFalse(rooms.isEmpty(), "Rooms list should not be empty");
                RoomFullProfile r1 = rooms.get(0);
                assertTrue(r1.getRoomNumber() > 0, "Room number must be valid");
                assertTrue(r1.getTotalSeats() == 4, "Room capacity should be 4");
                assertNotNull(r1.getRoomType(), "Room type should be present");
                assertNotNull(r1.getConditionStatus(), "Condition status should be present");
            });

            test("6. Single Room Detailed Profile with Occupants", () -> {
                List<RoomFullProfile> rooms = hallDAO.getRoomFullProfiles(1, null, null);
                int roomId = rooms.get(0).getRoomId();
                RoomFullProfile full = hallDAO.getRoomFullProfileById(roomId);
                assertNotNull(full, "Full room profile should not be null");
                assertNotNull(full.getOccupants(), "Occupants list should not be null");
                assertEquals(4, full.getOccupants().size(), "Room should have 4 seat records");
            });

            test("7. Dedicated Equipment Inventory (AC, CCTV, Network, Fire Safety)", () -> {
                List<EquipmentItem> acs = hallDAO.getEquipmentInventory(1, "AC");
                List<EquipmentItem> cctvs = hallDAO.getEquipmentInventory(1, "CCTV");
                List<EquipmentItem> nets = hallDAO.getEquipmentInventory(1, "NETWORK");
                List<EquipmentItem> fires = hallDAO.getEquipmentInventory(1, "FIRE_SAFETY");

                assertFalse(acs.isEmpty(), "AC inventory should have records");
                assertFalse(cctvs.isEmpty(), "CCTV inventory should have records");
                assertFalse(nets.isEmpty(), "Network inventory should have records");
                assertFalse(fires.isEmpty(), "Fire safety inventory should have records");

                EquipmentItem acItem = acs.get(0);
                assertNotNull(acItem.getItemCode(), "Item code should be present");
                assertNotNull(acItem.getBrandModel(), "Brand/Model should be present");
                assertEquals("WORKING", acItem.getConditionStatus(), "Condition status should be WORKING");
            });

            test("8. Maintenance Work Order Lifecycle", () -> {
                MaintenanceRecord rec = new MaintenanceRecord();
                rec.setHallId(1);
                rec.setLocation("Room 101");
                rec.setEquipmentCategory("Electrical");
                rec.setProblemDescription("Dimmer switch replacement test");
                rec.setReportedBy("TestRunner");
                rec.setPriority("HIGH");
                rec.setStatus("PENDING");
                rec.setEstimatedCost(350.0);
                rec.setReportDate(LocalDate.now());

                boolean created = hallDAO.createMaintenanceRecord(rec);
                assertTrue(created, "Maintenance record should be created");

                List<MaintenanceRecord> mnts = hallDAO.getMaintenanceRecords(1, "PENDING");
                assertFalse(mnts.isEmpty(), "Pending maintenance records should be found");
                MaintenanceRecord latest = mnts.get(0);

                boolean updated = hallDAO.updateMaintenanceStatus(latest.getId(), "COMPLETED", 320.0, "Replaced dimmer successfully");
                assertTrue(updated, "Maintenance status should be updated to COMPLETED");
            });

            test("9. Cleaning & Sanitation Schedules", () -> {
                List<CleaningSchedule> schedules = hallDAO.getCleaningSchedules(1);
                assertFalse(schedules.isEmpty(), "Cleaning schedules should exist");
                CleaningSchedule cs = schedules.get(0);
                assertNotNull(cs.getAreaType(), "Area type should be present");
                assertNotNull(cs.getFrequency(), "Frequency should be present");
                assertNotNull(cs.getResponsibleStaff(), "Staff crew should be assigned");
            });

            test("10. Hall Contacts Directory (Admin, Maintenance, Security, Emergency)", () -> {
                List<HallContact> contacts = hallDAO.getHallContacts(1);
                assertTrue(contacts.size() >= 5, "Should have multiple contact entries");
                boolean foundProvost = contacts.stream().anyMatch(c -> "Provost".equalsIgnoreCase(c.getDesignation()));
                boolean foundElectrician = contacts.stream().anyMatch(c -> c.getDesignation().toLowerCase().contains("electrician"));
                boolean foundEmergency = contacts.stream().anyMatch(c -> "EMERGENCY".equalsIgnoreCase(c.getCategory()));

                assertTrue(foundProvost, "Provost contact should exist");
                assertTrue(foundElectrician, "Electrician contact should exist");
                assertTrue(foundEmergency, "Emergency contact should exist");
            });

            test("11. Monthly Utility Finances Ledger", () -> {
                List<UtilityRecord> utils = hallDAO.getUtilityRecords(1);
                assertFalse(utils.isEmpty(), "Utility ledger should have records");
                UtilityRecord u = utils.get(0);
                assertTrue(u.getElectricityBill() > 0, "Electricity bill should be positive");
                assertTrue(u.getTotalExpense() > 0, "Total expense should be positive");
            });

            test("12. Fixed Asset Management & Category Filtering", () -> {
                AssetItem asset = new AssetItem();
                asset.setHallId(1);
                asset.setAssetCode("AST-TEST-" + System.currentTimeMillis() % 10000);
                asset.setAssetName("Smoke Test Asset Unit");
                asset.setCategory("SPORTS");
                asset.setQuantity(2);
                asset.setLocation("Indoor Games Room");
                asset.setPurchasePrice(15000.0);
                asset.setPurchaseDate(LocalDate.now());
                asset.setConditionStatus("GOOD");

                boolean created = hallDAO.createAssetItem(asset);
                assertTrue(created, "Asset should be inserted");

                List<AssetItem> sports = hallDAO.getHallAssets(1, "SPORTS");
                assertFalse(sports.isEmpty(), "Should find sports assets");
            });

            test("13. Water Management & Central Kitchen Profile", () -> {
                WaterSystemInfo water = hallDAO.getWaterSystemByHallId(1);
                assertNotNull(water, "Water system info should exist");
                assertTrue(water.getTotalTankCapacityLiters() > 0, "Tank capacity should be > 0");

                KitchenDiningInfo kd = hallDAO.getKitchenDiningByHallId(1);
                assertNotNull(kd, "Kitchen & Dining info should exist");
                assertTrue(kd.getDiningCapacity() > 0, "Dining capacity should be > 0");
                assertTrue(kd.getDiningTableCount() > 0, "Dining table count should be > 0");
            });

            test("14. Dynamic Room Mutation & Rollup Verification", () -> {
                List<RoomFullProfile> rooms = hallDAO.getRoomFullProfiles(1, null, null);
                int roomId = rooms.get(0).getRoomId();

                HallSummaryStats before = hallDAO.getHallSummaryStats(1);
                int initialAc = before.getTotalAc();

                hallDAO.updateRoomElectrical(roomId, 2, 2, 2, 2, 4);

                HallSummaryStats after = hallDAO.getHallSummaryStats(1);
                System.out.println("   [DEBUG] Rollup recalculation: Before AC=" + initialAc + ", After AC=" + after.getTotalAc());
                assertTrue(after.getTotalAc() >= initialAc, "Dynamic total AC should reflect room electrical update");
            });

        } catch (Exception e) {
            System.err.println("Fatal error during smoke tests: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("================================================================================");
        System.out.println("  SUMMARY: " + testsPassed + " / " + testsRun + " TESTS PASSED");
        System.out.println("================================================================================");

        if (testsPassed != testsRun) {
            System.exit(1);
        }
    }

    private static void test(String name, TestCase testCase) {
        testsRun++;
        System.out.print("[TEST " + testsRun + "] " + name + " ... ");
        try {
            testCase.run();
            testsPassed++;
            System.out.println("PASSED \u2705");
        } catch (Throwable t) {
            System.out.println("FAILED \u274C");
            System.err.println("   Error: " + t.getMessage());
            t.printStackTrace();
        }
    }

    private static void assertTrue(boolean condition, String msg) {
        if (!condition) throw new AssertionError("ASSERTION FAILED: " + msg);
    }

    private static void assertFalse(boolean condition, String msg) {
        if (condition) throw new AssertionError("ASSERTION FAILED: " + msg);
    }

    private static void assertEquals(Object expected, Object actual, String msg) {
        if (expected == null && actual == null) return;
        if (expected != null && expected.equals(actual)) return;
        throw new AssertionError("ASSERTION FAILED: " + msg + " (Expected: " + expected + ", Actual: " + actual + ")");
    }

    private static void assertNotNull(Object obj, String msg) {
        if (obj == null) throw new AssertionError("ASSERTION FAILED: " + msg + " (Object was null)");
    }

    @FunctionalInterface
    interface TestCase {
        void run() throws Exception;
    }
}
