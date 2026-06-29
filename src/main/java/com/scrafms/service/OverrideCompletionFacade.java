package com.scrafms.service;

import com.scrafms.model.AuditLog;
import com.scrafms.model.Notification;
import com.scrafms.repository.AuditLogRepository;
import com.scrafms.repository.BookingRepository;
import com.scrafms.repository.NotificationRepository;
import com.scrafms.repository.TimeSlotRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import com.scrafms.model.Booking;

/**
 * OverrideCompletionFacade — simplifies the multi-step override completion flow behind a single method.
 *
 * GRASP Pattern: Low Coupling — reduces OverrideController coupling to individual repos/services
 * GoF Pattern: Facade — single method wraps: status update, slot release, notification, audit log, queue promotion
 * Layer: Business Logic (Service)
 *
 * UC: UC-08 (Override Booking Decision)
 */
public class OverrideCompletionFacade {

    private final BookingRepository bookingRepo = new BookingRepository();
    private final TimeSlotRepository slotRepo = new TimeSlotRepository();
    private final NotificationRepository notifRepo = new NotificationRepository();
    private final AuditLogRepository auditRepo = new AuditLogRepository();
    private final WaitingQueueService waitingQueueService = new WaitingQueueService();

    public boolean completeOverride(String bookingId, String adminId, String reason) {
        Optional<Booking> opt = bookingRepo.findBookingById(bookingId);
        if (opt.isEmpty()) return false;
        Booking booking = opt.get();

        bookingRepo.updateStatus(bookingId, "OVERRIDDEN");
        slotRepo.updateAvailability(booking.getSlotId(), true);

        Notification n = new Notification(booking.getStudentId(), "OVERRIDE",
                "Your booking has been overridden by an administrator. Reason: " + reason);
        notifRepo.saveNotification(n);

        AuditLog audit = new AuditLog();
        audit.setAuditId(UUID.randomUUID().toString());
        audit.setActionId(bookingId);
        audit.setColor("RED");
        audit.setReason(reason);
        audit.setTimestamp(LocalDateTime.now());
        auditRepo.saveAuditEntry(audit);

        waitingQueueService.promoteNext(booking.getSlotId());
        return true;
    }
}
