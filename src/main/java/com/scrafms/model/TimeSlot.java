package com.scrafms.model;

import java.time.LocalDateTime;

public class TimeSlot {

    private String slotId;
    private String roomId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean available;

    public TimeSlot() {}

    public String getSlotId() { return slotId; }
    public void setSlotId(String slotId) { this.slotId = slotId; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public String toString() {
        return "TimeSlot{" +
                "slotId='" + slotId + '\'' +
                ", roomId='" + roomId + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", available=" + available +
                '}';
    }
}
