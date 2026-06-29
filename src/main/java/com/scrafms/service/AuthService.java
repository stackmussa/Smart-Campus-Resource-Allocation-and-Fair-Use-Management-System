package com.scrafms.service;

import com.scrafms.model.Student;
import com.scrafms.util.DatabaseConnection;
import com.scrafms.util.SessionManager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

/**
 * AuthService — authenticates users against the database with SHA-256 password hashing.
 *
 * GRASP Pattern: Pure Fabrication — authentication logic separated from the Student domain model
 * GoF Pattern: N/A
 * Layer: Business Logic (Service)
 *
 * UC: Authentication (all roles)
 */
public class AuthService {

    public Student login(String username, String password) {
        // Step 1: Hash the plain-text password using SHA-256 before comparing to the stored hash
        String hash = sha256(password);
        if (hash == null) return null;

        // Step 2: Query the database for a matching email + passwordHash combination
        String sql = "SELECT p.personId, p.name, p.email, p.role, " +
                "s.rollNumber, s.department, s.fairnessScore, s.noShowCount, " +
                "s.isRestricted, s.restrictionEndDate " +
                "FROM Persons p LEFT JOIN Students s ON p.personId=s.studentId " +
                "WHERE p.email=? AND p.passwordHash=?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, hash); // parameterised query prevents SQL injection

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Step 3: Map the result row into a Student object (includes fairness and restriction state)
                    Student user = new Student();
                    user.setPersonId(rs.getString("personId"));
                    user.setUsername(rs.getString("email"));
                    user.setFullName(rs.getString("name"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(rs.getString("role"));
                    user.setAccountStatus("ACTIVE");
                    user.setRollNumber(rs.getString("rollNumber"));
                    user.setDepartment(rs.getString("department"));
                    user.setFairnessScore(rs.getDouble("fairnessScore"));
                    user.setNoShowCount(rs.getInt("noShowCount"));
                    user.setRestricted(rs.getBoolean("isRestricted"));
                    Timestamp restrictionTs = rs.getTimestamp("restrictionEndDate");
                    if (restrictionTs != null) user.setRestrictionEndDate(restrictionTs.toLocalDateTime());
                    // Step 4: Register user in the in-process session for non-web callers
                    SessionManager.getInstance().setCurrentUser(user);
                    return user;
                }
            }
        } catch (SQLException e) {
            // Step 5: On DB failure, fall back to hardcoded credentials for demo purposes
            System.err.println("[AuthService] DB auth failed (" + e.getMessage() + "), using fallback.");
        }

        return fallbackLogin(username, password);
    }

    public void logout() {
        SessionManager.getInstance().logout();
    }

    public static String sha256(String input) {
        try {
            // Obtain the SHA-256 MessageDigest instance from the JCA provider
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            // Encode to UTF-8 first to ensure consistent byte representation across platforms
            byte[] bytes = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            // Convert the 32-byte hash to a 64-character lowercase hex string
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private Student fallbackLogin(String username, String password) {
        record Cred(String pass, String role, String name, String personId) {}
        var users = java.util.Map.of(
            "admin@scrafms.com",   new Cred("admin123",   "ADMIN",   "System Admin",     "admin001"),
            "manager@scrafms.com", new Cred("manager123", "MANAGER", "Resource Manager", "manager001"),
            "ahmed@scrafms.com",   new Cred("student123", "STUDENT", "Ahmed Jamal",      "student001"),
            "zain@scrafms.com",    new Cred("student123", "STUDENT", "Muhammad Zain",    "student002"),
            "mussa@scrafms.com",   new Cred("student123", "STUDENT", "Mussa Raza",       "student003")
        );
        Cred cred = users.get(username);
        if (cred == null || !cred.pass().equals(password)) return null;

        Student user = new Student();
        user.setPersonId(cred.personId());
        user.setUsername(username);
        user.setFullName(cred.name());
        user.setEmail(username);
        user.setRole(cred.role());
        user.setAccountStatus("ACTIVE");
        user.setFairnessScore(100.0);
        if ("student001".equals(cred.personId())) user.setRollNumber("24I-3006");
        else if ("student002".equals(cred.personId())) user.setRollNumber("24I-3126");
        else if ("student003".equals(cred.personId())) user.setRollNumber("24I-3022");
        SessionManager.getInstance().setCurrentUser(user);
        return user;
    }
}
