package com.scrafms.model;

import java.time.LocalDateTime;

public class Student {

    private int id;
    private String personId;
    private String username;
    private String passwordHash;
    private String fullName;
    private String email;
    private String role;
    private String accountStatus;
    private double fairnessScore;
    private String rollNumber;
    private String department;
    private int noShowCount;
    private boolean restricted;
    private LocalDateTime restrictionEndDate;

    public Student() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPersonId() { return personId; }
    public void setPersonId(String personId) { this.personId = personId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }

    public double getFairnessScore() { return fairnessScore; }
    public void setFairnessScore(double fairnessScore) { this.fairnessScore = fairnessScore; }

    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public int getNoShowCount() { return noShowCount; }
    public void setNoShowCount(int noShowCount) { this.noShowCount = noShowCount; }

    public boolean isRestricted() { return restricted; }
    public void setRestricted(boolean restricted) { this.restricted = restricted; }

    public LocalDateTime getRestrictionEndDate() { return restrictionEndDate; }
    public void setRestrictionEndDate(LocalDateTime restrictionEndDate) { this.restrictionEndDate = restrictionEndDate; }

    @Override
    public String toString() {
        return "Student{" +
                "personId='" + personId + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", rollNumber='" + rollNumber + '\'' +
                ", department='" + department + '\'' +
                ", fairnessScore=" + fairnessScore +
                ", noShowCount=" + noShowCount +
                ", restricted=" + restricted +
                ", restrictionEndDate=" + restrictionEndDate +
                '}';
    }
}
