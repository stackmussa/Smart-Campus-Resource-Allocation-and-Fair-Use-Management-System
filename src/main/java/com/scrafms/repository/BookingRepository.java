package com.scrafms.repository;

import com.scrafms.model.Booking;
import com.scrafms.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * BookingRepository — CRUD operations and queries for the Bookings table.
 *
 * GRASP Pattern: Information Expert — owns all knowledge about booking persistence
 * GoF Pattern: N/A
 * Layer: Data Access
 *
 * UC: UC-MR-01, UC-MR-03, UC-MR-05, UC-07, UC-08, UC-11, UC-12, UC-14, UC-15
 */
public class BookingRepository {

    public void saveBooking(Booking b) {
        String sql = "INSERT INTO Bookings(bookingId, studentId, roomId, slotId, status, startTime, endTime, checkInTime, allocationMode) VALUES(?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, b.getBookingId());
            ps.setString(2, b.getStudentId());
            ps.setString(3, b.getRoomId());
            ps.setString(4, b.getSlotId());
            ps.setString(5, b.getStatus());
            ps.setTimestamp(6, b.getStartTime() != null ? Timestamp.valueOf(b.getStartTime()) : null);
            ps.setTimestamp(7, b.getEndTime() != null ? Timestamp.valueOf(b.getEndTime()) : null);
            ps.setTimestamp(8, b.getCheckInTime() != null ? Timestamp.valueOf(b.getCheckInTime()) : null);
            ps.setString(9, b.getAllocationMode());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[BookingRepository] saveBooking error: " + e.getMessage());
        }
    }

    public List<Booking> findByStudent(String studentId) {
        String sql = "SELECT b.bookingId, b.studentId, b.roomId, b.slotId, b.status, b.startTime, b.endTime, b.checkInTime, b.allocationMode, r.name as roomName " +
                "FROM Bookings b LEFT JOIN StudyRooms r ON b.roomId=r.roomId " +
                "WHERE b.studentId=? ORDER BY b.startTime DESC";
        List<Booking> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Booking booking = mapRow(rs);
                    booking.setRoomName(rs.getString("roomName"));
                    result.add(booking);
                }
            }
        } catch (SQLException e) {
            System.err.println("[BookingRepository] findByStudent error: " + e.getMessage());
        }
        return result;
    }

    public Optional<Booking> findBookingById(String bookingId) {
        String sql = "SELECT b.bookingId, b.studentId, b.roomId, b.slotId, b.status, b.startTime, b.endTime, b.checkInTime, b.allocationMode, r.name as roomName " +
                "FROM Bookings b LEFT JOIN StudyRooms r ON b.roomId=r.roomId WHERE b.bookingId=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Booking booking = mapRow(rs);
                    booking.setRoomName(rs.getString("roomName"));
                    return Optional.of(booking);
                }
            }
        } catch (SQLException e) {
            System.err.println("[BookingRepository] findBookingById error: " + e.getMessage());
        }
        return Optional.empty();
    }

    public void deleteBooking(String bookingId) {
        String sql = "DELETE FROM Bookings WHERE bookingId=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookingId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[BookingRepository] deleteBooking error: " + e.getMessage());
        }
    }

    public void updateStatus(String bookingId, String status) {
        String sql = "UPDATE Bookings SET status=? WHERE bookingId=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, bookingId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[BookingRepository] updateStatus error: " + e.getMessage());
        }
    }

    public void updateCheckInTime(String bookingId, LocalDateTime time) {
        String sql = "UPDATE Bookings SET checkInTime=?, status='ACTIVE' WHERE bookingId=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, time != null ? Timestamp.valueOf(time) : null);
            ps.setString(2, bookingId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[BookingRepository] updateCheckInTime error: " + e.getMessage());
        }
    }

    public boolean checkSlotAvailability(String slotId) {
        String sql = "SELECT isAvailable FROM TimeSlots WHERE slotId=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, slotId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("isAvailable");
                }
            }
        } catch (SQLException e) {
            System.err.println("[BookingRepository] checkSlotAvailability error: " + e.getMessage());
        }
        return false;
    }

    public List<Booking> getExpiredUncheckedBookings(int checkInWindowMinutes) {
        String sql = "SELECT bookingId, studentId, roomId, slotId, status, startTime, endTime, checkInTime, allocationMode " +
                "FROM Bookings WHERE status='CONFIRMED' AND DATEADD(minute, ?, startTime) < GETDATE() AND checkInTime IS NULL";
        List<Booking> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, checkInWindowMinutes);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[BookingRepository] getExpiredUncheckedBookings error: " + e.getMessage());
        }
        return result;
    }

    public String getSlotIdForBooking(String bookingId) {
        String sql = "SELECT slotId FROM Bookings WHERE bookingId=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("slotId");
            }
        } catch (SQLException e) {
            System.err.println("[BookingRepository] getSlotIdForBooking error: " + e.getMessage());
        }
        return null;
    }

    public String findActiveBookingIdForSlot(String slotId) {
        String sql = "SELECT bookingId FROM Bookings WHERE slotId = ? AND status IN ('CONFIRMED','ACTIVE','PENDING')";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, slotId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("bookingId");
            }
        } catch (SQLException e) {
            System.err.println("[BookingRepository] findActiveBookingIdForSlot error: " + e.getMessage());
        }
        return null;
    }

    public boolean hasActiveBookingForSlot(String studentId, String slotId) {
        String sql = "SELECT COUNT(*) FROM Bookings WHERE studentId=? AND slotId=? " +
                "AND status IN ('CONFIRMED','ACTIVE','PENDING')";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setString(2, slotId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("[BookingRepository] hasActiveBookingForSlot error: " + e.getMessage());
        }
        return false;
    }

    public List<Booking> findConfirmedBookingsInWindow(String studentId) {
        String sql = "SELECT b.bookingId, b.studentId, b.roomId, b.slotId, b.status, " +
                "b.startTime, b.endTime, b.checkInTime, b.allocationMode, r.name as roomName " +
                "FROM Bookings b " +
                "JOIN StudyRooms r ON b.roomId = r.roomId " +
                "WHERE b.studentId = ? " +
                "AND b.status = 'CONFIRMED' " +
                "AND b.startTime >= DATEADD(minute, -60, GETDATE()) " +
                "AND b.startTime <= DATEADD(minute, 30, GETDATE()) " +
                "ORDER BY b.startTime ASC";
        List<Booking> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Booking booking = mapRow(rs);
                    booking.setRoomName(rs.getString("roomName"));
                    result.add(booking);
                }
            }
        } catch (SQLException e) {
            System.err.println("[BookingRepository] findConfirmedBookingsInWindow error: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    public List<Booking> findAllActive() {
        String sql = "SELECT b.bookingId, b.studentId, b.roomId, b.slotId, b.status, " +
                "b.startTime, b.endTime, b.checkInTime, b.allocationMode, " +
                "p.name as studentName, r.name as roomName " +
                "FROM Bookings b " +
                "JOIN Persons p ON b.studentId=p.personId " +
                "JOIN StudyRooms r ON b.roomId=r.roomId " +
                "WHERE b.status IN ('CONFIRMED','PENDING','ACTIVE') ORDER BY b.startTime DESC";
        List<Booking> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Booking booking = mapRow(rs);
                    booking.setRoomName(rs.getString("roomName"));
                    booking.setStudentName(rs.getString("studentName"));
                    result.add(booking);
                }
            }
        } catch (SQLException e) {
            System.err.println("[BookingRepository] findAllActive error: " + e.getMessage());
        }
        return result;
    }

    private Booking mapRow(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setBookingId(rs.getString("bookingId"));
        b.setStudentId(rs.getString("studentId"));
        b.setRoomId(rs.getString("roomId"));
        b.setSlotId(rs.getString("slotId"));
        b.setStatus(rs.getString("status"));
        Timestamp startTs = rs.getTimestamp("startTime");
        if (startTs != null) b.setStartTime(startTs.toLocalDateTime());
        Timestamp endTs = rs.getTimestamp("endTime");
        if (endTs != null) b.setEndTime(endTs.toLocalDateTime());
        Timestamp checkInTs = rs.getTimestamp("checkInTime");
        if (checkInTs != null) b.setCheckInTime(checkInTs.toLocalDateTime());
        b.setAllocationMode(rs.getString("allocationMode"));
        return b;
    }
}
