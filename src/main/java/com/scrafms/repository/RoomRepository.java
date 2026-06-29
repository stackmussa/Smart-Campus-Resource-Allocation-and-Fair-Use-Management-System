package com.scrafms.repository;

import com.scrafms.model.StudyRoom;
import com.scrafms.model.TimeSlot;
import com.scrafms.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * RoomRepository — queries study rooms and their time slots from the database.
 *
 * GRASP Pattern: Information Expert — owns room and slot availability knowledge
 * GoF Pattern: N/A
 * Layer: Data Access
 *
 * UC: UC-MR-01 (Request Study Room), UC-11 (Register New Resource)
 */
public class RoomRepository {

    public List<StudyRoom> findAllRooms() {
        String sql = "SELECT roomId, name, building, location, capacity, status, geofenceRadius FROM StudyRooms ORDER BY building, name";
        List<StudyRoom> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRoom(rs));
            }
        } catch (SQLException e) {
            System.err.println("[RoomRepository] findAllRooms error: " + e.getMessage());
        }
        return result;
    }

    public List<StudyRoom> findAvailableRooms() {
        String sql = "SELECT DISTINCT r.roomId, r.name, r.building, r.location, r.capacity, r.status, r.geofenceRadius " +
                "FROM StudyRooms r JOIN TimeSlots t ON r.roomId=t.roomId " +
                "WHERE t.isAvailable=1 AND r.status='AVAILABLE' ORDER BY r.building, r.name";
        List<StudyRoom> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRoom(rs));
            }
        } catch (SQLException e) {
            System.err.println("[RoomRepository] findAvailableRooms error: " + e.getMessage());
        }
        return result;
    }

    public Optional<StudyRoom> findByRoomId(String roomId) {
        String sql = "SELECT roomId, name, building, location, capacity, status, geofenceRadius FROM StudyRooms WHERE roomId=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRoom(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[RoomRepository] findByRoomId error: " + e.getMessage());
        }
        return Optional.empty();
    }

    public void updateSlotAvailability(String slotId, boolean available) {
        String sql = "UPDATE TimeSlots SET isAvailable=? WHERE slotId=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, available);
            ps.setString(2, slotId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[RoomRepository] updateSlotAvailability error: " + e.getMessage());
        }
    }

    public boolean checkAvailability(String roomId, String slotId) {
        String sql = "SELECT isAvailable FROM TimeSlots WHERE roomId=? AND slotId=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roomId);
            ps.setString(2, slotId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("isAvailable");
                }
            }
        } catch (SQLException e) {
            System.err.println("[RoomRepository] checkAvailability error: " + e.getMessage());
        }
        return false;
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
            System.err.println("[RoomRepository] getSlotsForRoom error: " + e.getMessage());
        }
        return result;
    }

    private StudyRoom mapRoom(ResultSet rs) throws SQLException {
        StudyRoom r = new StudyRoom();
        r.setRoomId(rs.getString("roomId"));
        r.setName(rs.getString("name"));
        r.setBuilding(rs.getString("building"));
        r.setLocation(rs.getString("location"));
        r.setCapacity(rs.getInt("capacity"));
        r.setStatus(rs.getString("status"));
        r.setGeofenceRadius(rs.getDouble("geofenceRadius"));
        return r;
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
