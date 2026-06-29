package com.scrafms.model;

import java.time.LocalDateTime;

public class FairnessScore {
    private int id;
    private int studentId;
    private double score;
    private int bookingsThisWeek;
    private int totalHoursThisWeek;
    private LocalDateTime lastUpdated;

    public FairnessScore() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public int getBookingsThisWeek() { return bookingsThisWeek; }
    public void setBookingsThisWeek(int bookingsThisWeek) { this.bookingsThisWeek = bookingsThisWeek; }

    public int getTotalHoursThisWeek() { return totalHoursThisWeek; }
    public void setTotalHoursThisWeek(int totalHoursThisWeek) { this.totalHoursThisWeek = totalHoursThisWeek; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}
