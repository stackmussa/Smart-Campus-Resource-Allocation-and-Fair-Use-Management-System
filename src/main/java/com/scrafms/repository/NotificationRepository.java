package com.scrafms.repository;

import com.scrafms.model.Notification;
import com.scrafms.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * NotificationRepository — persists and retrieves student notifications.
 *
 * GRASP Pattern: Information Expert — owns notification storage and retrieval logic
 * GoF Pattern: N/A
 * Layer: Data Access
 *
 * UC: UC-07 (Receive No-Show Penalty), UC-15 (Auto-Promote Queue Member)
 */
public class NotificationRepository {

    public void saveNotification(Notification n) {
        String sql = "INSERT INTO Notifications(notifId, studentId, type, message, sentAt, deliveryStatus) VALUES(?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String notifId = (n.getNotifId() != null) ? n.getNotifId() : UUID.randomUUID().toString();
            ps.setString(1, notifId);
            ps.setString(2, n.getStudentId());
            ps.setString(3, n.getType());
            ps.setString(4, n.getMessage());
            ps.setTimestamp(5, n.getSentAt() != null ? Timestamp.valueOf(n.getSentAt()) : Timestamp.valueOf(java.time.LocalDateTime.now()));
            ps.setString(6, n.getDeliveryStatus() != null ? n.getDeliveryStatus() : "PENDING");
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[NotificationRepository] saveNotification error: " + e.getMessage());
        }
    }

    public List<Notification> findByStudentId(String studentId) {
        String sql = "SELECT notifId, studentId, type, message, sentAt, deliveryStatus FROM Notifications WHERE studentId=? ORDER BY sentAt DESC";
        List<Notification> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[NotificationRepository] findByStudentId error: " + e.getMessage());
        }
        return result;
    }

    private Notification mapRow(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setNotifId(rs.getString("notifId"));
        n.setStudentId(rs.getString("studentId"));
        n.setType(rs.getString("type"));
        n.setMessage(rs.getString("message"));
        Timestamp ts = rs.getTimestamp("sentAt");
        if (ts != null) n.setSentAt(ts.toLocalDateTime());
        n.setDeliveryStatus(rs.getString("deliveryStatus"));
        return n;
    }
}
