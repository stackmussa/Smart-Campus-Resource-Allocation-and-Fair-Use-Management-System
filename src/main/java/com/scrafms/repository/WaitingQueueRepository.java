package com.scrafms.repository;

import com.scrafms.model.WaitingQueueEntry;
import com.scrafms.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * WaitingQueueRepository — manages persistence of waiting queue entries.
 *
 * GRASP Pattern: Information Expert — owns all knowledge about queue ordering and membership
 * GoF Pattern: N/A
 * Layer: Data Access
 *
 * UC: UC-MR-05 (Join Waiting List), UC-15 (Auto-Promote Queue Member)
 */
public class WaitingQueueRepository {

    public void addToQueue(String studentId, String roomId, String slotId, double priorityScore) {
        int position = getNextPosition(slotId);
        String sql = "INSERT INTO WaitingQueueEntries(queueId, studentId, roomId, slotId, position, requestedAt, priorityScore) VALUES(?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, studentId);
            ps.setString(3, roomId);
            ps.setString(4, slotId);
            ps.setInt(5, position);
            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            ps.setDouble(7, priorityScore);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[WaitingQueueRepository] addToQueue error: " + e.getMessage());
        }
    }

    private int getNextPosition(String slotId) {
        String sql = "SELECT ISNULL(MAX(position),0)+1 FROM WaitingQueueEntries WHERE slotId=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, slotId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[WaitingQueueRepository] getNextPosition error: " + e.getMessage());
        }
        return 1;
    }

    public boolean checkDuplicateEntry(String studentId, String roomId, String slotId) {
        String sql = "SELECT COUNT(*) FROM WaitingQueueEntries WHERE studentId=? AND roomId=? AND slotId=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setString(2, roomId);
            ps.setString(3, slotId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("[WaitingQueueRepository] checkDuplicateEntry error: " + e.getMessage());
        }
        return false;
    }

    public List<WaitingQueueEntry> getQueueForSlot(String slotId, String orderBy) {
        String sql = "SELECT wq.queueId, wq.studentId, wq.roomId, wq.slotId, wq.position, wq.requestedAt, wq.priorityScore, p.name as studentName " +
                "FROM WaitingQueueEntries wq LEFT JOIN Persons p ON wq.studentId=p.personId " +
                "WHERE wq.slotId=? ORDER BY " + orderBy;
        List<WaitingQueueEntry> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, slotId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    WaitingQueueEntry entry = mapEntry(rs);
                    entry.setStudentName(rs.getString("studentName"));
                    result.add(entry);
                }
            }
        } catch (SQLException e) {
            System.err.println("[WaitingQueueRepository] getQueueForSlot error: " + e.getMessage());
        }
        return result;
    }

    public void removeFromQueue(String queueId) {
        String sql = "DELETE FROM WaitingQueueEntries WHERE queueId=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, queueId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[WaitingQueueRepository] removeFromQueue error: " + e.getMessage());
        }
    }

    public int getQueuePosition(String studentId, String slotId) {
        String sql = "SELECT position FROM WaitingQueueEntries WHERE studentId=? AND slotId=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setString(2, slotId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("position");
                }
            }
        } catch (SQLException e) {
            System.err.println("[WaitingQueueRepository] getQueuePosition error: " + e.getMessage());
        }
        return -1;
    }

    public List<com.scrafms.model.SlotQueueInfo> getSlotQueueInfos() {
        String sql = "SELECT t.slotId, t.roomId, r.name as roomName, t.startTime, t.endTime, " +
                "(SELECT COUNT(*) FROM WaitingQueueEntries wq WHERE wq.slotId=t.slotId) as queueDepth " +
                "FROM TimeSlots t JOIN StudyRooms r ON t.roomId=r.roomId ORDER BY r.name, t.startTime";
        List<com.scrafms.model.SlotQueueInfo> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                com.scrafms.model.SlotQueueInfo info = new com.scrafms.model.SlotQueueInfo();
                info.setSlotId(rs.getString("slotId"));
                info.setRoomId(rs.getString("roomId"));
                info.setRoomName(rs.getString("roomName"));
                Timestamp startTs = rs.getTimestamp("startTime");
                if (startTs != null) info.setStartTime(startTs.toLocalDateTime());
                Timestamp endTs = rs.getTimestamp("endTime");
                if (endTs != null) info.setEndTime(endTs.toLocalDateTime());
                info.setQueueDepth(rs.getInt("queueDepth"));
                result.add(info);
            }
        } catch (SQLException e) {
            System.err.println("[WaitingQueueRepository] getSlotQueueInfos error: " + e.getMessage());
        }
        return result;
    }

    public List<WaitingQueueEntry> findByStudentId(String studentId) {
        String sql = "SELECT wq.queueId, wq.studentId, wq.roomId, wq.slotId, wq.position, wq.requestedAt, wq.priorityScore, p.name as studentName " +
                "FROM WaitingQueueEntries wq LEFT JOIN Persons p ON wq.studentId=p.personId " +
                "WHERE wq.studentId=? ORDER BY wq.requestedAt DESC";
        List<WaitingQueueEntry> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    WaitingQueueEntry entry = mapEntry(rs);
                    entry.setStudentName(rs.getString("studentName"));
                    result.add(entry);
                }
            }
        } catch (SQLException e) {
            System.err.println("[WaitingQueueRepository] findByStudentId error: " + e.getMessage());
        }
        return result;
    }

    public List<WaitingQueueEntry> getAllEntries() {
        String sql = "SELECT wq.queueId, wq.studentId, wq.roomId, wq.slotId, " +
                "wq.position, wq.requestedAt, wq.priorityScore, " +
                "p.name as studentName, r.name as roomName, " +
                "ts.startTime, ts.endTime " +
                "FROM WaitingQueueEntries wq " +
                "LEFT JOIN Persons p ON wq.studentId = p.personId " +
                "LEFT JOIN StudyRooms r ON wq.roomId = r.roomId " +
                "LEFT JOIN TimeSlots ts ON wq.slotId = ts.slotId " +
                "ORDER BY wq.position ASC";
        List<WaitingQueueEntry> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                WaitingQueueEntry entry = mapEntry(rs);
                entry.setStudentName(rs.getString("studentName"));
                entry.setRoomName(rs.getString("roomName"));
                Timestamp startTs = rs.getTimestamp("startTime");
                if (startTs != null) entry.setStartTime(startTs.toLocalDateTime());
                Timestamp endTs = rs.getTimestamp("endTime");
                if (endTs != null) entry.setEndTime(endTs.toLocalDateTime());
                result.add(entry);
            }
        } catch (SQLException e) {
            System.err.println("[WaitingQueueRepository] getAllEntries error: " + e.getMessage());
        }
        return result;
    }

    public int getQueueDepthForSlot(String slotId) {
        String sql = "SELECT COUNT(*) FROM WaitingQueueEntries WHERE slotId = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, slotId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[WaitingQueueRepository] getQueueDepthForSlot error: " + e.getMessage());
        }
        return 0;
    }

    private WaitingQueueEntry mapEntry(ResultSet rs) throws SQLException {
        WaitingQueueEntry e = new WaitingQueueEntry();
        e.setQueueId(rs.getString("queueId"));
        e.setStudentId(rs.getString("studentId"));
        e.setRoomId(rs.getString("roomId"));
        e.setSlotId(rs.getString("slotId"));
        e.setPosition(rs.getInt("position"));
        Timestamp ts = rs.getTimestamp("requestedAt");
        if (ts != null) e.setRequestedAt(ts.toLocalDateTime());
        e.setPriorityScore(rs.getDouble("priorityScore"));
        return e;
    }
}
