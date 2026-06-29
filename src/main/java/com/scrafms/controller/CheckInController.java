package com.scrafms.controller;

import com.scrafms.model.Booking;
import com.scrafms.model.FairnessPolicy;
import com.scrafms.model.ValidationResult;
import com.scrafms.repository.ActivityLogRepository;
import com.scrafms.repository.BookingRepository;
import com.scrafms.repository.FairnessPolicyRepository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * CheckInController — verifies student check-in within the allowed time window.
 *
 * GRASP Pattern: Controller — entry point for the check-in system operation
 * GoF Pattern: Template Method — check-in validation follows a fixed sequence: ownership → status → time window
 * Layer: Business Logic (Controller)
 *
 * UC: UC-12 (Verify Check-In)
 */
public class CheckInController {

    private final BookingRepository bookingRepo = new BookingRepository();
    private final FairnessPolicyRepository policyRepo = new FairnessPolicyRepository();
    private final ActivityLogRepository activityLog = new ActivityLogRepository();

    public ValidationResult requestCheckIn(String bookingId, String studentId) {
        Optional<Booking> opt = bookingRepo.findBookingById(bookingId);
        if (opt.isEmpty()) return ValidationResult.invalid("Booking not found.");
        Booking booking = opt.get();

        if (!booking.verifyOwnership(studentId))
            return ValidationResult.invalid("You do not own this booking.");
        if ("ACTIVE".equals(booking.getStatus()))
            return ValidationResult.invalid("Already checked in.");
        if (!"CONFIRMED".equals(booking.getStatus()))
            return ValidationResult.invalid("Booking is not in CONFIRMED state.");

        FairnessPolicy policy = policyRepo.getCurrentPolicy();
        if (!booking.verifyTimeWindow(policy.getCheckInWindowMinutes()))
            return ValidationResult.invalid("Not within check-in window. Check-in opens " +
                    policy.getCheckInWindowMinutes() + " minutes before start time.");

        bookingRepo.updateCheckInTime(bookingId, LocalDateTime.now());
        activityLog.logEvent("CHECK_IN", bookingId);
        return ValidationResult.valid();
    }

    public Optional<Booking> findBooking(String bookingId) {
        return bookingRepo.findBookingById(bookingId);
    }
}
