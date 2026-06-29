package com.scrafms.web;

import com.scrafms.model.Student;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public abstract class BaseHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Cookie");
        exchange.getResponseHeaders().set("Access-Control-Allow-Credentials", "true");

        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        try {
            handleRequest(exchange);
        } catch (Exception e) {
            System.err.println("[Handler] Error: " + e.getMessage());
            e.printStackTrace();
            try {
                JsonUtil.sendError(exchange, 500, "Internal server error");
            } catch (Exception ignored) {}
        }
    }

    protected abstract void handleRequest(HttpExchange exchange) throws IOException;

    protected Student requireAuth(HttpExchange exchange) throws IOException {
        Student user = WebSessionManager.getUserFromRequest(exchange);
        if (user == null) {
            JsonUtil.sendError(exchange, 401, "Not authenticated");
            return null;
        }
        return user;
    }

    protected Student requireRole(HttpExchange exchange, String... roles) throws IOException {
        Student user = requireAuth(exchange);
        if (user == null) return null;
        for (String role : roles) {
            if (role.equals(user.getRole())) return user;
        }
        JsonUtil.sendError(exchange, 403, "Insufficient permissions");
        return null;
    }
}
