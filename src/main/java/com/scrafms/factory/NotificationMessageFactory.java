package com.scrafms.factory;

import com.scrafms.model.Notification;

/**
 * NotificationMessageFactory — creates typed Notification objects for each system event.
 *
 * GRASP Pattern: N/A
 * GoF Pattern: Factory (static factory) — centralises notification message construction; callers don't build messages inline
 * Layer: Business Logic (Factory)
 *
 * UC: UC-07 (Receive No-Show Penalty), UC-08 (Override Booking Decision), UC-15 (Auto-Promote Queue Member)
 */
public class NotificationMessageFactory {

    public static Notification createBookingConfirmation(String studentId, String roomName, String timeSlot) {
        String msg = "Your booking for " + roomName + " at " + timeSlot + " has been confirmed.";
        return new Notification(studentId, "BOOKING_CONFIRMED", msg);
    }

    public static Notification createBookingCancellation(String studentId, String roomName, String timeSlot) {
        String msg = "Your booking for " + roomName + " at " + timeSlot + " has been cancelled.";
        return new Notification(studentId, "BOOKING_CANCELLED", msg);
    }

    public static Notification createQueuePromotion(String studentId, String roomName, String timeSlot) {
        String msg = "A slot opened up! You have been promoted from the waiting list for " + roomName + " at " + timeSlot + ".";
        return new Notification(studentId, "QUEUE_PROMOTED", msg);
    }

    public static Notification createPenaltyNotice(String studentId, String reason) {
        String msg = "Your account has been restricted: " + reason;
        return new Notification(studentId, "PENALTY_NOTICE", msg);
    }

    public static Notification createViolationWarning(String studentId, String details) {
        String msg = "Warning: A policy violation has been recorded on your account. " + details;
        return new Notification(studentId, "VIOLATION_WARNING", msg);
    }
}
