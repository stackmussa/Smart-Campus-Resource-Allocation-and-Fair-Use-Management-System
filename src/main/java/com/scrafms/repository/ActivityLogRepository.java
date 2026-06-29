package com.scrafms.repository;

import com.scrafms.model.ActivityLog;
import com.scrafms.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ActivityLogRepository — records system activity events for auditing and reporting.
 *
 * GRASP Pattern: Pure Fabrication — cross-cutting logging concern with no domain home
 * GoF Pattern: N/A
 * Layer: Data Access
 *
 * UC: UC-MR-03 (Cancel Booking), UC-12 (Verify Check-In), UC-15 (Auto-Promote Queue Member)
 */
public class ActivityLogRepository {

    private static final int MAX_EVENT_TYPE_LEN = 200;

    public void logEvent(String eventType, String referenceId) {
        String combined = eventType + ":" + referenceId;
        if (combined.length() > MAX_EVENT_TYPE_LEN) {
            combined = combined.substring(0, MAX_EVENT_TYPE_LEN);
        }
        String sql = "INSERT INTO ActivityLogs(logId, eventType, timestamp) VALUES(?,?,?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, combined);
            ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[ActivityLogRepository] logEvent INSERT failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void testInsert() {
        String sql = "INSERT INTO ActivityLogs(logId, eventType, timestamp) VALUES(?,?,?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, "TEST_EVENT");
            ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();
            System.out.println("[ActivityLogRepository] testInsert SUCCESS");
        } catch (Exception e) {
            System.err.println("[ActivityLogRepository] testInsert FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<ActivityLog> findAll() {
        String sql = "SELECT logId, eventType, timestamp FROM ActivityLogs ORDER BY timestamp DESC";
        List<ActivityLog> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ActivityLogRepository] findAll error: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    public List<ActivityLog> findRecentByPrefix(String prefix, int limit) {
        String sql = "SELECT TOP " + limit + " logId, eventType, timestamp FROM ActivityLogs " +
                "WHERE eventType LIKE ? ORDER BY timestamp DESC";
        List<ActivityLog> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ActivityLogRepository] findRecentByPrefix error: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    private ActivityLog mapRow(ResultSet rs) throws SQLException {
        ActivityLog a = new ActivityLog();
        a.setLogId(rs.getString("logId"));
        a.setEventType(rs.getString("eventType"));
        Timestamp ts = rs.getTimestamp("timestamp");
        if (ts != null) a.setTimestamp(ts.toLocalDateTime());
        return a;
    }
}
