package com.scrafms.model;

import java.time.LocalDateTime;

public class Booking {

    private String bookingId;
    private String studentId;
    private String roomId;
    private String slotId;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime checkInTime;
    private String allocationMode;

    // transient display fields
    private String roomName;
    private String slotDisplay;
    private String studentName;

    public Booking() {}

    public boolean verifyOwnership(String studentId) {
        return this.studentId != null && this.studentId.equals(studentId);
    }

    public boolean verifyTimeWindow(int checkInWindowMinutes) {
        if (startTime == null) return false;
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(startTime.minusMinutes(checkInWindowMinutes))
                && !now.isAfter(startTime.plusMinutes(30));
    }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getSlotId() { return slotId; }
    public void setSlotId(String slotId) { this.slotId = slotId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public LocalDateTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalDateTime checkInTime) { this.checkInTime = checkInTime; }

    public String getAllocationMode() { return allocationMode; }
    public void setAllocationMode(String allocationMode) { this.allocationMode = allocationMode; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public String getSlotDisplay() { return slotDisplay; }
    public void setSlotDisplay(String slotDisplay) { this.slotDisplay = slotDisplay; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    @Override
    public String toString() {
        return "Booking{" +
                "bookingId='" + bookingId + '\'' +
                ", studentId='" + studentId + '\'' +
                ", roomId='" + roomId + '\'' +
                ", status='" + status + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}
