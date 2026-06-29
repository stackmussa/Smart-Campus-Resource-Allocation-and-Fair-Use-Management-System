package com.scrafms.repository;

import com.scrafms.model.StudyRoom;
import com.scrafms.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ResourceRepository {

    public void saveRoom(StudyRoom room) {
        String sql = "INSERT INTO StudyRooms(roomId, name, building, location, capacity, status, geofenceRadius) VALUES(?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, room.getRoomId());
            ps.setString(2, room.getName());
            ps.setString(3, room.getBuilding());
            ps.setString(4, room.getLocation());
            ps.setInt(5, room.getCapacity());
            ps.setString(6, room.getStatus());
            ps.setDouble(7, room.getGeofenceRadius());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[ResourceRepository] saveRoom error: " + e.getMessage());
        }
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
            System.err.println("[ResourceRepository] findByRoomId error: " + e.getMessage());
        }
        return Optional.empty();
    }

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
            System.err.println("[ResourceRepository] findAllRooms error: " + e.getMessage());
        }
        return result;
    }

    public void deactivateRoom(String roomId) {
        String sql = "UPDATE StudyRooms SET status='INACTIVE' WHERE roomId=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roomId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[ResourceRepository] deactivateRoom error: " + e.getMessage());
        }
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
}
