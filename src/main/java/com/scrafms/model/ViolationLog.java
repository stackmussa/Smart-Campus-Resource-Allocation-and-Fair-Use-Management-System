package com.scrafms.model;

import java.time.LocalDateTime;

public class ViolationLog {

    private String logId;
    private String studentId;
    private String violationType;
    private LocalDateTime occuredAt;
    private double penaltyApplied;

    // transient display field
    private String studentName;

    public ViolationLog() {}

    public String getLogId() { return logId; }
    public void setLogId(String logId) { this.logId = logId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getViolationType() { return violationType; }
    public void setViolationType(String violationType) { this.violationType = violationType; }

    public LocalDateTime getOccuredAt() { return occuredAt; }
    public void setOccuredAt(LocalDateTime occuredAt) { this.occuredAt = occuredAt; }

    public double getPenaltyApplied() { return penaltyApplied; }
    public void setPenaltyApplied(double penaltyApplied) { this.penaltyApplied = penaltyApplied; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    @Override
    public String toString() {
        return "ViolationLog{" +
                "logId='" + logId + '\'' +
                ", studentId='" + studentId + '\'' +
                ", violationType='" + violationType + '\'' +
                ", occuredAt=" + occuredAt +
                ", penaltyApplied=" + penaltyApplied +
                '}';
    }
}
