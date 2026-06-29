package com.scrafms;

import com.scrafms.controller.NoShowController;
import com.scrafms.repository.ActivityLogRepository;
import com.scrafms.web.SCRAFMSWebServer;


public class Main {

    public static void main(String[] args) throws Exception {
        new ActivityLogRepository().testInsert();
        startNoShowProcessor();
        SCRAFMSWebServer.start();
        System.out.println("[SCRAFMS] Press Ctrl+C to stop.");
        Thread.currentThread().join();
    }

    private static void startNoShowProcessor() {
        Thread t = new Thread(() -> {
            try {
                new NoShowController().processExpiredCheckInWindows();
            } catch (Exception e) {
                System.err.println("[Main] No-show processor error: " + e.getMessage());
            }
        });
        t.setDaemon(true);
        t.setName("no-show-processor");
        t.start();
    }
}
