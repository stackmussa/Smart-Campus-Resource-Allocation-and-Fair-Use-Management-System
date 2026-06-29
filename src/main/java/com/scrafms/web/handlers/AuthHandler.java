package com.scrafms.web.handlers;

import com.scrafms.controller.AuthController;
import com.scrafms.model.Student;
import com.scrafms.repository.StudentRepository;
import com.scrafms.web.BaseHandler;
import com.scrafms.web.JsonUtil;
import com.scrafms.web.WebSessionManager;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

/**
 * AuthHandler — handles /api/auth/login, /logout, and /me HTTP endpoints.
 *
 * GRASP Pattern: N/A
 * GoF Pattern: N/A
 * Layer: Presentation (Web API)
 *
 * UC: Authentication (all roles)
 */
public class AuthHandler extends BaseHandler {

    private final AuthController authController = new AuthController();

    @Override
    protected void handleRequest(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if ("POST".equals(method) && path.endsWith("/login")) {
            handleLogin(exchange);
        } else if ("POST".equals(method) && path.endsWith("/logout")) {
            handleLogout(exchange);
        } else if ("GET".equals(method) && path.endsWith("/me")) {
            handleMe(exchange);
        } else {
            JsonUtil.sendError(exchange, 404, "Not found");
        }
    }

    private synchronized void handleLogin(HttpExchange exchange) throws IOException {
        String body = JsonUtil.readBody(exchange);
        String email = JsonUtil.parseField(body, "email");
        String password = JsonUtil.parseField(body, "password");

        if (email == null || password == null) {
            JsonUtil.sendError(exchange, 400, "Missing email or password");
            return;
        }

        Student raw = authController.login(email, password);
        if (raw == null) {
            JsonUtil.sendError(exchange, 401, "Invalid credentials");
            return;
        }

        Student webUser = cloneStudent(raw);
        String token = WebSessionManager.createSession(webUser);

        exchange.getResponseHeaders().set("Set-Cookie", "session=" + token + "; Path=/; HttpOnly");
        JsonUtil.sendSuccess(exchange,
            "{\"token\":\"" + JsonUtil.escape(token) + "\"" +
            ",\"role\":\"" + JsonUtil.escape(webUser.getRole()) + "\"" +
            ",\"name\":\"" + JsonUtil.escape(webUser.getFullName()) + "\"" +
            ",\"personId\":\"" + JsonUtil.escape(webUser.getPersonId()) + "\"}");
    }

    private void handleLogout(HttpExchange exchange) throws IOException {
        String token = WebSessionManager.extractToken(exchange);
        WebSessionManager.destroySession(token);
        authController.logout();
        exchange.getResponseHeaders().set("Set-Cookie", "session=; Path=/; Max-Age=0");
        JsonUtil.sendSuccess(exchange, "\"logged out\"");
    }

    private void handleMe(HttpExchange exchange) throws IOException {
        Student sessionUser = requireAuth(exchange);
        if (sessionUser == null) return;
        String studentId = sessionUser.getPersonId();
        Student freshUser = new StudentRepository().findById(studentId).orElse(sessionUser);
        JsonUtil.sendSuccess(exchange, studentToJson(freshUser));
    }

    private String studentToJson(Student u) {
        return "{\"personId\":\"" + JsonUtil.escape(u.getPersonId()) + "\"" +
               ",\"name\":\"" + JsonUtil.escape(u.getFullName()) + "\"" +
               ",\"email\":\"" + JsonUtil.escape(u.getEmail()) + "\"" +
               ",\"role\":\"" + JsonUtil.escape(u.getRole()) + "\"" +
               ",\"fairnessScore\":" + u.getFairnessScore() +
               ",\"noShowCount\":" + u.getNoShowCount() +
               ",\"isRestricted\":" + u.isRestricted() + "}";
    }

    private Student cloneStudent(Student src) {
        Student copy = new Student();
        copy.setPersonId(src.getPersonId());
        copy.setUsername(src.getUsername());
        copy.setFullName(src.getFullName());
        copy.setEmail(src.getEmail());
        copy.setRole(src.getRole());
        copy.setAccountStatus(src.getAccountStatus());
        copy.setFairnessScore(src.getFairnessScore());
        copy.setNoShowCount(src.getNoShowCount());
        copy.setRestricted(src.isRestricted());
        copy.setRestrictionEndDate(src.getRestrictionEndDate());
        copy.setRollNumber(src.getRollNumber());
        copy.setDepartment(src.getDepartment());
        return copy;
    }
}
