package com.scrafms.web.handlers;

import com.scrafms.controller.AllocationController;
import com.scrafms.model.Student;
import com.scrafms.model.ValidationResult;
import com.scrafms.repository.AllocationModeRepository;
import com.scrafms.web.BaseHandler;
import com.scrafms.web.JsonUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

/**
 * AllocationHandler — handles GET/POST /api/allocation/mode HTTP endpoints.
 *
 * GRASP Pattern: N/A
 * GoF Pattern: N/A
 * Layer: Presentation (Web API)
 *
 * UC: UC-14 (Switch Allocation Mode)
 */
public class AllocationHandler extends BaseHandler {

    private final AllocationController allocationController = new AllocationController();
    private final AllocationModeRepository modeRepo = new AllocationModeRepository();

    @Override
    protected void handleRequest(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if ("GET".equals(method) && path.endsWith("/mode")) {
            handleGetMode(exchange);
        } else if ("POST".equals(method) && path.endsWith("/mode")) {
            handleSetMode(exchange);
        } else {
            JsonUtil.sendError(exchange, 404, "Not found");
        }
    }

    private void handleGetMode(HttpExchange exchange) throws IOException {
        Student user = requireRole(exchange, "ADMIN", "MANAGER");
        if (user == null) return;
        String mode = allocationController.fetchCurrentMode();
        Object changedAtObj = modeRepo.getLastChangedAt();
        String changedAt = changedAtObj != null ? changedAtObj.toString() : "";
        JsonUtil.sendSuccess(exchange,
            "{\"currentMode\":\"" + JsonUtil.escape(mode != null ? mode : "") + "\"" +
            ",\"lastChangedAt\":\"" + JsonUtil.escape(changedAt) + "\"}");
    }

    private void handleSetMode(HttpExchange exchange) throws IOException {
        Student user = requireRole(exchange, "ADMIN", "MANAGER");
        if (user == null) return;
        String body = JsonUtil.readBody(exchange);
        String mode = JsonUtil.parseField(body, "mode");
        if (mode == null || mode.trim().isEmpty()) {
            JsonUtil.sendError(exchange, 400, "Missing mode field");
            return;
        }
        ValidationResult result = allocationController.switchMode(user.getPersonId(), mode.trim());
        if (result.isValid()) {
            JsonUtil.sendSuccess(exchange, "{\"mode\":\"" + JsonUtil.escape(mode.trim()) + "\"}");
        } else {
            JsonUtil.sendError(exchange, 400, result.getErrorMessage());
        }
    }
}
