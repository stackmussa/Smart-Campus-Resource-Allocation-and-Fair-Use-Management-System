package com.scrafms.model;

import java.time.LocalDateTime;

public class SlotQueueInfo {

    private String slotId;
    private String roomId;
    private String roomName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int queueDepth;

    public SlotQueueInfo() {}

    public String getSlotId() { return slotId; }
    public void setSlotId(String slotId) { this.slotId = slotId; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public int getQueueDepth() { return queueDepth; }
    public void setQueueDepth(int queueDepth) { this.queueDepth = queueDepth; }
}
