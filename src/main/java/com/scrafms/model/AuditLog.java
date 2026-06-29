package com.scrafms.model;

import java.time.LocalDateTime;

public class AuditLog {

    private String auditId;
    private String actionId;
    private String color;
    private String reason;
    private LocalDateTime timestamp;

    public AuditLog() {}

    public String getAuditId() { return auditId; }
    public void setAuditId(String auditId) { this.auditId = auditId; }

    public String getActionId() { return actionId; }
    public void setActionId(String actionId) { this.actionId = actionId; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "AuditLog{" +
                "auditId='" + auditId + '\'' +
                ", actionId='" + actionId + '\'' +
                ", color='" + color + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
