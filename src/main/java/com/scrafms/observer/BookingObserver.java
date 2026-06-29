package com.scrafms.observer;

import com.scrafms.model.Booking;

/**
 * BookingObserver — observer interface for booking lifecycle events.
 *
 * GRASP Pattern: N/A
 * GoF Pattern: Observer (interface) — subjects notify implementors on booking creation, cancellation, confirmation
 * Layer: Business Logic (Observer)
 *
 * UC: UC-MR-03 (Cancel Booking), UC-07 (Receive No-Show Penalty)
 */
public interface BookingObserver {
    void onBookingCreated(Booking booking);
    void onBookingCancelled(Booking booking);
    void onBookingConfirmed(Booking booking);
}
