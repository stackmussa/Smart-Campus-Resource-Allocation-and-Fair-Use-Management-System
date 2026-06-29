package com.scrafms.model;

import java.time.LocalDateTime;

public class ActivityLog {

    private String logId;
    private String eventType;
    private LocalDateTime timestamp;

    public ActivityLog() {}

    public String getLogId() { return logId; }
    public void setLogId(String logId) { this.logId = logId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "ActivityLog{" +
                "logId='" + logId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
