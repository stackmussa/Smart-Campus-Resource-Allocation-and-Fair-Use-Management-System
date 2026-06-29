package com.scrafms.model;

import java.time.LocalDateTime;

public class WaitingQueueEntry {

    private String queueId;
    private String studentId;
    private String roomId;
    private String slotId;
    private int position;
    private LocalDateTime requestedAt;
    private double priorityScore;

    // transient display fields
    private String studentName;
    private String roomName;
    private java.time.LocalDateTime startTime;
    private java.time.LocalDateTime endTime;

    public WaitingQueueEntry() {}

    public String getQueueId() { return queueId; }
    public void setQueueId(String queueId) { this.queueId = queueId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getSlotId() { return slotId; }
    public void setSlotId(String slotId) { this.slotId = slotId; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }

    public double getPriorityScore() { return priorityScore; }
    public void setPriorityScore(double priorityScore) { this.priorityScore = priorityScore; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public java.time.LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(java.time.LocalDateTime startTime) { this.startTime = startTime; }

    public java.time.LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(java.time.LocalDateTime endTime) { this.endTime = endTime; }

    @Override
    public String toString() {
        return "WaitingQueueEntry{" +
                "queueId='" + queueId + '\'' +
                ", studentId='" + studentId + '\'' +
                ", slotId='" + slotId + '\'' +
                ", position=" + position +
                ", priorityScore=" + priorityScore +
                '}';
    }
}
