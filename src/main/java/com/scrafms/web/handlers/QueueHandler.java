package com.scrafms.web.handlers;

import com.scrafms.controller.WaitingListController;
import com.scrafms.model.AllocationResult;
import com.scrafms.model.Student;
import com.scrafms.model.WaitingQueueEntry;
import com.scrafms.repository.BookingRepository;
import com.scrafms.repository.TimeSlotRepository;
import com.scrafms.repository.WaitingQueueRepository;
import com.scrafms.service.WaitingQueueService;
import com.scrafms.web.BaseHandler;
import com.scrafms.web.JsonUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * QueueHandler — handles waiting queue retrieval, join, depth query, and manual process endpoints.
 *
 * GRASP Pattern: N/A
 * GoF Pattern: N/A
 * Layer: Presentation (Web API)
 *
 * UC: UC-MR-05 (Join Waiting List), UC-15 (Auto-Promote Queue Member)
 */
public class QueueHandler extends BaseHandler {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final WaitingQueueRepository queueRepo = new WaitingQueueRepository();
    private final BookingRepository bookingRepo = new BookingRepository();
    private final TimeSlotRepository slotRepo = new TimeSlotRepository();
    private final WaitingListController waitingListController = new WaitingListController();
    private final WaitingQueueService queueService = new WaitingQueueService();

    @Override
    protected void handleRequest(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if ("GET".equals(method) && (path.equals("/api/queue") || path.equals("/api/queue/"))) {
            handleGetQueue(exchange);
        } else if ("GET".equals(method) && path.startsWith("/api/queue/depth/")) {
            String slotId = path.substring("/api/queue/depth/".length());
            handleGetQueueDepth(exchange, slotId);
        } else if ("POST".equals(method) && path.endsWith("/join")) {
            handleJoinQueue(exchange);
        } else if ("POST".equals(method) && path.startsWith("/api/queue/process/")) {
            String slotId = path.substring("/api/queue/process/".length());
            handleProcessQueue(exchange, slotId);
        } else {
            JsonUtil.sendError(exchange, 404, "Not found");
        }
    }

    private void handleGetQueue(HttpExchange exchange) throws IOException {
        Student user = requireAuth(exchange);
        if (user == null) return;
        List<WaitingQueueEntry> entries;
        if ("STUDENT".equals(user.getRole())) {
            entries = queueRepo.findByStudentId(user.getPersonId());
        } else {
            entries = queueRepo.getAllEntries();
        }
        JsonUtil.sendSuccess(exchange, entriesToJson(entries));
    }

    private void handleGetQueueDepth(HttpExchange exchange, String slotId) throws IOException {
        Student user = requireAuth(exchange);
        if (user == null) return;
        if (slotId.isEmpty()) {
            JsonUtil.sendError(exchange, 400, "Missing slot ID");
            return;
        }
        int depth = queueRepo.getQueueDepthForSlot(slotId);
        JsonUtil.sendSuccess(exchange, "{\"depth\":" + depth + "}");
    }

    private void handleJoinQueue(HttpExchange exchange) throws IOException {
        Student user = requireRole(exchange, "STUDENT");
        if (user == null) return;
        String body = JsonUtil.readBody(exchange);
        String roomId = JsonUtil.parseField(body, "roomId");
        String slotId = JsonUtil.parseField(body, "slotId");
        if (roomId == null || slotId == null) {
            JsonUtil.sendError(exchange, 400, "Missing roomId or slotId");
            return;
        }
        AllocationResult result = waitingListController.joinWaitingList(user.getPersonId(), roomId, slotId);
        if ("QUEUED".equals(result.getStatus())) {
            JsonUtil.sendSuccess(exchange,
                "{\"status\":\"QUEUED\",\"position\":" + result.getQueuePosition() +
                ",\"message\":\"" + JsonUtil.escape(result.getMessage()) + "\"}");
        } else {
            JsonUtil.sendError(exchange, 400, result.getMessage() != null ? result.getMessage() : "Could not join queue");
        }
    }

    private void handleProcessQueue(HttpExchange exchange, String slotId) throws IOException {
        Student user = requireRole(exchange, "ADMIN", "MANAGER");
        if (user == null) return;
        if (slotId.isEmpty()) {
            JsonUtil.sendError(exchange, 400, "Missing slot ID");
            return;
        }
        // 1. Find any active booking for this slot and cancel it
        String existingBookingId = bookingRepo.findActiveBookingIdForSlot(slotId);
        if (existingBookingId != null) {
            bookingRepo.updateStatus(existingBookingId, "CANCELLED");
        }
        // 2. Free the slot
        slotRepo.updateAvailability(slotId, true);
        // 3. Promote next from queue
        String promotedBookingId = queueService.promoteNext(slotId);
        if (promotedBookingId != null) {
            JsonUtil.sendSuccess(exchange,
                "{\"promoted\":true,\"bookingId\":\"" + JsonUtil.escape(promotedBookingId) + "\"}");
        } else {
            JsonUtil.sendSuccess(exchange, "{\"promoted\":false,\"message\":\"No eligible queue entries\"}");
        }
    }

    private String entriesToJson(List<WaitingQueueEntry> entries) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < entries.size(); i++) {
            if (i > 0) sb.append(",");
            WaitingQueueEntry e = entries.get(i);
            sb.append("{\"queueId\":\"").append(JsonUtil.escape(e.getQueueId())).append("\"")
              .append(",\"studentId\":\"").append(JsonUtil.escape(e.getStudentId())).append("\"")
              .append(",\"studentName\":\"").append(JsonUtil.escape(e.getStudentName())).append("\"")
              .append(",\"roomId\":\"").append(JsonUtil.escape(e.getRoomId())).append("\"")
              .append(",\"roomName\":\"").append(JsonUtil.escape(e.getRoomName())).append("\"")
              .append(",\"slotId\":\"").append(JsonUtil.escape(e.getSlotId())).append("\"")
              .append(",\"position\":").append(e.getPosition())
              .append(",\"priorityScore\":").append(e.getPriorityScore())
              .append(",\"requestedAt\":\"").append(e.getRequestedAt() != null ? e.getRequestedAt().format(FMT) : "").append("\"")
              .append(",\"startTime\":\"").append(e.getStartTime() != null ? e.getStartTime().format(FMT) : "").append("\"")
              .append(",\"endTime\":\"").append(e.getEndTime() != null ? e.getEndTime().format(FMT) : "").append("\"")
              .append("}");
        }
        sb.append("]");
        return sb.toString();
    }
}
