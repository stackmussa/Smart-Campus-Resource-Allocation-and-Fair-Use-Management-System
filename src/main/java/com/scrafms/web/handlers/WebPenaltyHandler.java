package com.scrafms.web.handlers;

import com.scrafms.model.Student;
import com.scrafms.model.ViolationLog;
import com.scrafms.repository.ViolationLogRepository;
import com.scrafms.util.DatabaseConnection;
import com.scrafms.web.BaseHandler;
import com.scrafms.web.JsonUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * WebPenaltyHandler — handles penalty/violation log retrieval for students and admins.
 *
 * GRASP Pattern: N/A
 * GoF Pattern: N/A
 * Layer: Presentation (Web API)
 *
 * UC: UC-07 (Receive No-Show Penalty)
 */
public class WebPenaltyHandler extends BaseHandler {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final ViolationLogRepository violationRepo = new ViolationLogRepository();

    @Override
    protected void handleRequest(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if ("GET".equals(exchange.getRequestMethod())) {
            if (path.endsWith("/all")) {
                handleGetAllPenalties(exchange);
            } else {
                handleGetPenalties(exchange);
            }
        } else {
            JsonUtil.sendError(exchange, 405, "Method not allowed");
        }
    }

    private void handleGetPenalties(HttpExchange exchange) throws IOException {
        Student user = requireRole(exchange, "STUDENT");
        if (user == null) return;
        List<ViolationLog> violations = violationRepo.findByStudentId(user.getPersonId());
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < violations.size(); i++) {
            if (i > 0) sb.append(",");
            ViolationLog v = violations.get(i);
            sb.append("{\"logId\":\"").append(JsonUtil.escape(v.getLogId())).append("\"")
              .append(",\"violationType\":\"").append(JsonUtil.escape(v.getViolationType())).append("\"")
              .append(",\"occuredAt\":\"").append(v.getOccuredAt() != null ? v.getOccuredAt().format(FMT) : "").append("\"")
              .append(",\"penaltyApplied\":").append(v.getPenaltyApplied())
              .append("}");
        }
        sb.append("]");
        JsonUtil.sendSuccess(exchange, sb.toString());
    }

    private void handleGetAllPenalties(HttpExchange exchange) throws IOException {
        Student user = requireRole(exchange, "MANAGER", "ADMIN");
        if (user == null) return;
        String sql = "SELECT vl.logId, vl.studentId, vl.violationType, vl.occuredAt, " +
                "vl.penaltyApplied, p.name as studentName, " +
                "s.noShowCount, s.isRestricted " +
                "FROM ViolationLogs vl " +
                "JOIN Persons p ON vl.studentId = p.personId " +
                "JOIN Students s ON vl.studentId = s.studentId " +
                "ORDER BY vl.occuredAt DESC";
        StringBuilder sb = new StringBuilder("[");
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            boolean first = true;
            while (rs.next()) {
                if (!first) sb.append(",");
                first = false;
                String occuredAt = "";
                Timestamp ts = rs.getTimestamp("occuredAt");
                if (ts != null) occuredAt = ts.toLocalDateTime().format(FMT);
                sb.append("{\"logId\":\"").append(JsonUtil.escape(rs.getString("logId"))).append("\"")
                  .append(",\"studentId\":\"").append(JsonUtil.escape(rs.getString("studentId"))).append("\"")
                  .append(",\"studentName\":\"").append(JsonUtil.escape(rs.getString("studentName"))).append("\"")
                  .append(",\"violationType\":\"").append(JsonUtil.escape(rs.getString("violationType"))).append("\"")
                  .append(",\"occuredAt\":\"").append(occuredAt).append("\"")
                  .append(",\"penaltyApplied\":").append(rs.getDouble("penaltyApplied"))
                  .append(",\"noShowCount\":").append(rs.getInt("noShowCount"))
                  .append(",\"isRestricted\":").append(rs.getBoolean("isRestricted"))
                  .append("}");
            }
        } catch (SQLException e) {
            System.err.println("[WebPenaltyHandler] getAllPenalties error: " + e.getMessage());
            e.printStackTrace();
            JsonUtil.sendError(exchange, 500, "Database error: " + e.getMessage());
            return;
        }
        sb.append("]");
        JsonUtil.sendSuccess(exchange, sb.toString());
    }
}
