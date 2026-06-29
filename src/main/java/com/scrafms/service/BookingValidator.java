package com.scrafms.service;

import com.scrafms.model.Booking;
import com.scrafms.model.ValidationResult;
import com.scrafms.repository.BookingRepository;

import java.util.Optional;

/**
 * BookingValidator — validates preconditions for cancellation requests.
 *
 * GRASP Pattern: Pure Fabrication — validation logic extracted to prevent bloating BookingController
 * GoF Pattern: N/A
 * Layer: Business Logic (Service)
 *
 * UC: UC-MR-03 (Cancel Booking)
 */
public class BookingValidator {

    private final BookingRepository bookingRepo = new BookingRepository();

    public ValidationResult validateCancellation(String bookingId, String studentId) {
        Optional<Booking> opt = bookingRepo.findBookingById(bookingId);
        if (opt.isEmpty()) return ValidationResult.invalid("Booking not found.");
        Booking b = opt.get();
        if (!b.verifyOwnership(studentId)) return ValidationResult.invalid("You do not own this booking.");
        if ("ACTIVE".equals(b.getStatus())) return ValidationResult.invalid("Cannot cancel a booking that is already active.");
        if ("CANCELLED".equals(b.getStatus())) return ValidationResult.invalid("Booking is already cancelled.");
        return ValidationResult.valid();
    }
}
