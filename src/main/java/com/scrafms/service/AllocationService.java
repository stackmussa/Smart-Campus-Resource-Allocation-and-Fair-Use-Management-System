package com.scrafms.service;

import com.scrafms.model.AllocationResult;
import com.scrafms.model.Booking;
import com.scrafms.repository.AllocationModeRepository;
import com.scrafms.repository.BookingRepository;
import com.scrafms.repository.TimeSlotRepository;
import com.scrafms.repository.WaitingQueueRepository;
import com.scrafms.strategy.AllocationStrategy;
import com.scrafms.strategy.ExamModeStrategy;
import com.scrafms.strategy.FCFSStrategy;
import com.scrafms.strategy.FairUseStrategy;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * AllocationService — executes room allocation by delegating to the active Strategy.
 *
 * GRASP Pattern: Pure Fabrication — invented service with no domain counterpart; coordinates allocation logic
 * GoF Pattern: Strategy — dynamically selects FairUseStrategy, FCFSStrategy, or ExamModeStrategy at runtime
 * Layer: Business Logic (Service)
 *
 * UC: UC-MR-01 (Request Study Room)
 */
public class AllocationService {

    private final BookingRepository bookingRepo = new BookingRepository();
    private final TimeSlotRepository slotRepo = new TimeSlotRepository();
    private final WaitingQueueRepository queueRepo = new WaitingQueueRepository();
    private final AllocationModeRepository modeRepo = new AllocationModeRepository();
    private AllocationStrategy strategy;

    public AllocationService() {
        loadStrategy();
    }

    private void loadStrategy() {
        String mode = modeRepo.getCurrentMode();
        this.strategy = resolveStrategy(mode);
    }

    private AllocationStrategy resolveStrategy(String mode) {
        return switch (mode) {
            case "FCFS" -> new FCFSStrategy();
            case "EXAM_MODE" -> new ExamModeStrategy();
            default -> new FairUseStrategy();
        };
    }

    public void setStrategy(AllocationStrategy strategy) {
        this.strategy = strategy;
    }

    public AllocationResult allocate(String studentId, String roomId, String slotId, double fairnessScore) {
        // Re-read active mode on every request — admin may switch strategy while server is running
        loadStrategy();
        try {
            // Step 1: Check real-time slot availability from the TimeSlots table
            boolean slotAvailable = bookingRepo.checkSlotAvailability(slotId);

            // Step 2: Delegate the allocation decision to the active Strategy
            // FAIR_USE → score-based; FCFS → first-request wins; EXAM_MODE → always blocked
            String decision = strategy.allocate(slotAvailable, fairnessScore);

            switch (decision) {
                case "CONFIRMED" -> {
                    // Step 3a: Slot is free — create booking record and mark slot unavailable
                    String bookingId = UUID.randomUUID().toString();
                    Booking b = new Booking();
                    b.setBookingId(bookingId);
                    b.setStudentId(studentId);
                    b.setRoomId(roomId);
                    b.setSlotId(slotId);
                    b.setStatus("CONFIRMED");
                    b.setStartTime(slotRepo.findById(slotId)
                            .map(ts -> ts.getStartTime()).orElse(LocalDateTime.now().plusDays(1)));
                    b.setEndTime(slotRepo.findById(slotId)
                            .map(ts -> ts.getEndTime()).orElse(LocalDateTime.now().plusDays(1).plusHours(2)));
                    b.setAllocationMode(strategy.getStrategyName());
                    bookingRepo.saveBooking(b);
                    // Mark slot unavailable so no second booking is created for this slot
                    slotRepo.updateAvailability(slotId, false);
                    return AllocationResult.confirmed(bookingId);
                }
                case "QUEUED" -> {
                    // Step 3b: Slot is occupied — add student to the waiting queue
                    // Guard: skip if student already has an active booking for this slot
                    if (bookingRepo.hasActiveBookingForSlot(studentId, slotId)) {
                        return AllocationResult.alreadyBooked();
                    }
                    // Guard: skip if student is already in the queue for this slot
                    if (queueRepo.checkDuplicateEntry(studentId, roomId, slotId)) {
                        int pos = queueRepo.getQueuePosition(studentId, slotId);
                        return AllocationResult.queued(null, pos);
                    }
                    // Insert into queue with fairness score as priority weight
                    queueRepo.addToQueue(studentId, roomId, slotId, fairnessScore);
                    int position = queueRepo.getQueuePosition(studentId, slotId);
                    return AllocationResult.queued(null, position);
                }
                // Step 3c: Exam mode is active — no bookings allowed
                case "EXAM_BLOCKED" -> { return AllocationResult.examBlocked(); }
                default -> { return AllocationResult.error("Unknown allocation decision: " + decision); }
            }
        } catch (Exception e) {
            return AllocationResult.error(e.getMessage());
        }
    }
}
