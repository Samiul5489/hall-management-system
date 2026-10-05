package com.hallmanagement.model;

public class SeatInfo {
    private int seatId;
    private int roomId;
    private int roomNumber;
    private int hallId;
    private String hallName;
    private int seatNumber;
    private boolean occupied;
    private String studentUserId;
    private String studentName;
    private String studentRoll;
    private String studentDept;
    private String studentYear;

    public SeatInfo(int seatId, int roomId, int roomNumber, int hallId, String hallName, int seatNumber,
                    boolean occupied, String studentUserId, String studentName, String studentRoll,
                    String studentDept, String studentYear) {
        this.seatId = seatId;
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.hallId = hallId;
        this.hallName = hallName;
        this.seatNumber = seatNumber;
        this.occupied = occupied;
        this.studentUserId = studentUserId;
        this.studentName = studentName;
        this.studentRoll = studentRoll != null ? studentRoll : studentUserId;
        this.studentDept = studentDept;
        this.studentYear = studentYear;
    }

    public SeatInfo(int seatId, int roomId, int roomNumber, int hallId, String hallName, int seatNumber,
                    boolean occupied, String studentUserId, String studentName, String studentDept) {
        this(seatId, roomId, roomNumber, hallId, hallName, seatNumber, occupied, studentUserId, studentName, studentUserId, studentDept, null);
    }

    public int getSeatId() {
        return seatId;
    }

    public int getRoomId() {
        return roomId;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public int getHallId() {
        return hallId;
    }

    public String getHallName() {
        return hallName;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public String getSeatNumberDisplay() {
        return String.format("Seat %02d", seatNumber);
    }

    public boolean isOccupied() {
        return occupied;
    }

    public String getStatus() {
        return occupied ? "OCCUPIED" : "AVAILABLE";
    }

    public String getStudentUserId() {
        return studentUserId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getStudentRoll() {
        return studentRoll;
    }

    public String getStudentDept() {
        return studentDept;
    }

    public String getStudentYear() {
        return studentYear;
    }

    public String getOccupantBasicInfo() {
        if (!occupied) {
            return "Ready for allocation";
        }
        StringBuilder sb = new StringBuilder();
        if (studentName != null && !studentName.isEmpty()) {
            sb.append("Name: ").append(studentName).append("\n");
        }
        if (studentUserId != null) {
            sb.append("ID: ").append(studentUserId).append("\n");
        }
        if (studentDept != null && !studentDept.isEmpty()) {
            sb.append("Dept: ").append(studentDept);
        }
        return sb.toString().trim();
    }

    public String getOccupantDisplay(boolean isProvostView, String currentUserId) {
        if (!occupied) {
            return "Available";
        }
        if (currentUserId != null && currentUserId.equalsIgnoreCase(studentUserId)) {
            return "Occupied by You";
        }
        StringBuilder sb = new StringBuilder("Occupied — Student ID: ").append(studentUserId);
        if (studentName != null && !studentName.isEmpty()) {
            sb.append(" (").append(studentName).append(")");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return getSeatNumberDisplay() + " — " + getStatus();
    }
}
