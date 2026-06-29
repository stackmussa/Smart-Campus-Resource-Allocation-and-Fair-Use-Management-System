package com.scrafms.web;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class JsonUtil {

    public static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public static void sendJson(HttpExchange ex, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    public static void sendError(HttpExchange ex, int status, String message) throws IOException {
        sendJson(ex, status, "{\"success\":false,\"error\":\"" + escape(message) + "\"}");
    }

    public static void sendSuccess(HttpExchange ex, String dataJson) throws IOException {
        sendJson(ex, 200, "{\"success\":true,\"data\":" + dataJson + "}");
    }

    public static String parseField(String json, String field) {
        if (json == null) return null;
        String search = "\"" + field + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return null;
        idx += search.length();
        while (idx < json.length() && (json.charAt(idx) == ' ' || json.charAt(idx) == ':')) idx++;
        if (idx >= json.length()) return null;
        if (json.charAt(idx) == '"') {
            idx++;
            StringBuilder sb = new StringBuilder();
            while (idx < json.length() && json.charAt(idx) != '"') {
                if (json.charAt(idx) == '\\') {
                    idx++;
                    if (idx < json.length()) sb.append(json.charAt(idx));
                } else {
                    sb.append(json.charAt(idx));
                }
                idx++;
            }
            return sb.toString();
        } else {
            int end = idx;
            while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}') end++;
            return json.substring(idx, end).trim();
        }
    }

    public static String readBody(HttpExchange ex) throws IOException {
        return new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }
}
