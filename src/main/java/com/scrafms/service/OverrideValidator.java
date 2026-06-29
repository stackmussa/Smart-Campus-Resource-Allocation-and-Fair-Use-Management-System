package com.scrafms.service;

import com.scrafms.model.Booking;
import com.scrafms.model.ValidationResult;
import com.scrafms.repository.BookingRepository;

import java.util.Optional;

/**
 * OverrideValidator — checks that a booking exists and the override reason meets length requirements.
 *
 * GRASP Pattern: Information Expert — knows override eligibility rules
 * GoF Pattern: N/A
 * Layer: Business Logic (Service)
 *
 * UC: UC-08 (Override Booking Decision)
 */
public class OverrideValidator {

    private final BookingRepository bookingRepo = new BookingRepository();

    public ValidationResult validateEligibility(String bookingId, String reason) {
        if (bookingId == null || bookingId.isBlank()) return ValidationResult.invalid("Booking ID is required.");
        Optional<Booking> opt = bookingRepo.findBookingById(bookingId);
        if (opt.isEmpty()) return ValidationResult.invalid("Booking not found.");
        if (reason == null || reason.trim().length() < 10)
            return ValidationResult.invalid("Override reason must be at least 10 characters.");
        if (reason.trim().length() > 500)
            return ValidationResult.invalid("Override reason must not exceed 500 characters.");
        return ValidationResult.valid();
    }
}
