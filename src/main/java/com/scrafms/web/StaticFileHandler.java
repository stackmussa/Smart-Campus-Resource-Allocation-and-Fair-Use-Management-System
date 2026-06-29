package com.scrafms.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class StaticFileHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        String path = exchange.getRequestURI().getPath();
        if ("/".equals(path) || "".equals(path)) {
            exchange.getResponseHeaders().set("Location", "/login.html");
            exchange.sendResponseHeaders(302, -1);
            return;
        }
        String resourcePath = resolveResourcePath(path);

        InputStream is = getClass().getResourceAsStream(resourcePath);
        if (is == null) {
            // SPA fallback
            is = getClass().getResourceAsStream("/web/index.html");
        }
        if (is == null) {
            String body = "404 Not Found";
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(404, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
            return;
        }
        byte[] bytes = is.readAllBytes();
        is.close();
        exchange.getResponseHeaders().set("Content-Type", getMimeType(resourcePath));
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
    }

    private String resolveResourcePath(String path) {
        if (path == null || path.isEmpty() || path.equals("/")) return "/web/index.html";
        if (path.equals("/login"))                               return "/web/login.html";
        if (path.startsWith("/css/"))  return "/web" + path;
        if (path.startsWith("/js/"))   return "/web" + path;
        if (path.endsWith(".html"))    return "/web" + path;
        if (path.endsWith(".css"))     return "/web" + path;
        if (path.endsWith(".js"))      return "/web" + path;
        if (path.endsWith(".png"))     return "/web" + path;
        if (path.endsWith(".ico"))     return "/web" + path;
        return "/web/index.html";
    }

    private String getMimeType(String path) {
        if (path.endsWith(".html")) return "text/html; charset=utf-8";
        if (path.endsWith(".css"))  return "text/css; charset=utf-8";
        if (path.endsWith(".js"))   return "application/javascript; charset=utf-8";
        if (path.endsWith(".json")) return "application/json; charset=utf-8";
        if (path.endsWith(".png"))  return "image/png";
        if (path.endsWith(".ico"))  return "image/x-icon";
        return "text/plain; charset=utf-8";
    }
}
