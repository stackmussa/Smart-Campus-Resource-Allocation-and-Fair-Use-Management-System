package com.scrafms.controller;

import com.scrafms.model.AllocationResult;
import com.scrafms.model.WaitingQueueEntry;
import com.scrafms.repository.ActivityLogRepository;
import com.scrafms.repository.BookingRepository;
import com.scrafms.repository.WaitingQueueRepository;
import com.scrafms.service.EligibilityService;
import com.scrafms.service.FairnessScoreCalculator;

import java.util.List;

/**
 * WaitingListController — manages student joining and querying of the waiting list.
 *
 * GRASP Pattern: Controller — entry point for the joinWaitingList system operation
 * GoF Pattern: N/A
 * Layer: Business Logic (Controller)
 *
 * UC: UC-MR-05 (Join Waiting List)
 */
public class WaitingListController {

    private final EligibilityService eligibilityService = new EligibilityService();
    private final FairnessScoreCalculator scoreCalc = new FairnessScoreCalculator();
    private final WaitingQueueRepository queueRepo = new WaitingQueueRepository();
    private final BookingRepository bookingRepo = new BookingRepository();
    private final ActivityLogRepository activityLog = new ActivityLogRepository();

    public AllocationResult joinWaitingList(String studentId, String roomId, String slotId) {
        String eligibility = eligibilityService.checkEligibility(studentId);
        if (!"ELIGIBLE".equals(eligibility)) return AllocationResult.restricted();

        // Bug 1 fix: prevent joining queue when an active booking already exists for this slot
        if (bookingRepo.hasActiveBookingForSlot(studentId, slotId)) {
            return AllocationResult.error("You already have a booking for this slot.");
        }

        if (queueRepo.checkDuplicateEntry(studentId, roomId, slotId)) {
            int pos = queueRepo.getQueuePosition(studentId, slotId);
            return AllocationResult.queued(null, pos);
        }

        double score = scoreCalc.calculateScore(studentId);
        queueRepo.addToQueue(studentId, roomId, slotId, score);
        int position = queueRepo.getQueuePosition(studentId, slotId);
        activityLog.logEvent("QUEUE_JOIN", studentId + ":" + slotId);
        return AllocationResult.queued(null, position);
    }

    public List<WaitingQueueEntry> getQueueEntries() {
        return queueRepo.getAllEntries();
    }

    public List<WaitingQueueEntry> getEntriesForStudent(String studentId) {
        return queueRepo.findByStudentId(studentId);
    }

    public List<WaitingQueueEntry> getEntriesForSlot(String slotId) {
        return queueRepo.getQueueForSlot(slotId, "position ASC");
    }
}
