package com.scrafms.repository;

import com.scrafms.model.FairnessScore;
import com.scrafms.util.DatabaseConnection;

import java.sql.*;
import java.util.Optional;

public class FairnessScoreRepository {

    public Optional<FairnessScore> findByStudentId(int studentId) throws SQLException {
        String sql = "SELECT id, student_id, score, bookings_this_week, total_hours_this_week, last_updated FROM fairness_scores WHERE student_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public void saveOrUpdate(FairnessScore score) throws SQLException {
        String sql = "MERGE fairness_scores AS target USING (VALUES (?, ?, ?, ?, ?)) AS source (student_id, score, bookings_this_week, total_hours_this_week, last_updated) " +
                "ON target.student_id = source.student_id " +
                "WHEN MATCHED THEN UPDATE SET score=source.score, bookings_this_week=source.bookings_this_week, total_hours_this_week=source.total_hours_this_week, last_updated=source.last_updated " +
                "WHEN NOT MATCHED THEN INSERT (student_id, score, bookings_this_week, total_hours_this_week, last_updated) VALUES (source.student_id, source.score, source.bookings_this_week, source.total_hours_this_week, source.last_updated);";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, score.getStudentId());
            ps.setDouble(2, score.getScore());
            ps.setInt(3, score.getBookingsThisWeek());
            ps.setInt(4, score.getTotalHoursThisWeek());
            ps.setTimestamp(5, Timestamp.valueOf(score.getLastUpdated()));
            ps.executeUpdate();
        }
    }

    private FairnessScore mapRow(ResultSet rs) throws SQLException {
        FairnessScore f = new FairnessScore();
        f.setId(rs.getInt("id"));
        f.setStudentId(rs.getInt("student_id"));
        f.setScore(rs.getDouble("score"));
        f.setBookingsThisWeek(rs.getInt("bookings_this_week"));
        f.setTotalHoursThisWeek(rs.getInt("total_hours_this_week"));
        Timestamp ts = rs.getTimestamp("last_updated");
        if (ts != null) f.setLastUpdated(ts.toLocalDateTime());
        return f;
    }
}
