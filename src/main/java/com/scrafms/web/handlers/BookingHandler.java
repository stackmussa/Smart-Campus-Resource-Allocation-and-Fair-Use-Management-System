package com.scrafms.web.handlers;

import com.scrafms.controller.BookingController;
import com.scrafms.controller.CancellationController;
import com.scrafms.model.AllocationResult;
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
 * BookingHandler — handles booking request, history retrieval, and cancellation HTTP endpoints.
 *
 * GRASP Pattern: N/A
 * GoF Pattern: N/A
 * Layer: Presentation (Web API)
 *
 * UC: UC-MR-01 (Request Study Room), UC-MR-03 (Cancel Booking)
 */
public class BookingHandler extends BaseHandler {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final BookingController bookingController = new BookingController();
    private final CancellationController cancellationController = new CancellationController();

    @Override
    protected void handleRequest(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if ("GET".equals(method) && path.endsWith("/history")) {
            handleHistory(exchange);
        } else if ("GET".equals(method) && (path.equals("/api/bookings") || path.equals("/api/bookings/"))) {
            handleGetBookings(exchange);
        } else if ("POST".equals(method) && path.endsWith("/request")) {
            handleBookingRequest(exchange);
        } else if ("DELETE".equals(method) && path.startsWith("/api/bookings/")) {
            String bookingId = path.substring("/api/bookings/".length());
            handleDelete(exchange, bookingId);
        } else {
            JsonUtil.sendError(exchange, 404, "Not found");
        }
    }

    private void handleGetBookings(HttpExchange exchange) throws IOException {
        Student user = requireAuth(exchange);
        if (user == null) return;
        List<Booking> bookings = bookingController.getStudentBookings(user.getPersonId());
        JsonUtil.sendSuccess(exchange, bookingsToJson(bookings));
    }

    private void handleHistory(HttpExchange exchange) throws IOException {
        Student user = requireRole(exchange, "STUDENT");
        if (user == null) return;
        List<Booking> bookings = bookingController.getStudentBookings(user.getPersonId());
        JsonUtil.sendSuccess(exchange, bookingsToJson(bookings));
    }

    private void handleBookingRequest(HttpExchange exchange) throws IOException {
        Student user = requireRole(exchange, "STUDENT");
        if (user == null) return;
        String body = JsonUtil.readBody(exchange);
        String roomId = JsonUtil.parseField(body, "roomId");
        String slotId = JsonUtil.parseField(body, "slotId");
        if (roomId == null || slotId == null) {
            JsonUtil.sendError(exchange, 400, "Missing roomId or slotId");
            return;
        }
        AllocationResult result = bookingController.requestRoom(user.getPersonId(), roomId, slotId);
        String status = result.getStatus();
        if ("CONFIRMED".equals(status) || "QUEUED".equals(status)
                || "EXAM_BLOCKED".equals(status) || "RESTRICTED".equals(status)
                || "ALREADY_BOOKED".equals(status)) {
            String bookingId = result.getBookingId() != null ? result.getBookingId() : "";
            JsonUtil.sendSuccess(exchange,
                "{\"status\":\"" + JsonUtil.escape(status) + "\"" +
                ",\"message\":\"" + JsonUtil.escape(result.getMessage()) + "\"" +
                ",\"bookingId\":\"" + JsonUtil.escape(bookingId) + "\"}");
        } else {
            JsonUtil.sendError(exchange, 400, result.getMessage());
        }
    }

    private void handleDelete(HttpExchange exchange, String bookingId) throws IOException {
        Student user = requireRole(exchange, "STUDENT");
        if (user == null) return;
        if (bookingId.isEmpty()) {
            JsonUtil.sendError(exchange, 400, "Missing booking ID");
            return;
        }
        ValidationResult result = cancellationController.cancelBooking(bookingId, user.getPersonId());
        if (result.isValid()) {
            JsonUtil.sendSuccess(exchange, "\"Booking cancelled\"");
        } else {
            JsonUtil.sendError(exchange, 400, result.getErrorMessage());
        }
    }

    private String bookingsToJson(List<Booking> bookings) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < bookings.size(); i++) {
            if (i > 0) sb.append(",");
            Booking b = bookings.get(i);
            sb.append("{\"bookingId\":\"").append(JsonUtil.escape(b.getBookingId())).append("\"")
              .append(",\"roomId\":\"").append(JsonUtil.escape(b.getRoomId())).append("\"")
              .append(",\"roomName\":\"").append(JsonUtil.escape(b.getRoomName())).append("\"")
              .append(",\"slotId\":\"").append(JsonUtil.escape(b.getSlotId())).append("\"")
              .append(",\"status\":\"").append(JsonUtil.escape(b.getStatus())).append("\"")
              .append(",\"startTime\":\"").append(b.getStartTime() != null ? b.getStartTime().format(FMT) : "").append("\"")
              .append(",\"endTime\":\"").append(b.getEndTime() != null ? b.getEndTime().format(FMT) : "").append("\"")
              .append(",\"allocationMode\":\"").append(JsonUtil.escape(b.getAllocationMode())).append("\"")
              .append("}");
        }
        sb.append("]");
        return sb.toString();
    }
}
