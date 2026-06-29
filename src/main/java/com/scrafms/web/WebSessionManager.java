package com.scrafms.web;

import com.scrafms.model.Student;
import com.sun.net.httpserver.HttpExchange;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSessionManager — in-memory HTTP session store keyed by UUID tokens.
 *
 * GRASP Pattern: N/A
 * GoF Pattern: N/A
 * Layer: Presentation (Web)
 *
 * UC: All use cases (every authenticated API endpoint uses this class)
 */
public class WebSessionManager {

    private static final ConcurrentHashMap<String, Student> sessions = new ConcurrentHashMap<>();

    public static String createSession(Student user) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, user);
        return token;
    }

    public static Student getSession(String token) {
        if (token == null) return null;
        return sessions.get(token);
    }

    public static void destroySession(String token) {
        if (token != null) sessions.remove(token);
    }

    public static String extractToken(HttpExchange exchange) {
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookieHeader == null) return null;
        for (String part : cookieHeader.split(";")) {
            part = part.trim();
            if (part.startsWith("session=")) {
                return part.substring(8);
            }
        }
        return null;
    }

    public static Student getUserFromRequest(HttpExchange exchange) {
        return getSession(extractToken(exchange));
    }
}
