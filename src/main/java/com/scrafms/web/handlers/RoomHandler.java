package com.scrafms.web.handlers;

import com.scrafms.controller.ResourceController;
import com.scrafms.model.Student;
import com.scrafms.model.StudyRoom;
import com.scrafms.model.TimeSlot;
import com.scrafms.model.ValidationResult;
import com.scrafms.repository.RoomRepository;
import com.scrafms.web.BaseHandler;
import com.scrafms.web.JsonUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * RoomHandler — handles room listing, slot querying, and room creation HTTP endpoints.
 *
 * GRASP Pattern: N/A
 * GoF Pattern: N/A
 * Layer: Presentation (Web API)
 *
 * UC: UC-11 (Register New Resource)
 */
public class RoomHandler extends BaseHandler {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final RoomRepository roomRepo = new RoomRepository();
    private final ResourceController resourceController = new ResourceController();

    @Override
    protected void handleRequest(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if ("GET".equals(method) && (path.equals("/api/rooms") || path.equals("/api/rooms/"))) {
            handleGetRooms(exchange);
        } else if ("GET".equals(method) && path.endsWith("/slots")) {
            handleGetSlots(exchange, path);
        } else if ("POST".equals(method) && (path.equals("/api/rooms") || path.equals("/api/rooms/"))) {
            handleCreateRoom(exchange);
        } else {
            JsonUtil.sendError(exchange, 404, "Not found");
        }
    }

    private void handleGetRooms(HttpExchange exchange) throws IOException {
        Student user = requireAuth(exchange);
        if (user == null) return;
        List<StudyRoom> rooms = roomRepo.findAllRooms();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < rooms.size(); i++) {
            if (i > 0) sb.append(",");
            StudyRoom r = rooms.get(i);
            sb.append("{\"roomId\":\"").append(JsonUtil.escape(r.getRoomId())).append("\"")
              .append(",\"name\":\"").append(JsonUtil.escape(r.getName())).append("\"")
              .append(",\"building\":\"").append(JsonUtil.escape(r.getBuilding())).append("\"")
              .append(",\"location\":\"").append(JsonUtil.escape(r.getLocation())).append("\"")
              .append(",\"capacity\":").append(r.getCapacity())
              .append(",\"status\":\"").append(JsonUtil.escape(r.getStatus())).append("\"")
              .append(",\"geofenceRadius\":").append(r.getGeofenceRadius())
              .append("}");
        }
        sb.append("]");
        JsonUtil.sendSuccess(exchange, sb.toString());
    }

    private void handleGetSlots(HttpExchange exchange, String path) throws IOException {
        Student user = requireAuth(exchange);
        if (user == null) return;
        // path = /api/rooms/{roomId}/slots → parts[3] is roomId
        String[] parts = path.split("/");
        if (parts.length < 4) {
            JsonUtil.sendError(exchange, 400, "Missing room ID");
            return;
        }
        String roomId = parts[3];
        List<TimeSlot> slots = roomRepo.getSlotsForRoom(roomId);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < slots.size(); i++) {
            if (i > 0) sb.append(",");
            TimeSlot t = slots.get(i);
            sb.append("{\"slotId\":\"").append(JsonUtil.escape(t.getSlotId())).append("\"")
              .append(",\"roomId\":\"").append(JsonUtil.escape(t.getRoomId())).append("\"")
              .append(",\"startTime\":\"").append(t.getStartTime() != null ? t.getStartTime().format(FMT) : "").append("\"")
              .append(",\"endTime\":\"").append(t.getEndTime() != null ? t.getEndTime().format(FMT) : "").append("\"")
              .append(",\"isAvailable\":").append(t.isAvailable())
              .append("}");
        }
        sb.append("]");
        JsonUtil.sendSuccess(exchange, sb.toString());
    }

    private void handleCreateRoom(HttpExchange exchange) throws IOException {
        Student user = requireRole(exchange, "ADMIN", "MANAGER");
        if (user == null) return;
        String body = JsonUtil.readBody(exchange);
        String roomId = JsonUtil.parseField(body, "roomId");
        String name = JsonUtil.parseField(body, "name");
        String building = JsonUtil.parseField(body, "building");
        String location = JsonUtil.parseField(body, "location");
        String capacityStr = JsonUtil.parseField(body, "capacity");
        if (roomId == null || name == null || building == null || capacityStr == null) {
            JsonUtil.sendError(exchange, 400, "Missing required fields: roomId, name, building, capacity");
            return;
        }
        int capacity;
        try {
            capacity = Integer.parseInt(capacityStr.trim());
        } catch (NumberFormatException e) {
            JsonUtil.sendError(exchange, 400, "Invalid capacity value");
            return;
        }
        ValidationResult result = resourceController.registerResource(
            roomId, name, building,
            location != null ? location : "",
            capacity, user.getPersonId());
        if (result.isValid()) {
            JsonUtil.sendSuccess(exchange, "{\"roomId\":\"" + JsonUtil.escape(roomId) + "\"}");
        } else {
            JsonUtil.sendError(exchange, 400, result.getErrorMessage());
        }
    }
}
