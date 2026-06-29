package com.scrafms.repository;

import com.scrafms.model.AuditLog;
import com.scrafms.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * AuditLogRepository — records administrative actions for traceability and override protection.
 *
 * GRASP Pattern: Pure Fabrication — logging concern extracted from domain classes
 * GoF Pattern: N/A
 * Layer: Data Access
 *
 * UC: UC-08 (Override Booking Decision), UC-11 (Register New Resource), UC-14 (Switch Allocation Mode)
 */
public class AuditLogRepository {

    public void saveAuditEntry(AuditLog log) {
        String sql = "INSERT INTO AuditLogs(auditId, actionId, color, reason, timestamp) VALUES(?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, log.getAuditId());
            ps.setString(2, log.getActionId());
            ps.setString(3, log.getColor());
            ps.setString(4, log.getReason());
            ps.setTimestamp(5, log.getTimestamp() != null ? Timestamp.valueOf(log.getTimestamp()) : null);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[AuditLogRepository] saveAuditEntry error: " + e.getMessage());
        }
    }

    public List<AuditLog> findByActionId(String actionId) {
        String sql = "SELECT auditId, actionId, color, reason, timestamp FROM AuditLogs WHERE actionId=? ORDER BY timestamp DESC";
        List<AuditLog> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, actionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[AuditLogRepository] findByActionId error: " + e.getMessage());
        }
        return result;
    }

    private AuditLog mapRow(ResultSet rs) throws SQLException {
        AuditLog a = new AuditLog();
        a.setAuditId(rs.getString("auditId"));
        a.setActionId(rs.getString("actionId"));
        a.setColor(rs.getString("color"));
        a.setReason(rs.getString("reason"));
        Timestamp ts = rs.getTimestamp("timestamp");
        if (ts != null) a.setTimestamp(ts.toLocalDateTime());
        return a;
    }
}
