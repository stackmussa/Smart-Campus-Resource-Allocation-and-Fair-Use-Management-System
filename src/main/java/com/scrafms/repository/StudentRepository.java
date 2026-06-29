package com.scrafms.repository;

import com.scrafms.model.Student;
import com.scrafms.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * StudentRepository — queries and updates student records in Persons and Students tables.
 *
 * GRASP Pattern: Information Expert — authoritative source for student state (score, restrictions, credentials)
 * GoF Pattern: N/A
 * Layer: Data Access
 *
 * UC: UC-07 (Receive No-Show Penalty), UC-15 (Auto-Promote Queue Member)
 */
public class StudentRepository {

    public Optional<Student> findById(String studentId) {
        String sql = "SELECT p.personId, p.name, p.email, p.role, s.rollNumber, s.department, " +
                "s.fairnessScore, s.noShowCount, s.isRestricted, s.restrictionEndDate " +
                "FROM Persons p JOIN Students s ON p.personId=s.studentId WHERE s.studentId=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapStudent(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[StudentRepository] findById error: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<Student> findByUsername(String email) {
        String sql = "SELECT p.personId, p.name, p.email, p.role, s.rollNumber, s.department, " +
                "s.fairnessScore, s.noShowCount, s.isRestricted, s.restrictionEndDate " +
                "FROM Persons p JOIN Students s ON p.personId=s.studentId WHERE p.email=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapStudent(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[StudentRepository] findByUsername error: " + e.getMessage());
        }
        return Optional.empty();
    }

    public void updatePenalty(String studentId, double penaltyValue) {
        String sql = "UPDATE Students " +
                "SET fairnessScore = fairnessScore - ?, " +
                "    noShowCount = noShowCount + 1 " +
                "WHERE studentId = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, penaltyValue);
            ps.setString(2, studentId);
            int rows = ps.executeUpdate();
            System.out.println("[StudentRepository] updatePenalty affected " + rows + " rows for studentId=" + studentId);
            if (rows == 0) {
                System.err.println("[StudentRepository] WARNING: 0 rows updated — studentId " + studentId + " not found");
            }
        } catch (SQLException e) {
            System.err.println("[StudentRepository] updatePenalty FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void applyRestriction(String studentId, LocalDateTime endDate) {
        String sql = "UPDATE Students SET isRestricted=1, restrictionEndDate=? WHERE studentId=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, endDate != null ? Timestamp.valueOf(endDate) : null);
            ps.setString(2, studentId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[StudentRepository] applyRestriction error: " + e.getMessage());
        }
    }

    public boolean verifyCredentials(String email, String passwordHash) {
        String sql = "SELECT COUNT(*) FROM Persons WHERE email=? AND passwordHash=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, passwordHash);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("[StudentRepository] verifyCredentials error: " + e.getMessage());
        }
        return false;
    }

    private Student mapStudent(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setPersonId(rs.getString("personId"));
        String name = rs.getString("name");
        s.setFullName(name);
        String email = rs.getString("email");
        s.setEmail(email);
        s.setUsername(email);
        s.setRole(rs.getString("role"));
        s.setRollNumber(rs.getString("rollNumber"));
        s.setDepartment(rs.getString("department"));
        s.setFairnessScore(rs.getDouble("fairnessScore"));
        s.setNoShowCount(rs.getInt("noShowCount"));
        s.setRestricted(rs.getBoolean("isRestricted"));
        Timestamp restrictionTs = rs.getTimestamp("restrictionEndDate");
        if (restrictionTs != null) {
            s.setRestrictionEndDate(restrictionTs.toLocalDateTime());
        }
        return s;
    }
}
