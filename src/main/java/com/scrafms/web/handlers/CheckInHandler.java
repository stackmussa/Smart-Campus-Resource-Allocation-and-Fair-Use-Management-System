package com.scrafms.web.handlers;

import com.scrafms.model.Booking;
import com.scrafms.model.Student;
import com.scrafms.model.ValidationResult;
import com.scrafms.repository.BookingRepository;
import com.scrafms.web.BaseHandler;
import com.scrafms.web.JsonUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * CheckInHandler — handles available-bookings query and check-in submission HTTP endpoints.
 *
 * GRASP Pattern: N/A
 * GoF Pattern: N/A
 * Layer: Presentation (Web API)
 *
 * UC: UC-12 (Verify Check-In)
 */
public class CheckInHandler extends BaseHandler {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final BookingRepository bookingRepo = new BookingRepository();
    private final com.scrafms.controller.CheckInController checkInController =
            new com.scrafms.controller.CheckInController();

    @Override
    protected void handleRequest(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if ("GET".equals(method) && path.endsWith("/available")) {
            handleAvailable(exchange);
        } else if ("POST".equals(method) && path.startsWith("/api/checkin/")
                   && !path.endsWith("/available")) {
            String bookingId = path.substring("/api/checkin/".length());
            handleCheckIn(exchange, bookingId);
        } else {
            JsonUtil.sendError(exchange, 404, "Not found");
        }
    }

    private void handleAvailable(HttpExchange exchange) throws IOException {
        Student user = requireRole(exchange, "STUDENT");
        if (user == null) return;
        List<Booking> bookings = bookingRepo.findConfirmedBookingsInWindow(user.getPersonId());
        JsonUtil.sendSuccess(exchange, bookingsToJson(bookings));
    }

    private void handleCheckIn(HttpExchange exchange, String bookingId) throws IOException {
        Student user = requireRole(exchange, "STUDENT");
        if (user == null) return;
        if (bookingId.isEmpty()) {
            JsonUtil.sendError(exchange, 400, "Missing booking ID");
            return;
        }
        ValidationResult result = checkInController.requestCheckIn(bookingId, user.getPersonId());
        if (result.isValid()) {
            JsonUtil.sendSuccess(exchange, "{\"message\":\"Check-in successful\"}");
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
              .append(",\"roomName\":\"").append(JsonUtil.escape(b.getRoomName())).append("\"")
              .append(",\"status\":\"").append(JsonUtil.escape(b.getStatus())).append("\"")
              .append(",\"startTime\":\"").append(b.getStartTime() != null ? b.getStartTime().format(FMT) : "").append("\"")
              .append(",\"endTime\":\"").append(b.getEndTime() != null ? b.getEndTime().format(FMT) : "").append("\"")
              .append("}");
        }
        sb.append("]");
        return sb.toString();
    }
}
