package com.scrafms.repository;

import com.scrafms.model.TimeSlot;
import com.scrafms.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TimeSlotRepository {

    public void saveSlot(TimeSlot slot) {
        String sql = "INSERT INTO TimeSlots(slotId, roomId, startTime, endTime, isAvailable) VALUES(?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, slot.getSlotId());
            ps.setString(2, slot.getRoomId());
            ps.setTimestamp(3, slot.getStartTime() != null ? Timestamp.valueOf(slot.getStartTime()) : null);
            ps.setTimestamp(4, slot.getEndTime() != null ? Timestamp.valueOf(slot.getEndTime()) : null);
            ps.setBoolean(5, slot.isAvailable());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[TimeSlotRepository] saveSlot error: " + e.getMessage());
        }
    }

    public List<TimeSlot> getSlotsForRoom(String roomId) {
        String sql = "SELECT slotId, roomId, startTime, endTime, isAvailable FROM TimeSlots WHERE roomId=? ORDER BY startTime";
        List<TimeSlot> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapSlot(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[TimeSlotRepository] getSlotsForRoom error: " + e.getMessage());
        }
        return result;
    }

    public void updateAvailability(String slotId, boolean available) {
        String sql = "UPDATE TimeSlots SET isAvailable=? WHERE slotId=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, available);
            ps.setString(2, slotId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[TimeSlotRepository] updateAvailability error: " + e.getMessage());
        }
    }

    public Optional<TimeSlot> findById(String slotId) {
        String sql = "SELECT slotId, roomId, startTime, endTime, isAvailable FROM TimeSlots WHERE slotId=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, slotId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapSlot(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[TimeSlotRepository] findById error: " + e.getMessage());
        }
        return Optional.empty();
    }

    private TimeSlot mapSlot(ResultSet rs) throws SQLException {
        TimeSlot t = new TimeSlot();
        t.setSlotId(rs.getString("slotId"));
        t.setRoomId(rs.getString("roomId"));
        Timestamp startTs = rs.getTimestamp("startTime");
        if (startTs != null) t.setStartTime(startTs.toLocalDateTime());
        Timestamp endTs = rs.getTimestamp("endTime");
        if (endTs != null) t.setEndTime(endTs.toLocalDateTime());
        t.setAvailable(rs.getBoolean("isAvailable"));
        return t;
    }
}
