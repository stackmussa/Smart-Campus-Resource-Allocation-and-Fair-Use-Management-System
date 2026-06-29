package com.scrafms.controller;

import com.scrafms.command.OverrideBookingCommand;
import com.scrafms.model.Booking;
import com.scrafms.model.ValidationResult;
import com.scrafms.repository.ActivityLogRepository;
import com.scrafms.repository.BookingRepository;
import com.scrafms.service.OverrideValidator;

import java.util.List;

/**
 * OverrideController — initiates admin/manager booking overrides via the Command pattern.
 *
 * GRASP Pattern: Controller — entry point for the override system operation
 * GoF Pattern: N/A (delegates to OverrideBookingCommand)
 * Layer: Business Logic (Controller)
 *
 * UC: UC-08 (Override Booking Decision)
 */
public class OverrideController {

    private final OverrideValidator validator = new OverrideValidator();
    private final BookingRepository bookingRepo = new BookingRepository();
    private final ActivityLogRepository activityLog = new ActivityLogRepository();

    public ValidationResult initiateOverride(String bookingId, String adminId, String reason) {
        ValidationResult vr = validator.validateEligibility(bookingId, reason);
        if (!vr.isValid()) return vr;

        OverrideBookingCommand cmd = new OverrideBookingCommand(bookingId, adminId, reason);
        try {
            cmd.execute();
            activityLog.logEvent("OVERRIDE", adminId + ":" + bookingId);
            return ValidationResult.valid();
        } catch (Exception e) {
            return ValidationResult.invalid(e.getMessage());
        }
    }

    public List<Booking> loadAllBookings() {
        return bookingRepo.findAllActive();
    }
}
