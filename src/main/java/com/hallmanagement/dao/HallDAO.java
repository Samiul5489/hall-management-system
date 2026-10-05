package com.hallmanagement.dao;

import com.hallmanagement.database.DBConnection;
import com.hallmanagement.model.*;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HallDAO {

    public void ensureSchemaAndSeedData() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            boolean hallsExist = false;
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM halls")) {
                hallsExist = true;
            } catch (SQLException ignored) {
                hallsExist = false;
            }

            if (!hallsExist) {
                System.out.println("[HallDAO] Tables not found. Initializing schema from /sql/schema.sql...");
                executeSqlResource(conn, "/sql/schema.sql");
                return;
            }

            try { stmt.executeUpdate("ALTER TABLE halls ADD COLUMN provost_user_id VARCHAR(50) DEFAULT NULL"); } catch (SQLException ignored) {}
            try { stmt.executeUpdate("ALTER TABLE halls ADD COLUMN provost_name VARCHAR(100) DEFAULT NULL"); } catch (SQLException ignored) {}
            try { stmt.executeUpdate("ALTER TABLE halls ADD COLUMN provost_phone VARCHAR(20) DEFAULT NULL"); } catch (SQLException ignored) {}

            try {
                stmt.executeUpdate("UPDATE halls SET provost_user_id = 'provost001', provost_name = 'Prof. Dr. M. Rahman', provost_phone = '01711223344' WHERE id = 1 AND provost_name IS NULL");
                stmt.executeUpdate("UPDATE halls SET provost_user_id = 'provost002', provost_name = 'Prof. Dr. A. K. Azad', provost_phone = '01722334455' WHERE id = 2 AND provost_name IS NULL");
                stmt.executeUpdate("UPDATE halls SET provost_user_id = 'provost003', provost_name = 'Prof. Dr. M. S. Islam', provost_phone = '01733445566' WHERE id = 3 AND provost_name IS NULL");
                stmt.executeUpdate("UPDATE halls SET provost_user_id = 'provost001', provost_name = 'Prof. Dr. M. Rahman', provost_phone = '01711223344' WHERE id = 4 AND provost_name IS NULL");
                stmt.executeUpdate("UPDATE halls SET provost_user_id = 'provost004', provost_name = 'Prof. Dr. N. Sultana', provost_phone = '01744556677' WHERE id = 5 AND provost_name IS NULL");
            } catch (SQLException ignored) {}

            String[] dropFkQueries = new String[] {
                "ALTER TABLE students DROP FOREIGN KEY students_ibfk_1",
                "ALTER TABLE provosts DROP FOREIGN KEY provosts_ibfk_1",
                "ALTER TABLE bills DROP FOREIGN KEY bills_ibfk_1",
                "ALTER TABLE payment_requests DROP FOREIGN KEY payment_requests_ibfk_1",
                "ALTER TABLE payment_requests DROP FOREIGN KEY payment_requests_ibfk_2",
                "ALTER TABLE payments DROP FOREIGN KEY payments_ibfk_2",
                "ALTER TABLE payments DROP FOREIGN KEY payments_ibfk_3",
                "ALTER TABLE notices DROP FOREIGN KEY notices_ibfk_1",
                "ALTER TABLE student_notice_reads DROP FOREIGN KEY student_notice_reads_ibfk_1",
                "ALTER TABLE hall_change_requests DROP FOREIGN KEY hall_change_requests_ibfk_1",
                "ALTER TABLE hall_change_requests DROP FOREIGN KEY hall_change_requests_ibfk_4"
            };
            for (String q : dropFkQueries) {
                try { stmt.executeUpdate(q); } catch (SQLException ignored) {}
            }

            try { stmt.executeUpdate("ALTER TABLE students ADD COLUMN password VARCHAR(255) NOT NULL DEFAULT 'student123'"); } catch (SQLException ignored) {}
            try { stmt.executeUpdate("ALTER TABLE students ADD COLUMN phone VARCHAR(20) DEFAULT NULL"); } catch (SQLException ignored) {}
            try { stmt.executeUpdate("ALTER TABLE students ADD COLUMN status ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE'"); } catch (SQLException ignored) {}

            try { stmt.executeUpdate("ALTER TABLE provosts ADD COLUMN password VARCHAR(255) NOT NULL DEFAULT 'provost123'"); } catch (SQLException ignored) {}
            try { stmt.executeUpdate("ALTER TABLE provosts ADD COLUMN name VARCHAR(100) DEFAULT 'Provost'"); } catch (SQLException ignored) {}
            try { stmt.executeUpdate("ALTER TABLE provosts ADD COLUMN phone VARCHAR(20) DEFAULT '01711223344'"); } catch (SQLException ignored) {}
            try { stmt.executeUpdate("ALTER TABLE provosts ADD COLUMN status ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE'"); } catch (SQLException ignored) {}

            try {
                stmt.executeUpdate("UPDATE students s JOIN users u ON s.user_id = u.user_id SET s.password = u.password, s.phone = u.phone, s.status = u.status");
                stmt.executeUpdate("UPDATE provosts p JOIN users u ON p.user_id = u.user_id SET p.password = u.password, p.name = u.name, p.phone = u.phone, p.status = u.status");
                stmt.executeUpdate("DROP TABLE IF EXISTS users");
            } catch (SQLException ignored) {}

            try {
                stmt.executeUpdate("UPDATE students SET password = 'student123' WHERE user_id = '2403001'");
                stmt.executeUpdate("UPDATE students SET password = 'student456' WHERE user_id = '2403002'");
                stmt.executeUpdate("UPDATE students SET password = 'student789' WHERE user_id = '2403015'");
                stmt.executeUpdate("UPDATE students SET password = 'student321' WHERE user_id = '2403020'");
                stmt.executeUpdate("UPDATE provosts SET password = 'provost123' WHERE user_id IN ('provost001', 'provost002', 'provost003')");
            } catch (SQLException ignored) {}

            String createRooms = "CREATE TABLE IF NOT EXISTS rooms (" +
                    "    id INT UNSIGNED NOT NULL AUTO_INCREMENT," +
                    "    hall_id INT UNSIGNED NOT NULL," +
                    "    room_number INT NOT NULL," +
                    "    total_seats INT NOT NULL DEFAULT 4," +
                    "    PRIMARY KEY (id)," +
                    "    UNIQUE KEY uq_hall_room (hall_id, room_number)," +
                    "    FOREIGN KEY (hall_id) REFERENCES halls(id) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";
            stmt.executeUpdate(createRooms);

            String createSeats = "CREATE TABLE IF NOT EXISTS seats (" +
                    "    id INT UNSIGNED NOT NULL AUTO_INCREMENT," +
                    "    room_id INT UNSIGNED NOT NULL," +
                    "    seat_number INT NOT NULL," +
                    "    PRIMARY KEY (id)," +
                    "    UNIQUE KEY uq_room_seat (room_id, seat_number)," +
                    "    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";
            stmt.executeUpdate(createSeats);

            try {
                stmt.executeUpdate("ALTER TABLE students ADD UNIQUE KEY uq_active_seat (current_hall_id, current_room, current_seat)");
            } catch (SQLException ignored) {

            }

            try {
                stmt.executeUpdate("UPDATE students SET current_seat = 1 WHERE current_seat = 12 AND current_room = 305");
                stmt.executeUpdate("UPDATE students SET current_seat = 2 WHERE current_seat = 15 AND current_room = 204");
            } catch (SQLException ignored) {}

            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM rooms")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    seedRoomsAndSeats(conn);
                }
            }

            ensureDetailedHallSchemaAndSeed(conn);
        } catch (SQLException e) {
            System.err.println("[HallDAO] Error ensuring schema/seed: " + e.getMessage());
        }
    }

    private void executeSqlResource(Connection conn, String resourcePath) {
        try (java.io.InputStream in = getClass().getResourceAsStream(resourcePath);
             java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8));
             Statement stmt = conn.createStatement()) {

            if (in == null) {
                System.err.println("[HallDAO] Resource not found: " + resourcePath);
                return;
            }

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.startsWith("--") || trimmed.startsWith("/*") || trimmed.isEmpty()) {
                    continue;
                }
                sb.append(line).append("\n");
                if (trimmed.endsWith(";")) {
                    String sql = sb.toString().trim();
                    if (sql.endsWith(";")) {
                        sql = sql.substring(0, sql.length() - 1);
                    }
                    if (!sql.isEmpty() && !sql.toUpperCase().startsWith("CREATE DATABASE") && !sql.toUpperCase().startsWith("USE ")) {
                        try {
                            stmt.execute(sql);
                        } catch (SQLException ex) {
                            System.err.println("[HallDAO] Error executing SQL: " + ex.getMessage());
                        }
                    }
                    sb.setLength(0);
                }
            }
        } catch (Exception e) {
            System.err.println("[HallDAO] Failed to execute SQL resource: " + e.getMessage());
        }
    }

    private void seedRoomsAndSeats(Connection conn) throws SQLException {

        List<Integer> hallIds = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id FROM halls ORDER BY id")) {
            while (rs.next()) {
                hallIds.add(rs.getInt("id"));
            }
        }

        int[] sampleRooms = {101, 102, 103, 104, 201, 202, 203, 204, 301, 302, 303, 304, 305};
        String insertRoom = "INSERT IGNORE INTO rooms (hall_id, room_number, total_seats) VALUES (?, ?, 4)";
        String insertSeats = "INSERT IGNORE INTO seats (room_id, seat_number) SELECT ?, n FROM (SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4) nums";

        try (PreparedStatement pstRoom = conn.prepareStatement(insertRoom, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement pstSeat = conn.prepareStatement(insertSeats)) {

            for (int hallId : hallIds) {
                for (int roomNum : sampleRooms) {
                    pstRoom.setInt(1, hallId);
                    pstRoom.setInt(2, roomNum);
                    pstRoom.executeUpdate();

                    try (ResultSet keys = pstRoom.getGeneratedKeys()) {
                        if (keys.next()) {
                            int roomId = keys.getInt(1);
                            pstSeat.setInt(1, roomId);
                            pstSeat.executeUpdate();
                        }
                    }
                }
            }
        }
    }

    public List<Hall> getAllHalls() throws SQLException {
        List<Hall> halls = new ArrayList<>();
        String sql = "SELECT id, hall_name, hall_type FROM halls ORDER BY id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                halls.add(new Hall(
                    rs.getInt("id"),
                    rs.getString("hall_name"),
                    rs.getString("hall_type")
                ));
            }
        }
        return halls;
    }

    public List<HallInfo> getAllHallsWithStats() throws SQLException {
        ensureSchemaAndSeedData();
        List<HallInfo> hallList = new ArrayList<>();

        String sql =
            "SELECT " +
            "    h.id AS hall_id, " +
            "    h.hall_name, " +
            "    h.hall_type, " +
            "    h.provost_user_id, " +
            "    h.provost_name, " +
            "    h.provost_phone, " +
            "    COUNT(DISTINCT r.id) AS total_rooms, " +
            "    COUNT(s.id) AS total_seats, " +
            "    COUNT(st.id) AS occupied_seats, " +
            "    (COUNT(s.id) - COUNT(st.id)) AS available_seats " +
            "FROM halls h " +
            "LEFT JOIN rooms r ON r.hall_id = h.id " +
            "LEFT JOIN seats s ON s.room_id = r.id " +
            "LEFT JOIN students st ON st.current_hall_id = h.id " +
            "                     AND st.current_room = r.room_number " +
            "                     AND st.current_seat = s.seat_number " +
            "GROUP BY h.id, h.hall_name, h.hall_type, h.provost_user_id, h.provost_name, h.provost_phone " +
            "ORDER BY h.id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                hallList.add(new HallInfo(
                    rs.getInt("hall_id"),
                    rs.getString("hall_name"),
                    rs.getString("hall_type"),
                    rs.getInt("total_rooms"),
                    rs.getInt("total_seats"),
                    rs.getInt("occupied_seats"),
                    rs.getInt("available_seats"),
                    rs.getString("provost_user_id"),
                    rs.getString("provost_name"),
                    rs.getString("provost_phone")
                ));
            }
        }
        return hallList;
    }

    public List<HallInfo> getAllHallsInfo() throws SQLException {
        return getAllHallsWithStats();
    }

    public HallInfo getHallById(int hallId) throws SQLException {
        ensureSchemaAndSeedData();
        String sql =
            "SELECT " +
            "    h.id AS hall_id, " +
            "    h.hall_name, " +
            "    h.hall_type, " +
            "    h.provost_user_id, " +
            "    h.provost_name, " +
            "    h.provost_phone, " +
            "    COUNT(DISTINCT r.id) AS total_rooms, " +
            "    COUNT(s.id) AS total_seats, " +
            "    COUNT(st.id) AS occupied_seats, " +
            "    (COUNT(s.id) - COUNT(st.id)) AS available_seats " +
            "FROM halls h " +
            "LEFT JOIN rooms r ON r.hall_id = h.id " +
            "LEFT JOIN seats s ON s.room_id = r.id " +
            "LEFT JOIN students st ON st.current_hall_id = h.id " +
            "                     AND st.current_room = r.room_number " +
            "                     AND st.current_seat = s.seat_number " +
            "WHERE h.id = ? " +
            "GROUP BY h.id, h.hall_name, h.hall_type, h.provost_user_id, h.provost_name, h.provost_phone";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hallId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new HallInfo(
                        rs.getInt("hall_id"),
                        rs.getString("hall_name"),
                        rs.getString("hall_type"),
                        rs.getInt("total_rooms"),
                        rs.getInt("total_seats"),
                        rs.getInt("occupied_seats"),
                        rs.getInt("available_seats"),
                        rs.getString("provost_user_id"),
                        rs.getString("provost_name"),
                        rs.getString("provost_phone")
                    );
                }
            }
        }
        return null;
    }

    public List<RoomInfo> getRoomsByHallId(int hallId) throws SQLException {
        ensureSchemaAndSeedData();
        List<RoomInfo> rooms = new ArrayList<>();

        String sql =
            "SELECT " +
            "    r.id AS room_id, " +
            "    r.hall_id, " +
            "    h.hall_name, " +
            "    r.room_number, " +
            "    COUNT(s.id) AS total_seats, " +
            "    COUNT(st.id) AS occupied_seats, " +
            "    (COUNT(s.id) - COUNT(st.id)) AS available_seats " +
            "FROM rooms r " +
            "JOIN halls h ON r.hall_id = h.id " +
            "LEFT JOIN seats s ON s.room_id = r.id " +
            "LEFT JOIN students st ON st.current_hall_id = h.id " +
            "                     AND st.current_room = r.room_number " +
            "                     AND st.current_seat = s.seat_number " +
            "WHERE r.hall_id = ? " +
            "GROUP BY r.id, r.hall_id, h.hall_name, r.room_number " +
            "ORDER BY r.room_number ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, hallId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rooms.add(new RoomInfo(
                        rs.getInt("room_id"),
                        rs.getInt("hall_id"),
                        rs.getString("hall_name"),
                        rs.getInt("room_number"),
                        rs.getInt("total_seats"),
                        rs.getInt("occupied_seats"),
                        rs.getInt("available_seats")
                    ));
                }
            }
        }
        return rooms;
    }

    public List<SeatInfo> getSeatsByRoomId(int roomId) throws SQLException {
        ensureSchemaAndSeedData();
        List<SeatInfo> seats = new ArrayList<>();

        String sql =
            "SELECT " +
            "    s.id AS seat_id, " +
            "    s.seat_number, " +
            "    r.id AS room_id, " +
            "    r.room_number, " +
            "    h.id AS hall_id, " +
            "    h.hall_name, " +
            "    st.id AS student_db_id, " +
            "    st.user_id AS student_user_id, " +
            "    st.name AS student_name, " +
            "    st.department AS student_dept, " +
            "    st.year AS student_year " +
            "FROM seats s " +
            "JOIN rooms r ON s.room_id = r.id " +
            "JOIN halls h ON r.hall_id = h.id " +
            "LEFT JOIN students st ON st.current_hall_id = h.id " +
            "                     AND st.current_room = r.room_number " +
            "                     AND st.current_seat = s.seat_number " +
            "WHERE r.id = ? " +
            "ORDER BY s.seat_number ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    boolean isOccupied = rs.getObject("student_db_id") != null;
                    String sUserId = rs.getString("student_user_id");
                    seats.add(new SeatInfo(
                        rs.getInt("seat_id"),
                        rs.getInt("room_id"),
                        rs.getInt("room_number"),
                        rs.getInt("hall_id"),
                        rs.getString("hall_name"),
                        rs.getInt("seat_number"),
                        isOccupied,
                        sUserId,
                        rs.getString("student_name"),
                        sUserId,
                        rs.getString("student_dept"),
                        rs.getString("student_year")
                    ));
                }
            }
        }
        return seats;
    }

    public boolean isSeatOccupied(int hallId, int roomNumber, int seatNumber) throws SQLException {
        String sql = "SELECT COUNT(*) FROM students WHERE current_hall_id = ? AND current_room = ? AND current_seat = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, hallId);
            ps.setInt(2, roomNumber);
            ps.setInt(3, seatNumber);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public HallInfo getHallInfoById(int hallId) throws SQLException {
        ensureSchemaAndSeedData();
        String sql =
            "SELECT " +
            "    h.id AS hall_id, " +
            "    h.hall_name, " +
            "    h.hall_type, " +
            "    COUNT(DISTINCT r.id) AS total_rooms, " +
            "    COUNT(s.id) AS total_seats, " +
            "    COUNT(st.id) AS occupied_seats, " +
            "    (COUNT(s.id) - COUNT(st.id)) AS available_seats " +
            "FROM halls h " +
            "LEFT JOIN rooms r ON r.hall_id = h.id " +
            "LEFT JOIN seats s ON s.room_id = r.id " +
            "LEFT JOIN students st ON st.current_hall_id = h.id " +
            "                     AND st.current_room = r.room_number " +
            "                     AND st.current_seat = s.seat_number " +
            "WHERE h.id = ? " +
            "GROUP BY h.id, h.hall_name, h.hall_type";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, hallId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new HallInfo(
                        rs.getInt("hall_id"),
                        rs.getString("hall_name"),
                        rs.getString("hall_type"),
                        rs.getInt("total_rooms"),
                        rs.getInt("total_seats"),
                        rs.getInt("occupied_seats"),
                        rs.getInt("available_seats")
                    );
                }
            }
        }
        return null;
    }

    public Hall getHallByProvostUserId(String provostUserId) throws SQLException {
        ensureSchemaAndSeedData();
        String sql = "SELECT h.* FROM halls h JOIN provosts p ON p.hall_id = h.id WHERE p.user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, provostUserId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Hall(rs.getInt("id"), rs.getString("hall_name"), rs.getString("hall_type"));
                }
            }
        }
        return null;
    }

    private void ensureDetailedHallSchemaAndSeed(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            boolean detailsTableExists = false;
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM hall_details")) {
                detailsTableExists = true;
            } catch (SQLException ignored) {
                detailsTableExists = false;
            }

            if (!detailsTableExists) {
                System.out.println("[HallDAO] Initializing detailed infrastructure schema...");
                executeSqlResource(conn, "/sql/hall_information_schema.sql");
            }

            boolean needsSeeding = false;
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM hall_equipment_inventory")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    needsSeeding = true;
                }
            } catch (SQLException ignored) {
                needsSeeding = true;
            }

            if (needsSeeding) {
                seedDetailedHallData(conn);
            }
        } catch (SQLException e) {
            System.err.println("[HallDAO] Error ensuring detailed schema: " + e.getMessage());
        }
    }

    private void seedDetailedHallData(Connection conn) throws SQLException {
        List<Integer> hallIds = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id FROM halls ORDER BY id")) {
            while (rs.next()) {
                hallIds.add(rs.getInt("id"));
            }
        }

        String insertDetail = "INSERT IGNORE INTO hall_details (hall_id, hall_code, established_year, address, description, total_floors, total_staff, non_residential_students, assistant_provosts, office_contact, emergency_contact, email) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String insertBuilding = "INSERT IGNORE INTO hall_buildings (hall_id, building_name, floors_count, rooms_per_floor, total_rooms, residential_rooms, guest_rooms, other_facilities) VALUES (?, ?, 4, 20, 80, 74, 2, 'Table Tennis Room, Gymnasium, Generator Substation, Wi-Fi Zone')";
        String insertWater = "INSERT IGNORE INTO hall_water_system (hall_id, source_type, deep_tube_well, pump_count, tank_count, total_tank_capacity_liters, filters_count, purifiers_count, supply_schedule, drinking_water_points, tap_count, pump_condition, tank_condition) VALUES (?, 'Deep Tube Well + Municipal Supply', '1 x 10HP Submersible Well', 2, 4, 20000, 8, 4, '24/7 Automated Sensor Supply', 6, 64, 'WORKING', 'EXCELLENT')";
        String insertKd = "INSERT IGNORE INTO hall_kitchen_dining (hall_id, kitchen_size_sqft, cooking_area_desc, stove_count, gas_burner_count, refrigerator_count, freezer_count, water_filter_count, sink_count, exhaust_fan_count, kitchen_staff_count, dining_capacity, dining_table_count, dining_chair_count, dining_fans, dining_lights, dining_ac, wash_basin_count) VALUES (?, 650.00, 'Central Industrial Cooking Block with S/S Countertops', 4, 8, 2, 2, 2, 4, 4, 8, 200, 35, 210, 16, 24, 2, 10)";

        try (PreparedStatement psDetail = conn.prepareStatement(insertDetail);
             PreparedStatement psBuilding = conn.prepareStatement(insertBuilding);
             PreparedStatement psWater = conn.prepareStatement(insertWater);
             PreparedStatement psKd = conn.prepareStatement(insertKd)) {

            for (int hId : hallIds) {
                String code = "HALL-0" + hId;
                int est = 1970 + (hId * 10);
                String desc = "Premier residential hall of Rajshahi University of Engineering & Technology (RUET), fostering academic excellence, cultural activities, and student welfare.";

                psDetail.setInt(1, hId);
                psDetail.setString(2, code);
                psDetail.setInt(3, est);
                psDetail.setString(4, "RUET Campus, Kazla, Rajshahi-6204");
                psDetail.setString(5, desc);
                psDetail.setInt(6, 4);
                psDetail.setInt(7, 25);
                psDetail.setInt(8, 120 + (hId * 15));
                psDetail.setString(9, "Dr. M. S. Rahman, Dr. K. Ahmed");
                psDetail.setString(10, "0258886000" + hId);
                psDetail.setString(11, "0171100000" + hId);
                psDetail.setString(12, "hall" + hId + ".office@ruet.ac.bd");
                psDetail.executeUpdate();

                psBuilding.setInt(1, hId);
                psBuilding.setString(2, "Main Residential Complex - Bhaban " + hId);
                psBuilding.executeUpdate();

                psWater.setInt(1, hId);
                psWater.executeUpdate();

                psKd.setInt(1, hId);
                psKd.executeUpdate();
            }
        }

        String insertFloor = "INSERT IGNORE INTO hall_floors (hall_id, floor_number, floor_name, total_rooms, washrooms_count, toilets_count, water_taps, emergency_exits, windows_count, doors_count) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement psFloor = conn.prepareStatement(insertFloor)) {
            for (int hId : hallIds) {
                for (int f = 1; f <= 4; f++) {
                    psFloor.setInt(1, hId);
                    psFloor.setInt(2, f);
                    psFloor.setString(3, "Floor " + f + (f == 1 ? " (Ground Floor)" : ""));
                    psFloor.setInt(4, 20);
                    psFloor.setInt(5, 4);
                    psFloor.setInt(6, 12);
                    psFloor.setInt(7, 16);
                    psFloor.setInt(8, 2);
                    psFloor.setInt(9, 40);
                    psFloor.setInt(10, 22);
                    psFloor.executeUpdate();
                }
            }
        }

        String insertRoomDet = "INSERT IGNORE INTO room_details (room_id, room_type, floor_number, room_size_sqft, window_count, door_count, has_balcony, has_attached_bath, condition_status) VALUES (?, ?, ?, 240.00, 2, 1, 1, ?, 'GOOD')";
        String insertRoomElec = "INSERT IGNORE INTO room_electrical (room_id, ac_count, ceiling_fan_count, wall_fan_count, exhaust_fan_count, tube_light_count, led_light_count, bulb_count, emergency_light_count, night_light_count, switch_count, socket_count, power_outlet_count, other_equipment) VALUES (?, ?, 2, 0, ?, 2, 2, 0, 1, 1, 6, 4, 2, 'Wall Clock')";
        String insertRoomFurn = "INSERT IGNORE INTO room_furniture (room_id, bed_count, table_count, chair_count, wardrobe_count, bookshelf_count, mattress_count, mirror_count, curtain_count, dustbin_count, good_condition_count, damaged_count, under_repair_count, missing_count) VALUES (?, 4, 4, 4, 4, 2, 4, 1, 2, 1, 21, 0, 0, 0)";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, room_number FROM rooms");
             PreparedStatement psRDet = conn.prepareStatement(insertRoomDet);
             PreparedStatement psRElec = conn.prepareStatement(insertRoomElec);
             PreparedStatement psRFurn = conn.prepareStatement(insertRoomFurn)) {

            while (rs.next()) {
                int rId = rs.getInt("id");
                int rNum = rs.getInt("room_number");
                int floor = rNum / 100;
                if (floor == 0) floor = 1;
                boolean isSpecial = (rNum % 4 == 0);
                String rType = isSpecial ? "ATTACHED_BATH" : (rNum % 3 == 0 ? "PREMIUM" : "STANDARD");

                psRDet.setInt(1, rId);
                psRDet.setString(2, rType);
                psRDet.setInt(3, floor);
                psRDet.setInt(4, isSpecial ? 1 : 0);
                psRDet.executeUpdate();

                psRElec.setInt(1, rId);
                psRElec.setInt(2, isSpecial ? 1 : 0);
                psRElec.setInt(3, isSpecial ? 1 : 0);
                psRElec.executeUpdate();

                psRFurn.setInt(1, rId);
                psRFurn.executeUpdate();
            }
        }

        String insertCommon = "INSERT IGNORE INTO hall_common_areas (hall_id, area_name, capacity, ac_count, fan_count, light_count, table_count, chair_count, condition_status, responsible_staff, remarks) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'GOOD', ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertCommon)) {
            for (int hId : hallIds) {
                ps.setInt(1, hId); ps.setString(2, "Central Dining Hall"); ps.setInt(3, 200); ps.setInt(4, 2); ps.setInt(5, 16); ps.setInt(6, 24); ps.setInt(7, 35); ps.setInt(8, 210); ps.setString(9, "Dining Supervisor"); ps.setString(10, "Breakfast, Lunch, Dinner"); ps.executeUpdate();
                ps.setInt(1, hId); ps.setString(2, "Main Reading Room"); ps.setInt(3, 60); ps.setInt(4, 1); ps.setInt(5, 6); ps.setInt(6, 12); ps.setInt(7, 10); ps.setInt(8, 60); ps.setString(9, "Library Assistant"); ps.setString(10, "Open 24/7 with high-speed Wi-Fi"); ps.executeUpdate();
                ps.setInt(1, hId); ps.setString(2, "Prayer Hall (Mosque)"); ps.setInt(3, 80); ps.setInt(4, 0); ps.setInt(5, 8); ps.setInt(6, 10); ps.setInt(7, 2); ps.setInt(8, 0); ps.setString(9, "Pesh Imam"); ps.setString(10, "Daily 5 prayers and Jummah"); ps.executeUpdate();
                ps.setInt(1, hId); ps.setString(2, "TV & Recreation Room"); ps.setInt(3, 50); ps.setInt(4, 1); ps.setInt(5, 4); ps.setInt(6, 8); ps.setInt(7, 4); ps.setInt(8, 40); ps.setString(9, "Recreation Caretaker"); ps.setString(10, "Satellite Cable TV & Carrom"); ps.executeUpdate();
                ps.setInt(1, hId); ps.setString(2, "Indoor Gymnasium"); ps.setInt(3, 30); ps.setInt(4, 0); ps.setInt(5, 4); ps.setInt(6, 6); ps.setInt(7, 2); ps.setInt(8, 10); ps.setString(9, "Gym Instructor"); ps.setString(10, "Free weights & cardio stations"); ps.executeUpdate();
                ps.setInt(1, hId); ps.setString(2, "Medical First-Aid Room"); ps.setInt(3, 10); ps.setInt(4, 1); ps.setInt(5, 2); ps.setInt(6, 4); ps.setInt(7, 2); ps.setInt(8, 6); ps.setString(9, "Medical Assistant"); ps.setString(10, "Emergency first aid & BP monitor"); ps.executeUpdate();
                ps.setInt(1, hId); ps.setString(2, "IT & Computer Lab"); ps.setInt(3, 40); ps.setInt(4, 2); ps.setInt(5, 6); ps.setInt(6, 12); ps.setInt(7, 20); ps.setInt(8, 40); ps.setString(9, "Network Admin"); ps.setString(10, "Gigabit LAN & Color Laser Printer"); ps.executeUpdate();
            }
        }

        String insertEquip = "INSERT IGNORE INTO hall_equipment_inventory (hall_id, item_code, category, equipment_type, location_type, room_number, floor_number, specific_location, brand_model, capacity_rating, condition_status, remarks) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'WORKING', ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertEquip)) {
            for (int hId : hallIds) {

                ps.setInt(1, hId); ps.setString(2, "AC-OFFICE-" + hId); ps.setString(3, "AC"); ps.setString(4, "Split AC (Inverter)"); ps.setString(5, "OFFICE"); ps.setNull(6, java.sql.Types.INTEGER); ps.setInt(7, 1); ps.setString(8, "Provost Office"); ps.setString(9, "Gree Fairy Series"); ps.setString(10, "2.0 Ton"); ps.setString(11, "Regular servicing on schedule"); ps.executeUpdate();
                ps.setInt(1, hId); ps.setString(2, "AC-READING-" + hId); ps.setString(3, "AC"); ps.setString(4, "Split AC"); ps.setString(5, "COMMON_AREA"); ps.setNull(6, java.sql.Types.INTEGER); ps.setInt(7, 2); ps.setString(8, "Reading Room"); ps.setString(9, "General Eco"); ps.setString(10, "2.0 Ton"); ps.setString(11, "High usage area"); ps.executeUpdate();
                ps.setInt(1, hId); ps.setString(2, "AC-DINING1-" + hId); ps.setString(3, "AC"); ps.setString(4, "Cassette AC"); ps.setString(5, "COMMON_AREA"); ps.setNull(6, java.sql.Types.INTEGER); ps.setInt(7, 1); ps.setString(8, "Dining Hall East"); ps.setString(9, "Carrier Inverter"); ps.setString(10, "3.0 Ton"); ps.setString(11, "Operational during meal hours"); ps.executeUpdate();

                ps.setInt(1, hId); ps.setString(2, "CAM-MAIN-" + hId); ps.setString(3, "CCTV"); ps.setString(4, "Bullet IP Camera"); ps.setString(5, "OUTDOOR"); ps.setNull(6, java.sql.Types.INTEGER); ps.setInt(7, 1); ps.setString(8, "Main Hall Gate"); ps.setString(9, "Hikvision 4MP IP"); ps.setString(10, "1080p Night Vision"); ps.setString(11, "24/7 Live Monitoring"); ps.executeUpdate();
                ps.setInt(1, hId); ps.setString(2, "CAM-CORR1-" + hId); ps.setString(3, "CCTV"); ps.setString(4, "Dome IP Camera"); ps.setString(5, "FLOOR_CORRIDOR"); ps.setNull(6, java.sql.Types.INTEGER); ps.setInt(7, 1); ps.setString(8, "1st Floor Central Corridor"); ps.setString(9, "Dahua 2MP"); ps.setString(10, "1080p"); ps.setString(11, "Operational"); ps.executeUpdate();
                ps.setInt(1, hId); ps.setString(2, "CAM-CORR2-" + hId); ps.setString(3, "CCTV"); ps.setString(4, "Dome IP Camera"); ps.setString(5, "FLOOR_CORRIDOR"); ps.setNull(6, java.sql.Types.INTEGER); ps.setInt(7, 2); ps.setString(8, "2nd Floor Central Corridor"); ps.setString(9, "Dahua 2MP"); ps.setString(10, "1080p"); ps.setString(11, "Operational"); ps.executeUpdate();
                ps.setInt(1, hId); ps.setString(2, "CAM-DINING-" + hId); ps.setString(3, "CCTV"); ps.setString(4, "Dome IP Camera"); ps.setString(5, "COMMON_AREA"); ps.setNull(6, java.sql.Types.INTEGER); ps.setInt(7, 1); ps.setString(8, "Dining Hall"); ps.setString(9, "Hikvision 2MP"); ps.setString(10, "1080p"); ps.setString(11, "Operational"); ps.executeUpdate();

                ps.setInt(1, hId); ps.setString(2, "FE-F1-" + hId); ps.setString(3, "FIRE_SAFETY"); ps.setString(4, "ABC Dry Powder"); ps.setString(5, "FLOOR_CORRIDOR"); ps.setNull(6, java.sql.Types.INTEGER); ps.setInt(7, 1); ps.setString(8, "1st Floor Staircase A"); ps.setString(9, "Safex Fire"); ps.setString(10, "5 Kg"); ps.setString(11, "Inspected and Certified"); ps.executeUpdate();
                ps.setInt(1, hId); ps.setString(2, "FE-F2-" + hId); ps.setString(3, "FIRE_SAFETY"); ps.setString(4, "ABC Dry Powder"); ps.setString(5, "FLOOR_CORRIDOR"); ps.setNull(6, java.sql.Types.INTEGER); ps.setInt(7, 2); ps.setString(8, "2nd Floor Staircase A"); ps.setString(9, "Safex Fire"); ps.setString(10, "5 Kg"); ps.setString(11, "Inspected and Certified"); ps.executeUpdate();
                ps.setInt(1, hId); ps.setString(2, "FE-KITCHEN-" + hId); ps.setString(3, "FIRE_SAFETY"); ps.setString(4, "CO2 Extinguisher"); ps.setString(5, "COMMON_AREA"); ps.setNull(6, java.sql.Types.INTEGER); ps.setInt(7, 1); ps.setString(8, "Kitchen Entrance"); ps.setString(9, "Safex Fire"); ps.setString(10, "3 Kg CO2"); ps.setString(11, "Inspected and Certified"); ps.executeUpdate();

                ps.setInt(1, hId); ps.setString(2, "NET-RTR-" + hId); ps.setString(3, "NETWORK"); ps.setString(4, "Core Router"); ps.setString(5, "OFFICE"); ps.setNull(6, java.sql.Types.INTEGER); ps.setInt(7, 1); ps.setString(8, "Server Rack, Hall Office"); ps.setString(9, "Cisco 2901"); ps.setString(10, "1 Gbps Uplink"); ps.setString(11, "RUET Optical Fiber Backbone"); ps.executeUpdate();
                ps.setInt(1, hId); ps.setString(2, "NET-AP-F1-" + hId); ps.setString(3, "NETWORK"); ps.setString(4, "Wi-Fi Access Point"); ps.setString(5, "FLOOR_CORRIDOR"); ps.setNull(6, java.sql.Types.INTEGER); ps.setInt(7, 1); ps.setString(8, "Floor 1 Corridor"); ps.setString(9, "UniFi 6 Long-Range"); ps.setString(10, "Wi-Fi 6 (3 Gbps)"); ps.setString(11, "Active mesh node"); ps.executeUpdate();
                ps.setInt(1, hId); ps.setString(2, "NET-AP-F2-" + hId); ps.setString(3, "NETWORK"); ps.setString(4, "Wi-Fi Access Point"); ps.setString(5, "FLOOR_CORRIDOR"); ps.setNull(6, java.sql.Types.INTEGER); ps.setInt(7, 2); ps.setString(8, "Floor 2 Corridor"); ps.setString(9, "UniFi 6 Long-Range"); ps.setString(10, "Wi-Fi 6 (3 Gbps)"); ps.setString(11, "Active mesh node"); ps.executeUpdate();
            }
        }

        String insertMnt = "INSERT IGNORE INTO hall_maintenance_records (hall_id, record_code, location, room_number, equipment_category, problem_description, reported_by, report_date, assigned_person, priority, status, estimated_cost, actual_cost, completion_date, remarks) VALUES (?, ?, ?, ?, ?, ?, ?, '2026-09-01', ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertMnt)) {
            for (int hId : hallIds) {
                ps.setInt(1, hId); ps.setString(2, "MNT-" + hId + "-001"); ps.setString(3, "Room 203"); ps.setInt(4, 203); ps.setString(5, "Electrical"); ps.setString(6, "Ceiling fan regulator sparking and speed not changing."); ps.setString(7, "Student Representative"); ps.setString(8, "Md. Al-Amin (Electrician)"); ps.setString(9, "MEDIUM"); ps.setString(10, "COMPLETED"); ps.setDouble(11, 400.0); ps.setDouble(12, 380.0); ps.setDate(13, Date.valueOf("2026-09-02")); ps.setString(14, "New dimmer regulator installed."); ps.executeUpdate();
                ps.setInt(1, hId); ps.setString(2, "MNT-" + hId + "-002"); ps.setString(3, "Floor 2 Washroom"); ps.setNull(4, java.sql.Types.INTEGER); ps.setString(5, "Plumbing"); ps.setString(6, "Water tap leakage in wash basin #2."); ps.setString(7, "Floor Monitor"); ps.setString(8, "Md. Jahangir (Plumber)"); ps.setString(9, "HIGH"); ps.setString(10, "IN_PROGRESS"); ps.setDouble(11, 250.0); ps.setDouble(12, 0.0); ps.setNull(13, java.sql.Types.DATE); ps.setString(14, "Parts procured, replacing valve."); ps.executeUpdate();
                ps.setInt(1, hId); ps.setString(2, "MNT-" + hId + "-003"); ps.setString(3, "Dining Hall"); ps.setNull(4, java.sql.Types.INTEGER); ps.setString(5, "Lighting"); ps.setString(6, "Two LED tube lights flickering near counter."); ps.setString(7, "Dining Manager"); ps.setString(8, "Md. Al-Amin (Electrician)"); ps.setString(9, "LOW"); ps.setString(10, "PENDING"); ps.setDouble(11, 600.0); ps.setDouble(12, 0.0); ps.setNull(13, java.sql.Types.DATE); ps.setString(14, "Scheduled for weekly maintenance."); ps.executeUpdate();
            }
        }

        String insertClean = "INSERT IGNORE INTO hall_cleaning_schedules (hall_id, area_type, frequency, responsible_staff, status, last_cleaned_date, next_scheduled_date, remarks) VALUES (?, ?, ?, ?, 'COMPLETED', '2026-09-02', '2026-09-03', 'Sanitized and inspected')";
        try (PreparedStatement ps = conn.prepareStatement(insertClean)) {
            for (int hId : hallIds) {
                ps.setInt(1, hId); ps.setString(2, "Floors & Corridors"); ps.setString(3, "DAILY"); ps.setString(4, "Sanitation Team A (4 Staff)"); ps.executeUpdate();
                ps.setInt(1, hId); ps.setString(2, "Washrooms & Toilets"); ps.setString(3, "DAILY"); ps.setString(4, "Sanitation Team B (4 Staff)"); ps.executeUpdate();
                ps.setInt(1, hId); ps.setString(2, "Dining Hall & Kitchen"); ps.setString(3, "DAILY"); ps.setString(4, "Kitchen Hygiene Crew"); ps.executeUpdate();
                ps.setInt(1, hId); ps.setString(2, "Water Tanks & Filters"); ps.setString(3, "WEEKLY"); ps.setString(4, "Plumbing & Quality Cell"); ps.executeUpdate();
                ps.setInt(1, hId); ps.setString(2, "Campus Surroundings & Drains"); ps.setString(3, "MONTHLY"); ps.setString(4, "External Municipal Team"); ps.executeUpdate();
            }
        }

        String insertContact = "INSERT IGNORE INTO hall_contacts (hall_id, category, designation, name, phone, email, availability) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertContact)) {
            for (int hId : hallIds) {
                ps.setInt(1, hId); ps.setString(2, "ADMIN"); ps.setString(3, "Provost"); ps.setString(4, "Prof. Dr. M. Rahman"); ps.setString(5, "01711223344"); ps.setString(6, "provost.hall" + hId + "@ruet.ac.bd"); ps.setString(7, "10:00 AM - 2:00 PM"); ps.executeUpdate();
                ps.setInt(1, hId); ps.setString(2, "ADMIN"); ps.setString(3, "Assistant Provost"); ps.setString(4, "Dr. K. Ahmed"); ps.setString(5, "01711334455"); ps.setString(6, "ahmed.k@ruet.ac.bd"); ps.setString(7, "On-Call / Evening"); ps.executeUpdate();
                ps.setInt(1, hId); ps.setString(2, "OFFICE"); ps.setString(3, "Senior Administrative Officer"); ps.setString(4, "Md. Shafiul Alam"); ps.setString(5, "0258886111" + hId); ps.setString(6, "shafiul@ruet.ac.bd"); ps.setString(7, "9:00 AM - 5:00 PM"); ps.executeUpdate();
                ps.setInt(1, hId); ps.setString(2, "MAINTENANCE"); ps.setString(3, "Hall Electrician"); ps.setString(4, "Md. Al-Amin"); ps.setString(5, "01711998877"); ps.setString(6, null); ps.setString(7, "24/7 On-Call"); ps.executeUpdate();
                ps.setInt(1, hId); ps.setString(2, "MAINTENANCE"); ps.setString(3, "Hall Plumber"); ps.setString(4, "Md. Jahangir"); ps.setString(5, "01711887766"); ps.setString(6, null); ps.setString(7, "24/7 On-Call"); ps.executeUpdate();
                ps.setInt(1, hId); ps.setString(2, "SECURITY"); ps.setString(3, "Main Gate Guard"); ps.setString(4, "Md. Abul Kashem"); ps.setString(5, "01711776655"); ps.setString(6, null); ps.setString(7, "24/7 Rotational"); ps.executeUpdate();
                ps.setInt(1, hId); ps.setString(2, "EMERGENCY"); ps.setString(3, "RUET Medical Ambulance"); ps.setString(4, "RUET Medical Center"); ps.setString(5, "01711000111"); ps.setString(6, "medical@ruet.ac.bd"); ps.setString(7, "24/7 Emergency"); ps.executeUpdate();
                ps.setInt(1, hId); ps.setString(2, "EMERGENCY"); ps.setString(3, "Fire Service Rajshahi"); ps.setString(4, "Fire Service & Civil Defence"); ps.setString(5, "999 / 01730000000"); ps.setString(6, null); ps.setString(7, "24/7 Emergency"); ps.executeUpdate();
                ps.setInt(1, hId); ps.setString(2, "EMERGENCY"); ps.setString(3, "Motihar Police Station"); ps.setString(4, "Duty Officer"); ps.setString(5, "01713373737"); ps.setString(6, null); ps.setString(7, "24/7 Emergency"); ps.executeUpdate();
            }
        }

        String insertUtil = "INSERT IGNORE INTO hall_utility_finances (hall_id, billing_month, electricity_bill, water_bill, internet_bill, gas_bill, maintenance_cost, cleaning_cost, total_expense, payment_status, payment_date, remarks) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertUtil)) {
            for (int hId : hallIds) {
                ps.setInt(1, hId); ps.setString(2, "July 2026"); ps.setDouble(3, 34500.0); ps.setDouble(4, 6200.0); ps.setDouble(5, 8000.0); ps.setDouble(6, 4500.0); ps.setDouble(7, 12300.0); ps.setDouble(8, 15000.0); ps.setDouble(9, 80500.0); ps.setString(10, "PAID"); ps.setDate(11, Date.valueOf("2026-08-05")); ps.setString(12, "Cleared by Finance Division"); ps.executeUpdate();
                ps.setInt(1, hId); ps.setString(2, "August 2026"); ps.setDouble(3, 38200.0); ps.setDouble(4, 6500.0); ps.setDouble(5, 8000.0); ps.setDouble(6, 4800.0); ps.setDouble(7, 9400.0); ps.setDouble(8, 15000.0); ps.setDouble(9, 81900.0); ps.setString(10, "PAID"); ps.setDate(11, Date.valueOf("2026-09-02")); ps.setString(12, "Cleared by Finance Division"); ps.executeUpdate();
                ps.setInt(1, hId); ps.setString(2, "September 2026"); ps.setDouble(3, 36100.0); ps.setDouble(4, 6400.0); ps.setDouble(5, 8000.0); ps.setDouble(6, 4600.0); ps.setDouble(7, 5200.0); ps.setDouble(8, 15000.0); ps.setDouble(9, 75300.0); ps.setString(10, "PENDING"); ps.setNull(11, java.sql.Types.DATE); ps.setString(12, "Invoice generated and forwarded"); ps.executeUpdate();
            }
        }

        String insertAsset = "INSERT IGNORE INTO hall_assets (hall_id, asset_code, asset_name, category, quantity, location, room_number, floor_number, purchase_date, purchase_price, condition_status, warranty_info, assigned_person, remarks) VALUES (?, ?, ?, ?, ?, ?, ?, ?, '2024-02-15', ?, 'GOOD', '3 Years Standard Warranty', 'Estate Officer', 'Barcoded and verified')";
        try (PreparedStatement ps = conn.prepareStatement(insertAsset)) {
            for (int hId : hallIds) {
                ps.setInt(1, hId); ps.setString(2, "AST-GEN-" + hId); ps.setString(3, "50 kVA Soundproof Diesel Generator"); ps.setString(4, "ELECTRICAL"); ps.setInt(5, 1); ps.setString(6, "Power Substation"); ps.setNull(7, java.sql.Types.INTEGER); ps.setInt(8, 1); ps.setDouble(9, 1250000.0); ps.executeUpdate();
                ps.setInt(1, hId); ps.setString(2, "AST-UPS-" + hId); ps.setString(3, "10 kVA Central Online UPS System"); ps.setString(4, "ELECTRICAL"); ps.setInt(5, 1); ps.setString(6, "Server Room"); ps.setNull(7, java.sql.Types.INTEGER); ps.setInt(8, 1); ps.setDouble(9, 180000.0); ps.executeUpdate();
                ps.setInt(1, hId); ps.setString(2, "AST-PC-" + hId); ps.setString(3, "Core i7 Desktop Computer Set"); ps.setString(4, "IT_EQUIPMENT"); ps.setInt(5, 20); ps.setString(6, "Computer Lab"); ps.setNull(7, java.sql.Types.INTEGER); ps.setInt(8, 2); ps.setDouble(9, 1400000.0); ps.executeUpdate();
                ps.setInt(1, hId); ps.setString(2, "AST-TT-" + hId); ps.setString(3, "Stiga International Table Tennis Board"); ps.setString(4, "SPORTS"); ps.setInt(5, 2); ps.setString(6, "Indoor Games Room"); ps.setNull(7, java.sql.Types.INTEGER); ps.setInt(8, 1); ps.setDouble(9, 65000.0); ps.executeUpdate();
                ps.setInt(1, hId); ps.setString(2, "AST-DIN-TBL-" + hId); ps.setString(3, "Heavy Gauge Stainless Steel Dining Tables"); ps.setString(4, "FURNITURE"); ps.setInt(5, 35); ps.setString(6, "Dining Hall"); ps.setNull(7, java.sql.Types.INTEGER); ps.setInt(8, 1); ps.setDouble(9, 525000.0); ps.executeUpdate();
            }
        }
    }

    public HallSummaryStats getHallSummaryStats(int hallId) throws SQLException {
        ensureSchemaAndSeedData();
        HallSummaryStats stats = new HallSummaryStats();
        stats.setHallId(hallId);

        String sql =
            "SELECT " +
            "    h.hall_name, " +
            "    h.hall_type, " +
            "    COALESCE(hd.total_floors, 4) AS total_floors, " +
            "    COALESCE(hd.total_staff, 25) AS total_staff, " +
            "    COALESCE(hd.non_residential_students, 120) AS non_residential_students, " +
            "    COUNT(DISTINCT r.id) AS total_rooms, " +
            "    COUNT(s.id) AS total_seats, " +
            "    COUNT(st.id) AS occupied_seats, " +
            "    (COUNT(s.id) - COUNT(st.id)) AS vacant_seats, " +
            "    (" +
            "        COALESCE((SELECT SUM(re.ac_count) FROM room_electrical re JOIN rooms rm ON re.room_id = rm.id WHERE rm.hall_id = ?), 0) + " +
            "        COALESCE((SELECT SUM(hca.ac_count) FROM hall_common_areas hca WHERE hca.hall_id = ?), 0) + " +
            "        COALESCE((SELECT hkd.dining_ac FROM hall_kitchen_dining hkd WHERE hkd.hall_id = ?), 0)" +
            "    ) AS total_ac, " +
            "    (" +
            "        COALESCE((SELECT SUM(re.ceiling_fan_count + re.wall_fan_count + re.exhaust_fan_count) FROM room_electrical re JOIN rooms rm ON re.room_id = rm.id WHERE rm.hall_id = ?), 0) + " +
            "        COALESCE((SELECT SUM(hca.fan_count) FROM hall_common_areas hca WHERE hca.hall_id = ?), 0) + " +
            "        COALESCE((SELECT hkd.dining_fans + hkd.exhaust_fan_count FROM hall_kitchen_dining hkd WHERE hkd.hall_id = ?), 0)" +
            "    ) AS total_fans, " +
            "    (" +
            "        COALESCE((SELECT SUM(re.tube_light_count + re.led_light_count + re.bulb_count + re.emergency_light_count) FROM room_electrical re JOIN rooms rm ON re.room_id = rm.id WHERE rm.hall_id = ?), 0) + " +
            "        COALESCE((SELECT SUM(hca.light_count) FROM hall_common_areas hca WHERE hca.hall_id = ?), 0) + " +
            "        COALESCE((SELECT hkd.dining_lights FROM hall_kitchen_dining hkd WHERE hkd.hall_id = ?), 0)" +
            "    ) AS total_lights, " +
            "    COALESCE((SELECT SUM(hf.washrooms_count) FROM hall_floors hf WHERE hf.hall_id = ?), 0) AS total_washrooms, " +
            "    COALESCE((SELECT COUNT(*) FROM hall_equipment_inventory hei WHERE hei.hall_id = ? AND hei.category = 'CCTV'), 0) AS total_cctv, " +
            "    COALESCE((SELECT COUNT(*) FROM hall_equipment_inventory hei WHERE hei.hall_id = ? AND hei.category = 'FIRE_SAFETY'), 0) AS total_fire_safety, " +
            "    COALESCE((SELECT COUNT(*) FROM hall_maintenance_records hmr WHERE hmr.hall_id = ? AND hmr.status IN ('PENDING', 'IN_PROGRESS')), 0) AS pending_maintenance " +
            "FROM halls h " +
            "LEFT JOIN hall_details hd ON hd.hall_id = h.id " +
            "LEFT JOIN rooms r ON r.hall_id = h.id " +
            "LEFT JOIN seats s ON s.room_id = r.id " +
            "LEFT JOIN students st ON st.current_hall_id = h.id " +
            "                     AND st.current_room = r.room_number " +
            "                     AND st.current_seat = s.seat_number " +
            "WHERE h.id = ? " +
            "GROUP BY h.id, h.hall_name, h.hall_type, hd.total_floors, hd.total_staff, hd.non_residential_students";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 1; i <= 14; i++) {
                ps.setInt(i, hallId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.setHallName(rs.getString("hall_name"));
                    stats.setHallType(rs.getString("hall_type"));
                    stats.setTotalFloors(rs.getInt("total_floors"));
                    stats.setTotalStaff(rs.getInt("total_staff"));
                    stats.setNonResidentialStudents(rs.getInt("non_residential_students"));
                    stats.setTotalRooms(rs.getInt("total_rooms"));
                    stats.setTotalSeats(rs.getInt("total_seats"));
                    stats.setOccupiedSeats(rs.getInt("occupied_seats"));
                    stats.setVacantSeats(rs.getInt("vacant_seats"));
                    stats.setResidentialStudents(rs.getInt("occupied_seats"));
                    stats.setTotalStudents(stats.getResidentialStudents() + stats.getNonResidentialStudents());
                    stats.setTotalAc(rs.getInt("total_ac"));
                    stats.setTotalFans(rs.getInt("total_fans"));
                    stats.setTotalLights(rs.getInt("total_lights"));
                    stats.setTotalWashrooms(rs.getInt("total_washrooms"));
                    stats.setTotalCctv(rs.getInt("total_cctv"));
                    stats.setTotalFireSafety(rs.getInt("total_fire_safety"));
                    stats.setPendingMaintenance(rs.getInt("pending_maintenance"));
                }
            }
        }
        return stats;
    }

    public HallDetail getHallDetail(int hallId) throws SQLException {
        ensureSchemaAndSeedData();
        String sql =
            "SELECT h.id, h.hall_name, h.hall_type, h.provost_name, h.provost_phone, " +
            "       hd.hall_code, hd.established_year, hd.address, hd.description, " +
            "       hd.total_floors, hd.total_staff, hd.non_residential_students, " +
            "       hd.assistant_provosts, hd.office_contact, hd.emergency_contact, hd.email " +
            "FROM halls h " +
            "LEFT JOIN hall_details hd ON hd.hall_id = h.id " +
            "WHERE h.id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hallId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    HallDetail d = new HallDetail();
                    d.setHallId(rs.getInt("id"));
                    d.setHallName(rs.getString("hall_name"));
                    d.setHallType(rs.getString("hall_type"));
                    d.setProvostName(rs.getString("provost_name"));
                    d.setProvostPhone(rs.getString("provost_phone"));
                    d.setHallCode(rs.getString("hall_code") != null ? rs.getString("hall_code") : "HALL-0" + hallId);
                    d.setEstablishedYear(rs.getInt("established_year") > 0 ? rs.getInt("established_year") : 1980);
                    d.setAddress(rs.getString("address") != null ? rs.getString("address") : "RUET Campus, Kazla, Rajshahi-6204");
                    d.setDescription(rs.getString("description"));
                    d.setTotalFloors(rs.getInt("total_floors") > 0 ? rs.getInt("total_floors") : 4);
                    d.setTotalStaff(rs.getInt("total_staff") > 0 ? rs.getInt("total_staff") : 25);
                    d.setNonResidentialStudents(rs.getInt("non_residential_students"));
                    d.setAssistantProvosts(rs.getString("assistant_provosts"));
                    d.setOfficeContact(rs.getString("office_contact"));
                    d.setEmergencyContact(rs.getString("emergency_contact"));
                    d.setEmail(rs.getString("email"));
                    return d;
                }
            }
        }
        return null;
    }

    public List<FloorInfo> getFloorsByHallId(int hallId) throws SQLException {
        ensureSchemaAndSeedData();
        List<FloorInfo> list = new ArrayList<>();

        String sql =
            "SELECT " +
            "    hf.floor_number, " +
            "    hf.floor_name, " +
            "    COUNT(DISTINCT r.id) AS total_rooms, " +
            "    COUNT(s.id) AS total_seats, " +
            "    COUNT(st.id) AS occupied_seats, " +
            "    (COUNT(s.id) - COUNT(st.id)) AS vacant_seats, " +
            "    COALESCE(SUM(re.ac_count), 0) AS ac_count, " +
            "    COALESCE(SUM(re.ceiling_fan_count + re.wall_fan_count + re.exhaust_fan_count), 0) AS fans_count, " +
            "    COALESCE(SUM(re.tube_light_count + re.led_light_count + re.bulb_count + re.emergency_light_count), 0) AS lights_count, " +
            "    hf.windows_count, " +
            "    hf.doors_count, " +
            "    hf.washrooms_count, " +
            "    hf.toilets_count, " +
            "    hf.water_taps, " +
            "    hf.emergency_exits " +
            "FROM hall_floors hf " +
            "LEFT JOIN rooms r ON r.hall_id = hf.hall_id AND (r.room_number DIV 100) = hf.floor_number " +
            "LEFT JOIN room_electrical re ON re.room_id = r.id " +
            "LEFT JOIN seats s ON s.room_id = r.id " +
            "LEFT JOIN students st ON st.current_hall_id = hf.hall_id " +
            "                     AND st.current_room = r.room_number " +
            "                     AND st.current_seat = s.seat_number " +
            "WHERE hf.hall_id = ? " +
            "GROUP BY hf.id, hf.floor_number, hf.floor_name, hf.windows_count, hf.doors_count, hf.washrooms_count, hf.toilets_count, hf.water_taps, hf.emergency_exits " +
            "ORDER BY hf.floor_number ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hallId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    FloorInfo f = new FloorInfo();
                    f.setFloorNumber(rs.getInt("floor_number"));
                    f.setFloorName(rs.getString("floor_name"));
                    f.setTotalRooms(rs.getInt("total_rooms"));
                    f.setTotalSeats(rs.getInt("total_seats"));
                    f.setOccupiedSeats(rs.getInt("occupied_seats"));
                    f.setVacantSeats(rs.getInt("vacant_seats"));
                    f.setAcCount(rs.getInt("ac_count"));
                    f.setFansCount(rs.getInt("fans_count"));
                    f.setLightsCount(rs.getInt("lights_count"));
                    f.setWindowsCount(rs.getInt("windows_count"));
                    f.setDoorsCount(rs.getInt("doors_count"));
                    f.setWashroomsCount(rs.getInt("washrooms_count"));
                    f.setToiletsCount(rs.getInt("toilets_count"));
                    f.setWaterTaps(rs.getInt("water_taps"));
                    f.setEmergencyExits(rs.getInt("emergency_exits"));
                    list.add(f);
                }
            }
        }
        return list;
    }

    public List<RoomFullProfile> getRoomFullProfiles(int hallId, Integer floorFilter, String search) throws SQLException {
        ensureSchemaAndSeedData();
        List<RoomFullProfile> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
            "SELECT " +
            "    r.id AS room_id, " +
            "    r.hall_id, " +
            "    h.hall_name, " +
            "    r.room_number, " +
            "    (r.room_number DIV 100) AS floor_number, " +
            "    COALESCE(rd.room_type, 'STANDARD') AS room_type, " +
            "    r.total_seats, " +
            "    COUNT(st.id) AS occupied_seats, " +
            "    (r.total_seats - COUNT(st.id)) AS available_seats, " +
            "    COALESCE(rd.room_size_sqft, 240.0) AS room_size_sqft, " +
            "    COALESCE(rd.window_count, 2) AS window_count, " +
            "    COALESCE(rd.door_count, 1) AS door_count, " +
            "    COALESCE(rd.has_balcony, 1) AS has_balcony, " +
            "    COALESCE(rd.has_attached_bath, 0) AS has_attached_bath, " +
            "    COALESCE(rd.condition_status, 'GOOD') AS condition_status, " +
            "    COALESCE(re.ac_count, 0) AS ac_count, " +
            "    COALESCE(re.ceiling_fan_count, 2) AS ceiling_fan_count, " +
            "    COALESCE(re.wall_fan_count, 0) AS wall_fan_count, " +
            "    COALESCE(re.exhaust_fan_count, 0) AS exhaust_fan_count, " +
            "    COALESCE(re.tube_light_count, 2) AS tube_light_count, " +
            "    COALESCE(re.led_light_count, 2) AS led_light_count, " +
            "    COALESCE(re.bulb_count, 0) AS bulb_count, " +
            "    COALESCE(re.emergency_light_count, 1) AS emergency_light_count, " +
            "    COALESCE(re.night_light_count, 1) AS night_light_count, " +
            "    COALESCE(re.switch_count, 6) AS switch_count, " +
            "    COALESCE(re.socket_count, 4) AS socket_count, " +
            "    COALESCE(re.power_outlet_count, 2) AS power_outlet_count, " +
            "    COALESCE(rf.bed_count, 4) AS bed_count, " +
            "    COALESCE(rf.table_count, 4) AS table_count, " +
            "    COALESCE(rf.chair_count, 4) AS chair_count, " +
            "    COALESCE(rf.wardrobe_count, 4) AS wardrobe_count, " +
            "    COALESCE(rf.bookshelf_count, 2) AS bookshelf_count, " +
            "    COALESCE(rf.good_condition_count, 21) AS good_condition_count, " +
            "    COALESCE(rf.damaged_count, 0) AS damaged_count, " +
            "    COALESCE(rf.under_repair_count, 0) AS under_repair_count, " +
            "    COALESCE(rf.missing_count, 0) AS missing_count, " +
            "    (SELECT COUNT(*) FROM hall_maintenance_records mr WHERE mr.hall_id = r.hall_id AND mr.room_number = r.room_number AND mr.status IN ('PENDING','IN_PROGRESS')) AS pending_maintenance_count " +
            "FROM rooms r " +
            "JOIN halls h ON h.id = r.hall_id " +
            "LEFT JOIN room_details rd ON rd.room_id = r.id " +
            "LEFT JOIN room_electrical re ON re.room_id = r.id " +
            "LEFT JOIN room_furniture rf ON rf.room_id = r.id " +
            "LEFT JOIN seats s ON s.room_id = r.id " +
            "LEFT JOIN students st ON st.current_hall_id = r.hall_id " +
            "                     AND st.current_room = r.room_number " +
            "                     AND st.current_seat = s.seat_number " +
            "WHERE r.hall_id = ? "
        );

        if (floorFilter != null && floorFilter > 0) {
            sql.append("AND (r.room_number DIV 100) = ").append(floorFilter).append(" ");
        }
        if (search != null && !search.trim().isEmpty()) {
            sql.append("AND (CAST(r.room_number AS CHAR) LIKE '%").append(search.trim()).append("%' OR rd.room_type LIKE '%").append(search.trim()).append("%') ");
        }

        sql.append("GROUP BY r.id, r.hall_id, h.hall_name, r.room_number, r.total_seats, rd.room_type, rd.room_size_sqft, rd.window_count, rd.door_count, rd.has_balcony, rd.has_attached_bath, rd.condition_status, re.ac_count, re.ceiling_fan_count, re.wall_fan_count, re.exhaust_fan_count, re.tube_light_count, re.led_light_count, re.bulb_count, re.emergency_light_count, re.night_light_count, re.switch_count, re.socket_count, re.power_outlet_count, rf.bed_count, rf.table_count, rf.chair_count, rf.wardrobe_count, rf.bookshelf_count, rf.good_condition_count, rf.damaged_count, rf.under_repair_count, rf.missing_count ");
        sql.append("ORDER BY r.room_number ASC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, hallId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RoomFullProfile p = new RoomFullProfile();
                    p.setRoomId(rs.getInt("room_id"));
                    p.setHallId(rs.getInt("hall_id"));
                    p.setHallName(rs.getString("hall_name"));
                    p.setRoomNumber(rs.getInt("room_number"));
                    p.setFloorNumber(rs.getInt("floor_number"));
                    p.setRoomType(rs.getString("room_type"));
                    p.setTotalSeats(rs.getInt("total_seats"));
                    p.setOccupiedSeats(rs.getInt("occupied_seats"));
                    p.setAvailableSeats(rs.getInt("available_seats"));
                    p.setRoomSizeSqft(rs.getDouble("room_size_sqft"));
                    p.setWindowCount(rs.getInt("window_count"));
                    p.setDoorCount(rs.getInt("door_count"));
                    p.setHasBalcony(rs.getInt("has_balcony") == 1);
                    p.setHasAttachedBath(rs.getInt("has_attached_bath") == 1);
                    p.setConditionStatus(rs.getString("condition_status"));
                    p.setAcCount(rs.getInt("ac_count"));
                    p.setCeilingFanCount(rs.getInt("ceiling_fan_count"));
                    p.setWallFanCount(rs.getInt("wall_fan_count"));
                    p.setExhaustFanCount(rs.getInt("exhaust_fan_count"));
                    p.setTubeLightCount(rs.getInt("tube_light_count"));
                    p.setLedLightCount(rs.getInt("led_light_count"));
                    p.setBulbCount(rs.getInt("bulb_count"));
                    p.setEmergencyLightCount(rs.getInt("emergency_light_count"));
                    p.setNightLightCount(rs.getInt("night_light_count"));
                    p.setSwitchCount(rs.getInt("switch_count"));
                    p.setSocketCount(rs.getInt("socket_count"));
                    p.setPowerOutletCount(rs.getInt("power_outlet_count"));
                    p.setBedCount(rs.getInt("bed_count"));
                    p.setTableCount(rs.getInt("table_count"));
                    p.setChairCount(rs.getInt("chair_count"));
                    p.setWardrobeCount(rs.getInt("wardrobe_count"));
                    p.setBookshelfCount(rs.getInt("bookshelf_count"));
                    p.setGoodConditionCount(rs.getInt("good_condition_count"));
                    p.setDamagedCount(rs.getInt("damaged_count"));
                    p.setUnderRepairCount(rs.getInt("under_repair_count"));
                    p.setMissingCount(rs.getInt("missing_count"));
                    p.setPendingMaintenanceCount(rs.getInt("pending_maintenance_count"));
                    list.add(p);
                }
            }
        }
        return list;
    }

    public RoomFullProfile getRoomFullProfileById(int roomId) throws SQLException {
        ensureSchemaAndSeedData();
        List<RoomFullProfile> singleList = new ArrayList<>();
        String sql =
            "SELECT " +
            "    r.id AS room_id, " +
            "    r.hall_id, " +
            "    h.hall_name, " +
            "    r.room_number, " +
            "    (r.room_number DIV 100) AS floor_number, " +
            "    COALESCE(rd.room_type, 'STANDARD') AS room_type, " +
            "    r.total_seats, " +
            "    COUNT(st.id) AS occupied_seats, " +
            "    (r.total_seats - COUNT(st.id)) AS available_seats, " +
            "    COALESCE(rd.room_size_sqft, 240.0) AS room_size_sqft, " +
            "    COALESCE(rd.window_count, 2) AS window_count, " +
            "    COALESCE(rd.door_count, 1) AS door_count, " +
            "    COALESCE(rd.has_balcony, 1) AS has_balcony, " +
            "    COALESCE(rd.has_attached_bath, 0) AS has_attached_bath, " +
            "    COALESCE(rd.condition_status, 'GOOD') AS condition_status, " +
            "    COALESCE(re.ac_count, 0) AS ac_count, " +
            "    COALESCE(re.ceiling_fan_count, 2) AS ceiling_fan_count, " +
            "    COALESCE(re.wall_fan_count, 0) AS wall_fan_count, " +
            "    COALESCE(re.exhaust_fan_count, 0) AS exhaust_fan_count, " +
            "    COALESCE(re.tube_light_count, 2) AS tube_light_count, " +
            "    COALESCE(re.led_light_count, 2) AS led_light_count, " +
            "    COALESCE(re.bulb_count, 0) AS bulb_count, " +
            "    COALESCE(re.emergency_light_count, 1) AS emergency_light_count, " +
            "    COALESCE(re.night_light_count, 1) AS night_light_count, " +
            "    COALESCE(re.switch_count, 6) AS switch_count, " +
            "    COALESCE(re.socket_count, 4) AS socket_count, " +
            "    COALESCE(re.power_outlet_count, 2) AS power_outlet_count, " +
            "    COALESCE(rf.bed_count, 4) AS bed_count, " +
            "    COALESCE(rf.table_count, 4) AS table_count, " +
            "    COALESCE(rf.chair_count, 4) AS chair_count, " +
            "    COALESCE(rf.wardrobe_count, 4) AS wardrobe_count, " +
            "    COALESCE(rf.bookshelf_count, 2) AS bookshelf_count, " +
            "    COALESCE(rf.mattress_count, 4) AS mattress_count, " +
            "    COALESCE(rf.mirror_count, 1) AS mirror_count, " +
            "    COALESCE(rf.curtain_count, 2) AS curtain_count, " +
            "    COALESCE(rf.dustbin_count, 1) AS dustbin_count, " +
            "    COALESCE(rf.good_condition_count, 21) AS good_condition_count, " +
            "    COALESCE(rf.damaged_count, 0) AS damaged_count, " +
            "    COALESCE(rf.under_repair_count, 0) AS under_repair_count, " +
            "    COALESCE(rf.missing_count, 0) AS missing_count, " +
            "    (SELECT COUNT(*) FROM hall_maintenance_records mr WHERE mr.hall_id = r.hall_id AND mr.room_number = r.room_number AND mr.status IN ('PENDING','IN_PROGRESS')) AS pending_maintenance_count, " +
            "    (SELECT problem_description FROM hall_maintenance_records mr WHERE mr.hall_id = r.hall_id AND mr.room_number = r.room_number ORDER BY mr.id DESC LIMIT 1) AS latest_problem " +
            "FROM rooms r " +
            "JOIN halls h ON h.id = r.hall_id " +
            "LEFT JOIN room_details rd ON rd.room_id = r.id " +
            "LEFT JOIN room_electrical re ON re.room_id = r.id " +
            "LEFT JOIN room_furniture rf ON rf.room_id = r.id " +
            "LEFT JOIN seats s ON s.room_id = r.id " +
            "LEFT JOIN students st ON st.current_hall_id = r.hall_id " +
            "                     AND st.current_room = r.room_number " +
            "                     AND st.current_seat = s.seat_number " +
            "WHERE r.id = ? " +
            "GROUP BY r.id, r.hall_id, h.hall_name, r.room_number, r.total_seats, rd.room_type, rd.room_size_sqft, rd.window_count, rd.door_count, rd.has_balcony, rd.has_attached_bath, rd.condition_status, re.ac_count, re.ceiling_fan_count, re.wall_fan_count, re.exhaust_fan_count, re.tube_light_count, re.led_light_count, re.bulb_count, re.emergency_light_count, re.night_light_count, re.switch_count, re.socket_count, re.power_outlet_count, rf.bed_count, rf.table_count, rf.chair_count, rf.wardrobe_count, rf.bookshelf_count, rf.mattress_count, rf.mirror_count, rf.curtain_count, rf.dustbin_count, rf.good_condition_count, rf.damaged_count, rf.under_repair_count, rf.missing_count";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    RoomFullProfile p = new RoomFullProfile();
                    p.setRoomId(rs.getInt("room_id"));
                    p.setHallId(rs.getInt("hall_id"));
                    p.setHallName(rs.getString("hall_name"));
                    p.setRoomNumber(rs.getInt("room_number"));
                    p.setFloorNumber(rs.getInt("floor_number"));
                    p.setRoomType(rs.getString("room_type"));
                    p.setTotalSeats(rs.getInt("total_seats"));
                    p.setOccupiedSeats(rs.getInt("occupied_seats"));
                    p.setAvailableSeats(rs.getInt("available_seats"));
                    p.setRoomSizeSqft(rs.getDouble("room_size_sqft"));
                    p.setWindowCount(rs.getInt("window_count"));
                    p.setDoorCount(rs.getInt("door_count"));
                    p.setHasBalcony(rs.getInt("has_balcony") == 1);
                    p.setHasAttachedBath(rs.getInt("has_attached_bath") == 1);
                    p.setConditionStatus(rs.getString("condition_status"));
                    p.setAcCount(rs.getInt("ac_count"));
                    p.setCeilingFanCount(rs.getInt("ceiling_fan_count"));
                    p.setWallFanCount(rs.getInt("wall_fan_count"));
                    p.setExhaustFanCount(rs.getInt("exhaust_fan_count"));
                    p.setTubeLightCount(rs.getInt("tube_light_count"));
                    p.setLedLightCount(rs.getInt("led_light_count"));
                    p.setBulbCount(rs.getInt("bulb_count"));
                    p.setEmergencyLightCount(rs.getInt("emergency_light_count"));
                    p.setNightLightCount(rs.getInt("night_light_count"));
                    p.setSwitchCount(rs.getInt("switch_count"));
                    p.setSocketCount(rs.getInt("socket_count"));
                    p.setPowerOutletCount(rs.getInt("power_outlet_count"));
                    p.setBedCount(rs.getInt("bed_count"));
                    p.setTableCount(rs.getInt("table_count"));
                    p.setChairCount(rs.getInt("chair_count"));
                    p.setWardrobeCount(rs.getInt("wardrobe_count"));
                    p.setBookshelfCount(rs.getInt("bookshelf_count"));
                    p.setMattressCount(rs.getInt("mattress_count"));
                    p.setMirrorCount(rs.getInt("mirror_count"));
                    p.setCurtainCount(rs.getInt("curtain_count"));
                    p.setDustbinCount(rs.getInt("dustbin_count"));
                    p.setGoodConditionCount(rs.getInt("good_condition_count"));
                    p.setDamagedCount(rs.getInt("damaged_count"));
                    p.setUnderRepairCount(rs.getInt("under_repair_count"));
                    p.setMissingCount(rs.getInt("missing_count"));
                    p.setPendingMaintenanceCount(rs.getInt("pending_maintenance_count"));
                    p.setLatestMaintenanceIssue(rs.getString("latest_problem"));

                    p.setOccupants(getSeatsByRoomId(roomId));
                    return p;
                }
            }
        }
        return null;
    }

    public List<EquipmentItem> getEquipmentInventory(int hallId, String categoryFilter) throws SQLException {
        ensureSchemaAndSeedData();
        List<EquipmentItem> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM hall_equipment_inventory WHERE hall_id = ? ");
        if (categoryFilter != null && !categoryFilter.equalsIgnoreCase("ALL")) {
            sql.append("AND category = '").append(categoryFilter.toUpperCase()).append("' ");
        }
        sql.append("ORDER BY id ASC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, hallId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    EquipmentItem item = new EquipmentItem();
                    item.setId(rs.getInt("id"));
                    item.setHallId(rs.getInt("hall_id"));
                    item.setItemCode(rs.getString("item_code"));
                    item.setCategory(rs.getString("category"));
                    item.setEquipmentType(rs.getString("equipment_type"));
                    item.setLocationType(rs.getString("location_type"));
                    item.setRoomNumber(rs.getInt("room_number"));
                    item.setFloorNumber(rs.getInt("floor_number"));
                    item.setSpecificLocation(rs.getString("specific_location"));
                    item.setBrandModel(rs.getString("brand_model"));
                    item.setCapacityRating(rs.getString("capacity_rating"));
                    item.setInstallDate(rs.getDate("install_date") != null ? rs.getDate("install_date").toLocalDate() : null);
                    item.setConditionStatus(rs.getString("condition_status"));
                    item.setLastServiceDate(rs.getDate("last_service_date") != null ? rs.getDate("last_service_date").toLocalDate() : null);
                    item.setNextServiceDate(rs.getDate("next_service_date") != null ? rs.getDate("next_service_date").toLocalDate() : null);
                    item.setRemarks(rs.getString("remarks"));
                    list.add(item);
                }
            }
        }
        return list;
    }

    public List<MaintenanceRecord> getMaintenanceRecords(int hallId, String statusFilter) throws SQLException {
        ensureSchemaAndSeedData();
        List<MaintenanceRecord> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM hall_maintenance_records WHERE hall_id = ? ");
        if (statusFilter != null && !statusFilter.equalsIgnoreCase("ALL")) {
            sql.append("AND status = '").append(statusFilter.toUpperCase()).append("' ");
        }
        sql.append("ORDER BY report_date DESC, id DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, hallId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MaintenanceRecord rec = new MaintenanceRecord();
                    rec.setId(rs.getInt("id"));
                    rec.setHallId(rs.getInt("hall_id"));
                    rec.setRecordCode(rs.getString("record_code"));
                    rec.setLocation(rs.getString("location"));
                    rec.setRoomNumber(rs.getInt("room_number"));
                    rec.setEquipmentCategory(rs.getString("equipment_category"));
                    rec.setProblemDescription(rs.getString("problem_description"));
                    rec.setReportedBy(rs.getString("reported_by"));
                    rec.setReportDate(rs.getDate("report_date") != null ? rs.getDate("report_date").toLocalDate() : LocalDate.now());
                    rec.setAssignedPerson(rs.getString("assigned_person"));
                    rec.setPriority(rs.getString("priority"));
                    rec.setStatus(rs.getString("status"));
                    rec.setEstimatedCost(rs.getDouble("estimated_cost"));
                    rec.setActualCost(rs.getDouble("actual_cost"));
                    rec.setCompletionDate(rs.getDate("completion_date") != null ? rs.getDate("completion_date").toLocalDate() : null);
                    rec.setRemarks(rs.getString("remarks"));
                    list.add(rec);
                }
            }
        }
        return list;
    }

    public boolean createMaintenanceRecord(MaintenanceRecord rec) throws SQLException {
        ensureSchemaAndSeedData();
        String sql = "INSERT INTO hall_maintenance_records (hall_id, record_code, location, room_number, equipment_category, problem_description, reported_by, report_date, assigned_person, priority, status, estimated_cost, actual_cost, remarks) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, rec.getHallId());
            ps.setString(2, "MNT-" + System.currentTimeMillis() % 100000);
            ps.setString(3, rec.getLocation());
            if (rec.getRoomNumber() > 0) ps.setInt(4, rec.getRoomNumber()); else ps.setNull(4, java.sql.Types.INTEGER);
            ps.setString(5, rec.getEquipmentCategory());
            ps.setString(6, rec.getProblemDescription());
            ps.setString(7, rec.getReportedBy());
            ps.setDate(8, Date.valueOf(rec.getReportDate() != null ? rec.getReportDate() : LocalDate.now()));
            ps.setString(9, rec.getAssignedPerson());
            ps.setString(10, rec.getPriority());
            ps.setString(11, rec.getStatus());
            ps.setDouble(12, rec.getEstimatedCost());
            ps.setDouble(13, rec.getActualCost());
            ps.setString(14, rec.getRemarks());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateMaintenanceStatus(int recordId, String status, double actualCost, String remarks) throws SQLException {
        ensureSchemaAndSeedData();
        String sql = "UPDATE hall_maintenance_records SET status = ?, actual_cost = ?, remarks = ?, completion_date = (CASE WHEN ? = 'COMPLETED' THEN CURDATE() ELSE completion_date END) WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setDouble(2, actualCost);
            ps.setString(3, remarks);
            ps.setString(4, status);
            ps.setInt(5, recordId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<CleaningSchedule> getCleaningSchedules(int hallId) throws SQLException {
        ensureSchemaAndSeedData();
        List<CleaningSchedule> list = new ArrayList<>();
        String sql = "SELECT * FROM hall_cleaning_schedules WHERE hall_id = ? ORDER BY id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hallId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CleaningSchedule s = new CleaningSchedule();
                    s.setId(rs.getInt("id"));
                    s.setHallId(rs.getInt("hall_id"));
                    s.setAreaType(rs.getString("area_type"));
                    s.setFrequency(rs.getString("frequency"));
                    s.setResponsibleStaff(rs.getString("responsible_staff"));
                    s.setStatus(rs.getString("status"));
                    s.setLastCleanedDate(rs.getDate("last_cleaned_date") != null ? rs.getDate("last_cleaned_date").toLocalDate() : null);
                    s.setNextScheduledDate(rs.getDate("next_scheduled_date") != null ? rs.getDate("next_scheduled_date").toLocalDate() : null);
                    s.setRemarks(rs.getString("remarks"));
                    list.add(s);
                }
            }
        }
        return list;
    }

    public List<HallContact> getHallContacts(int hallId) throws SQLException {
        ensureSchemaAndSeedData();
        List<HallContact> list = new ArrayList<>();
        String sql = "SELECT * FROM hall_contacts WHERE hall_id = ? ORDER BY id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hallId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new HallContact(
                        rs.getInt("id"),
                        rs.getInt("hall_id"),
                        rs.getString("category"),
                        rs.getString("designation"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("availability")
                    ));
                }
            }
        }
        return list;
    }

    public List<UtilityRecord> getUtilityRecords(int hallId) throws SQLException {
        ensureSchemaAndSeedData();
        List<UtilityRecord> list = new ArrayList<>();
        String sql = "SELECT * FROM hall_utility_finances WHERE hall_id = ? ORDER BY id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hallId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UtilityRecord u = new UtilityRecord();
                    u.setId(rs.getInt("id"));
                    u.setHallId(rs.getInt("hall_id"));
                    u.setBillingMonth(rs.getString("billing_month"));
                    u.setElectricityBill(rs.getDouble("electricity_bill"));
                    u.setWaterBill(rs.getDouble("water_bill"));
                    u.setInternetBill(rs.getDouble("internet_bill"));
                    u.setGasBill(rs.getDouble("gas_bill"));
                    u.setMaintenanceCost(rs.getDouble("maintenance_cost"));
                    u.setCleaningCost(rs.getDouble("cleaning_cost"));
                    u.setTotalExpense(rs.getDouble("total_expense"));
                    u.setPaymentStatus(rs.getString("payment_status"));
                    u.setPaymentDate(rs.getDate("payment_date") != null ? rs.getDate("payment_date").toLocalDate() : null);
                    u.setRemarks(rs.getString("remarks"));
                    list.add(u);
                }
            }
        }
        return list;
    }

    public List<AssetItem> getHallAssets(int hallId, String categoryFilter) throws SQLException {
        ensureSchemaAndSeedData();
        List<AssetItem> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM hall_assets WHERE hall_id = ? ");
        if (categoryFilter != null && !categoryFilter.equalsIgnoreCase("ALL")) {
            sql.append("AND category = '").append(categoryFilter.toUpperCase()).append("' ");
        }
        sql.append("ORDER BY id ASC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, hallId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AssetItem a = new AssetItem();
                    a.setId(rs.getInt("id"));
                    a.setHallId(rs.getInt("hall_id"));
                    a.setAssetCode(rs.getString("asset_code"));
                    a.setAssetName(rs.getString("asset_name"));
                    a.setCategory(rs.getString("category"));
                    a.setQuantity(rs.getInt("quantity"));
                    a.setLocation(rs.getString("location"));
                    a.setRoomNumber(rs.getInt("room_number"));
                    a.setFloorNumber(rs.getInt("floor_number"));
                    a.setPurchaseDate(rs.getDate("purchase_date") != null ? rs.getDate("purchase_date").toLocalDate() : null);
                    a.setPurchasePrice(rs.getDouble("purchase_price"));
                    a.setConditionStatus(rs.getString("condition_status"));
                    a.setWarrantyInfo(rs.getString("warranty_info"));
                    a.setAssignedPerson(rs.getString("assigned_person"));
                    a.setRemarks(rs.getString("remarks"));
                    list.add(a);
                }
            }
        }
        return list;
    }

    public List<CommonAreaInfo> getCommonAreasByHallId(int hallId) throws SQLException {
        ensureSchemaAndSeedData();
        List<CommonAreaInfo> list = new ArrayList<>();
        String sql = "SELECT * FROM hall_common_areas WHERE hall_id = ? ORDER BY id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hallId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CommonAreaInfo ca = new CommonAreaInfo();
                    ca.setId(rs.getInt("id"));
                    ca.setHallId(rs.getInt("hall_id"));
                    ca.setAreaName(rs.getString("area_name"));
                    ca.setCapacity(rs.getInt("capacity"));
                    ca.setAcCount(rs.getInt("ac_count"));
                    ca.setFanCount(rs.getInt("fan_count"));
                    ca.setLightCount(rs.getInt("light_count"));
                    ca.setTableCount(rs.getInt("table_count"));
                    ca.setChairCount(rs.getInt("chair_count"));
                    ca.setConditionStatus(rs.getString("condition_status"));
                    ca.setResponsibleStaff(rs.getString("responsible_staff"));
                    ca.setRemarks(rs.getString("remarks"));
                    list.add(ca);
                }
            }
        }
        return list;
    }

    public KitchenDiningInfo getKitchenDiningByHallId(int hallId) throws SQLException {
        ensureSchemaAndSeedData();
        String sql = "SELECT * FROM hall_kitchen_dining WHERE hall_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hallId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    KitchenDiningInfo kd = new KitchenDiningInfo();
                    kd.setId(rs.getInt("id"));
                    kd.setHallId(rs.getInt("hall_id"));
                    kd.setKitchenSizeSqft(rs.getDouble("kitchen_size_sqft"));
                    kd.setCookingAreaDesc(rs.getString("cooking_area_desc"));
                    kd.setStoveCount(rs.getInt("stove_count"));
                    kd.setGasBurnerCount(rs.getInt("gas_burner_count"));
                    kd.setRefrigeratorCount(rs.getInt("refrigerator_count"));
                    kd.setFreezerCount(rs.getInt("freezer_count"));
                    kd.setWaterFilterCount(rs.getInt("water_filter_count"));
                    kd.setSinkCount(rs.getInt("sink_count"));
                    kd.setExhaustFanCount(rs.getInt("exhaust_fan_count"));
                    kd.setKitchenStaffCount(rs.getInt("kitchen_staff_count"));
                    kd.setDiningCapacity(rs.getInt("dining_capacity"));
                    kd.setDiningTableCount(rs.getInt("dining_table_count"));
                    kd.setDiningChairCount(rs.getInt("dining_chair_count"));
                    kd.setDiningFans(rs.getInt("dining_fans"));
                    kd.setDiningLights(rs.getInt("dining_lights"));
                    kd.setDiningAc(rs.getInt("dining_ac"));
                    kd.setWashBasinCount(rs.getInt("wash_basin_count"));
                    return kd;
                }
            }
        }
        return null;
    }

    public WaterSystemInfo getWaterSystemByHallId(int hallId) throws SQLException {
        ensureSchemaAndSeedData();
        String sql = "SELECT * FROM hall_water_system WHERE hall_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hallId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    WaterSystemInfo w = new WaterSystemInfo();
                    w.setId(rs.getInt("id"));
                    w.setHallId(rs.getInt("hall_id"));
                    w.setSourceType(rs.getString("source_type"));
                    w.setDeepTubeWell(rs.getString("deep_tube_well"));
                    w.setPumpCount(rs.getInt("pump_count"));
                    w.setTankCount(rs.getInt("tank_count"));
                    w.setTotalTankCapacityLiters(rs.getInt("total_tank_capacity_liters"));
                    w.setFiltersCount(rs.getInt("filters_count"));
                    w.setPurifiersCount(rs.getInt("purifiers_count"));
                    w.setSupplySchedule(rs.getString("supply_schedule"));
                    w.setDrinkingWaterPoints(rs.getInt("drinking_water_points"));
                    w.setTapCount(rs.getInt("tap_count"));
                    w.setPumpCondition(rs.getString("pump_condition"));
                    w.setTankCondition(rs.getString("tank_condition"));
                    w.setLastCleaningDate(rs.getDate("last_cleaning_date") != null ? rs.getDate("last_cleaning_date").toLocalDate() : null);
                    w.setNextCleaningDate(rs.getDate("next_cleaning_date") != null ? rs.getDate("next_cleaning_date").toLocalDate() : null);
                    return w;
                }
            }
        }
        return null;
    }

    public boolean updateHallDetail(HallDetail d) throws SQLException {
        ensureSchemaAndSeedData();
        String sql = "UPDATE hall_details SET established_year = ?, address = ?, description = ?, total_staff = ?, non_residential_students = ?, assistant_provosts = ?, office_contact = ?, emergency_contact = ?, email = ? WHERE hall_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, d.getEstablishedYear());
            ps.setString(2, d.getAddress());
            ps.setString(3, d.getDescription());
            ps.setInt(4, d.getTotalStaff());
            ps.setInt(5, d.getNonResidentialStudents());
            ps.setString(6, d.getAssistantProvosts());
            ps.setString(7, d.getOfficeContact());
            ps.setString(8, d.getEmergencyContact());
            ps.setString(9, d.getEmail());
            ps.setInt(10, d.getHallId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateRoomElectrical(int roomId, int ac, int ceilingFan, int ledLight, int tubeLight, int sockets) throws SQLException {
        ensureSchemaAndSeedData();
        String sql = "UPDATE room_electrical SET ac_count = ?, ceiling_fan_count = ?, led_light_count = ?, tube_light_count = ?, socket_count = ? WHERE room_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Math.max(0, ac));
            ps.setInt(2, Math.max(0, ceilingFan));
            ps.setInt(3, Math.max(0, ledLight));
            ps.setInt(4, Math.max(0, tubeLight));
            ps.setInt(5, Math.max(0, sockets));
            ps.setInt(6, roomId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean createAssetItem(AssetItem item) throws SQLException {
        ensureSchemaAndSeedData();
        String sql = "INSERT INTO hall_assets (hall_id, asset_code, asset_name, category, quantity, location, purchase_date, purchase_price, condition_status, warranty_info, assigned_person, remarks) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, item.getHallId());
            ps.setString(2, item.getAssetCode());
            ps.setString(3, item.getAssetName());
            ps.setString(4, item.getCategory());
            ps.setInt(5, item.getQuantity());
            ps.setString(6, item.getLocation());
            ps.setDate(7, item.getPurchaseDate() != null ? Date.valueOf(item.getPurchaseDate()) : Date.valueOf(LocalDate.now()));
            ps.setDouble(8, item.getPurchasePrice());
            ps.setString(9, item.getConditionStatus() != null ? item.getConditionStatus() : "GOOD");
            ps.setString(10, item.getWarrantyInfo());
            ps.setString(11, item.getAssignedPerson());
            ps.setString(12, item.getRemarks());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateRoomFurniture(int roomId, int beds, int tables, int chairs, int wardrobes, int bookshelves) throws SQLException {
        ensureSchemaAndSeedData();
        String sql = "UPDATE room_furniture SET bed_count = ?, table_count = ?, chair_count = ?, wardrobe_count = ?, bookshelf_count = ? WHERE room_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Math.max(0, beds));
            ps.setInt(2, Math.max(0, tables));
            ps.setInt(3, Math.max(0, chairs));
            ps.setInt(4, Math.max(0, wardrobes));
            ps.setInt(5, Math.max(0, bookshelves));
            ps.setInt(6, roomId);
            return ps.executeUpdate() > 0;
        }
    }
}

