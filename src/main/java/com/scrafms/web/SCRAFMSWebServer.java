package com.scrafms.web;

import com.scrafms.web.handlers.*;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class SCRAFMSWebServer {

    private static final int PORT = 8080;

    public static void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/api/auth",       new AuthHandler());
        server.createContext("/api/bookings",   new BookingHandler());
        server.createContext("/api/rooms",      new RoomHandler());
        server.createContext("/api/queue",      new QueueHandler());
        server.createContext("/api/checkin",    new CheckInHandler());
        server.createContext("/api/allocation", new AllocationHandler());
        server.createContext("/api/penalties",  new WebPenaltyHandler());
        server.createContext("/api/override",   new OverrideHandler());
        server.createContext("/",               new StaticFileHandler());

        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();
        System.out.println("[SCRAFMS] Web server running at http://localhost:" + PORT);
    }
}
