package com.scrafms.service;

import com.scrafms.model.Booking;
import com.scrafms.model.Notification;
import com.scrafms.model.Student;
import com.scrafms.model.WaitingQueueEntry;
import com.scrafms.repository.ActivityLogRepository;
import com.scrafms.repository.AllocationModeRepository;
import com.scrafms.repository.BookingRepository;
import com.scrafms.repository.NotificationRepository;
import com.scrafms.repository.StudentRepository;
import com.scrafms.repository.TimeSlotRepository;
import com.scrafms.repository.WaitingQueueRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * WaitingQueueService — promotes the next eligible student from the waiting queue when a slot opens.
 *
 * GRASP Pattern: Information Expert — knows queue state and promotion rules; Pure Fabrication — cross-cutting service
 * GoF Pattern: N/A
 * Layer: Business Logic (Service)
 *
 * UC: UC-MR-05 (Join Waiting List), UC-15 (Auto-Promote Queue Member)
 */
public class WaitingQueueService {

    private final WaitingQueueRepository queueRepo = new WaitingQueueRepository();
    private final BookingRepository bookingRepo = new BookingRepository();
    private final StudentRepository studentRepo = new StudentRepository();
    private final NotificationRepository notifRepo = new NotificationRepository();
    private final TimeSlotRepository slotRepo = new TimeSlotRepository();
    private final AllocationModeRepository allocationModeRepo = new AllocationModeRepository();
    private final ActivityLogRepository activityLog = new ActivityLogRepository();

    public String promoteNext(String slotId) {
        // Step 1: Guard — only promote if the slot is genuinely available (caller must free it first)
        if (!bookingRepo.checkSlotAvailability(slotId)) {
            System.err.println("[WaitingQueueService] promoteNext: slot " + slotId + " is not available, skipping");
            return null;
        }

        // Step 2: Resolve ordering — FCFS mode uses request time; FAIR_USE uses priority score
        String orderBy = resolveOrderBy();
        List<WaitingQueueEntry> queue = queueRepo.getQueueForSlot(slotId, orderBy);

        // Step 3: Walk the ordered queue and find the first eligible (non-restricted) student
        for (WaitingQueueEntry entry : queue) {
            String studentId = entry.getStudentId();
            Optional<Student> studentOpt = studentRepo.findById(studentId);
            if (studentOpt.isPresent()) {
                Student s = studentOpt.get();
                // Skip students who are currently restricted (no-show threshold reached)
                if (s.isRestricted()) {
                    LocalDateTime end = s.getRestrictionEndDate();
                    if (end == null || end.isAfter(LocalDateTime.now())) {
                        continue; // still restricted — try next in queue
                    }
                }
            }

            // Step 4: Create a CONFIRMED booking for the promoted student
            String bookingId = UUID.randomUUID().toString();
            Booking b = new Booking();
            b.setBookingId(bookingId);
            b.setStudentId(studentId);
            b.setRoomId(entry.getRoomId());
            b.setSlotId(slotId);
            b.setStatus("CONFIRMED");
            b.setStartTime(slotRepo.findById(slotId)
                    .map(ts -> ts.getStartTime()).orElse(LocalDateTime.now().plusDays(1)));
            b.setEndTime(slotRepo.findById(slotId)
                    .map(ts -> ts.getEndTime()).orElse(LocalDateTime.now().plusDays(1).plusHours(2)));
            b.setAllocationMode(allocationModeRepo.getCurrentMode());
            bookingRepo.saveBooking(b);

            // Step 5: Mark slot unavailable so no other booking races against this one
            slotRepo.updateAvailability(slotId, false);

            // Step 6: Remove the student from the waiting queue now that they have a booking
            queueRepo.removeFromQueue(entry.getQueueId());

            // Step 7: Notify the student of their promotion
            Notification n = new Notification(studentId, "PROMOTION",
                    "You have been promoted from the waiting list and your booking is now confirmed for slot " + slotId);
            notifRepo.saveNotification(n);
            activityLog.logEvent("AUTO_PROMOTE", bookingId);
            return bookingId;
        }
        // No eligible student found in the queue for this slot
        return null;
    }

    private String resolveOrderBy() {
        String mode = allocationModeRepo.getCurrentMode();
        return "FCFS".equals(mode) ? "requestedAt ASC" : "priorityScore DESC";
    }
}
