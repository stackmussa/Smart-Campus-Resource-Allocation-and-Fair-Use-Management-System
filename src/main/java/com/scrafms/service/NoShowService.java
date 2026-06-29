package com.scrafms.service;

import com.scrafms.model.Booking;
import com.scrafms.repository.AuditLogRepository;
import com.scrafms.repository.BookingRepository;

import java.util.List;

/**
 * NoShowService — queries expired bookings and checks for manager override protection.
 *
 * GRASP Pattern: Information Expert — holds knowledge of no-show detection rules and audit log lookups
 * GoF Pattern: N/A
 * Layer: Business Logic (Service)
 *
 * UC: UC-07 (Receive No-Show Penalty)
 */
public class NoShowService {

    private final BookingRepository bookingRepo = new BookingRepository();
    private final AuditLogRepository auditRepo = new AuditLogRepository();

    public boolean validateNoManagerOverride(String bookingId) {
        // A booking is exempt from no-show processing if a manager has already acted on it (audit record exists)
        List<?> audits = auditRepo.findByActionId(bookingId);
        return audits.isEmpty(); // true = no override → safe to apply no-show penalty
    }

    public List<Booking> getExpiredBookings(int checkInWindowMinutes) {
        // Returns CONFIRMED bookings whose check-in window has closed with no checkInTime recorded
        // SQL: startTime + checkInWindowMinutes < NOW AND checkInTime IS NULL AND status = 'CONFIRMED'
        return bookingRepo.getExpiredUncheckedBookings(checkInWindowMinutes);
    }
}
