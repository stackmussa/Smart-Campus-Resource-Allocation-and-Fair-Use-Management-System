package com.scrafms.web.handlers;

import com.scrafms.controller.OverrideController;
import com.scrafms.model.Booking;
import com.scrafms.model.Student;
import com.scrafms.model.ValidationResult;
import com.scrafms.web.BaseHandler;
import com.scrafms.web.JsonUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * OverrideHandler — handles booking override initiation and active-bookings listing HTTP endpoints.
 *
 * GRASP Pattern: N/A
 * GoF Pattern: N/A
 * Layer: Presentation (Web API)
 *
 * UC: UC-08 (Override Booking Decision)
 */
public class OverrideHandler extends BaseHandler {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final OverrideController overrideController = new OverrideController();

    @Override
    protected void handleRequest(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if ("GET".equals(method) && path.endsWith("/bookings")) {
            handleGetBookings(exchange);
        } else if ("POST".equals(method)) {
            handleOverride(exchange);
        } else {
            JsonUtil.sendError(exchange, 404, "Not found");
        }
    }

    private void handleGetBookings(HttpExchange exchange) throws IOException {
        Student user = requireRole(exchange, "ADMIN", "MANAGER");
        if (user == null) return;
        List<Booking> bookings = overrideController.loadAllBookings();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < bookings.size(); i++) {
            if (i > 0) sb.append(",");
            Booking b = bookings.get(i);
            sb.append("{\"bookingId\":\"").append(JsonUtil.escape(b.getBookingId())).append("\"")
              .append(",\"studentId\":\"").append(JsonUtil.escape(b.getStudentId())).append("\"")
              .append(",\"studentName\":\"").append(JsonUtil.escape(b.getStudentName())).append("\"")
              .append(",\"roomId\":\"").append(JsonUtil.escape(b.getRoomId())).append("\"")
              .append(",\"roomName\":\"").append(JsonUtil.escape(b.getRoomName())).append("\"")
              .append(",\"status\":\"").append(JsonUtil.escape(b.getStatus())).append("\"")
              .append(",\"startTime\":\"").append(b.getStartTime() != null ? b.getStartTime().format(FMT) : "").append("\"")
              .append(",\"endTime\":\"").append(b.getEndTime() != null ? b.getEndTime().format(FMT) : "").append("\"")
              .append("}");
        }
        sb.append("]");
        JsonUtil.sendSuccess(exchange, sb.toString());
    }

    private void handleOverride(HttpExchange exchange) throws IOException {
        Student user = requireRole(exchange, "ADMIN", "MANAGER");
        if (user == null) return;
        String body = JsonUtil.readBody(exchange);
        String bookingId = JsonUtil.parseField(body, "bookingId");
        String reason = JsonUtil.parseField(body, "reason");
        if (bookingId == null || bookingId.isEmpty()) {
            JsonUtil.sendError(exchange, 400, "Missing bookingId");
            return;
        }
        ValidationResult result = overrideController.initiateOverride(
            bookingId, user.getPersonId(), reason != null ? reason : "");
        if (result.isValid()) {
            JsonUtil.sendSuccess(exchange, "{\"bookingId\":\"" + JsonUtil.escape(bookingId) + "\"}");
        } else {
            JsonUtil.sendError(exchange, 400, result.getErrorMessage());
        }
    }
}
