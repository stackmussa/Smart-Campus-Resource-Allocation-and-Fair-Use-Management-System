package com.scrafms.model;

import java.time.LocalDateTime;

public class Notification {

    private String notifId;
    private String studentId;
    private String type;
    private String message;
    private LocalDateTime sentAt;
    private String deliveryStatus;

    public Notification() {}

    public Notification(String studentId, String type, String message) {
        this.studentId = studentId;
        this.type = type;
        this.message = message;
        this.sentAt = LocalDateTime.now();
        this.deliveryStatus = "PENDING";
    }

    public String getNotifId() { return notifId; }
    public void setNotifId(String notifId) { this.notifId = notifId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public String getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(String deliveryStatus) { this.deliveryStatus = deliveryStatus; }

    @Override
    public String toString() {
        return "Notification{" +
                "notifId='" + notifId + '\'' +
                ", studentId='" + studentId + '\'' +
                ", type='" + type + '\'' +
                ", deliveryStatus='" + deliveryStatus + '\'' +
                '}';
    }
}
