package com.scrafms.observer;

import com.scrafms.model.Booking;

/**
 * NotificationService — concrete BookingObserver that sends notifications on booking events.
 *
 * GRASP Pattern: N/A
 * GoF Pattern: Observer (concrete observer) — reacts to booking lifecycle events to dispatch notifications
 * Layer: Business Logic (Observer)
 *
 * UC: UC-MR-03 (Cancel Booking), UC-07 (Receive No-Show Penalty)
 */
public class NotificationService implements BookingObserver {

    @Override
    public void onBookingCreated(Booking booking) {
        // TODO: send booking confirmation notification to student
    }

    @Override
    public void onBookingCancelled(Booking booking) {
        // TODO: send cancellation notification and trigger waiting queue promotion
    }

    @Override
    public void onBookingConfirmed(Booking booking) {
        // TODO: send confirmation notification to student
    }
}
