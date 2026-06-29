package com.scrafms.model;

public class PenaltyContext {

    private String studentId;
    private String bookingId;
    private int noShowCount;
    private double currentFairnessScore;
    private FairnessPolicy policy;
    private boolean restrictionApplied;
    private String notificationMessage;

    public PenaltyContext() {}

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public int getNoShowCount() { return noShowCount; }
    public void setNoShowCount(int noShowCount) { this.noShowCount = noShowCount; }

    public double getCurrentFairnessScore() { return currentFairnessScore; }
    public void setCurrentFairnessScore(double currentFairnessScore) { this.currentFairnessScore = currentFairnessScore; }

    public FairnessPolicy getPolicy() { return policy; }
    public void setPolicy(FairnessPolicy policy) { this.policy = policy; }

    public boolean isRestrictionApplied() { return restrictionApplied; }
    public void setRestrictionApplied(boolean restrictionApplied) { this.restrictionApplied = restrictionApplied; }

    public String getNotificationMessage() { return notificationMessage; }
    public void setNotificationMessage(String notificationMessage) { this.notificationMessage = notificationMessage; }

    @Override
    public String toString() {
        return "PenaltyContext{" +
                "studentId='" + studentId + '\'' +
                ", bookingId='" + bookingId + '\'' +
                ", noShowCount=" + noShowCount +
                ", currentFairnessScore=" + currentFairnessScore +
                ", restrictionApplied=" + restrictionApplied +
                '}';
    }
}
