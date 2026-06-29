package com.scrafms.repository;

import com.scrafms.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * AllocationModeRepository — persists and retrieves the active room allocation mode.
 *
 * GRASP Pattern: Information Expert — single source of truth for the current allocation strategy name
 * GoF Pattern: N/A
 * Layer: Data Access
 *
 * UC: UC-14 (Switch Allocation Mode)
 */
public class AllocationModeRepository {

    public String getCurrentMode() {
        String sql = "SELECT TOP 1 activeMode FROM AllocationConfigs ORDER BY changedAt DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String mode = rs.getString("activeMode");
                return (mode != null) ? mode : "FAIR_USE";
            }
        } catch (SQLException e) {
            System.err.println("[AllocationModeRepository] getCurrentMode error: " + e.getMessage());
        }
        return "FAIR_USE";
    }

    public void saveActiveMode(String mode) {
        String updateSql = "UPDATE AllocationConfigs SET activeMode=?, changedAt=? WHERE configId=(SELECT TOP 1 configId FROM AllocationConfigs)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setString(1, mode);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            int rows = ps.executeUpdate();
            if (rows == 0) {
                String insertSql = "INSERT INTO AllocationConfigs(configId, activeMode, changedAt) VALUES(?,?,?)";
                try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                    insertPs.setString(1, UUID.randomUUID().toString());
                    insertPs.setString(2, mode);
                    insertPs.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                    insertPs.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.err.println("[AllocationModeRepository] saveActiveMode error: " + e.getMessage());
        }
    }

    public boolean isSameMode(String mode) {
        return getCurrentMode().equals(mode);
    }

    public LocalDateTime getLastChangedAt() {
        String sql = "SELECT TOP 1 changedAt FROM AllocationConfigs ORDER BY changedAt DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                Timestamp ts = rs.getTimestamp("changedAt");
                if (ts != null) return ts.toLocalDateTime();
            }
        } catch (SQLException e) {
            System.err.println("[AllocationModeRepository] getLastChangedAt error: " + e.getMessage());
        }
        return LocalDateTime.now();
    }
}
