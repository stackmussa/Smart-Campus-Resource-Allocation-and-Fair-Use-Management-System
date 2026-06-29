package com.scrafms.controller;

import com.scrafms.model.ValidationResult;
import com.scrafms.repository.ActivityLogRepository;
import com.scrafms.repository.BookingRepository;
import com.scrafms.repository.TimeSlotRepository;
import com.scrafms.service.BookingValidator;
import com.scrafms.service.WaitingQueueService;

import java.util.Optional;
import com.scrafms.model.Booking;

/**
 * CancellationController — handles booking cancellation and triggers queue promotion.
 *
 * GRASP Pattern: Controller — orchestrates cancellation system operation
 * GoF Pattern: N/A
 * Layer: Business Logic (Controller)
 *
 * UC: UC-MR-03 (Cancel Booking)
 */
public class CancellationController {

    private final BookingValidator validator = new BookingValidator();
    private final BookingRepository bookingRepo = new BookingRepository();
    private final TimeSlotRepository slotRepo = new TimeSlotRepository();
    private final WaitingQueueService queueService = new WaitingQueueService();
    private final ActivityLogRepository activityLog = new ActivityLogRepository();

    public ValidationResult cancelBooking(String bookingId, String studentId) {
        ValidationResult validation = validator.validateCancellation(bookingId, studentId);
        if (!validation.isValid()) return validation;

        Optional<Booking> opt = bookingRepo.findBookingById(bookingId);
        if (opt.isEmpty()) return ValidationResult.invalid("Booking not found.");
        Booking booking = opt.get();

        bookingRepo.updateStatus(bookingId, "CANCELLED");
        slotRepo.updateAvailability(booking.getSlotId(), true);
        queueService.promoteNext(booking.getSlotId());
        activityLog.logEvent("CANCELLATION", bookingId);
        return ValidationResult.valid();
    }
}
