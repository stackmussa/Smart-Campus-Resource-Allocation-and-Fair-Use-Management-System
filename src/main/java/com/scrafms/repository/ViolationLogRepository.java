package com.scrafms.repository;

import com.scrafms.model.ViolationLog;
import com.scrafms.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ViolationLogRepository — persists and retrieves student no-show violation records.
 *
 * GRASP Pattern: Information Expert — owns violation log knowledge for penalty history display
 * GoF Pattern: N/A
 * Layer: Data Access
 *
 * UC: UC-07 (Receive No-Show Penalty)
 */
public class ViolationLogRepository {

    public void saveViolation(ViolationLog log) {
        String sql = "INSERT INTO ViolationLogs(logId, studentId, violationType, occuredAt, penaltyApplied) VALUES(?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, log.getLogId());
            ps.setString(2, log.getStudentId());
            ps.setString(3, log.getViolationType());
            ps.setTimestamp(4, log.getOccuredAt() != null ? Timestamp.valueOf(log.getOccuredAt()) : null);
            ps.setDouble(5, log.getPenaltyApplied());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[ViolationLogRepository] saveViolation error: " + e.getMessage());
        }
    }

    public List<ViolationLog> findByStudentId(String studentId) {
        String sql = "SELECT vl.logId, vl.studentId, vl.violationType, vl.occuredAt, vl.penaltyApplied, p.name as studentName " +
                "FROM ViolationLogs vl LEFT JOIN Persons p ON vl.studentId=p.personId " +
                "WHERE vl.studentId=? ORDER BY vl.occuredAt DESC";
        List<ViolationLog> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ViolationLog log = mapRow(rs);
                    log.setStudentName(rs.getString("studentName"));
                    result.add(log);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ViolationLogRepository] findByStudentId error: " + e.getMessage());
        }
        return result;
    }

    private ViolationLog mapRow(ResultSet rs) throws SQLException {
        ViolationLog v = new ViolationLog();
        v.setLogId(rs.getString("logId"));
        v.setStudentId(rs.getString("studentId"));
        v.setViolationType(rs.getString("violationType"));
        Timestamp ts = rs.getTimestamp("occuredAt");
        if (ts != null) v.setOccuredAt(ts.toLocalDateTime());
        v.setPenaltyApplied(rs.getDouble("penaltyApplied"));
        return v;
    }
}
