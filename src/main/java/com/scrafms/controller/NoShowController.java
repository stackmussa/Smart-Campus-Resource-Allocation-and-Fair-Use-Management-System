package com.scrafms.controller;

import com.scrafms.model.Booking;
import com.scrafms.model.FairnessPolicy;
import com.scrafms.repository.ActivityLogRepository;
import com.scrafms.repository.BookingRepository;
import com.scrafms.repository.FairnessPolicyRepository;
import com.scrafms.repository.TimeSlotRepository;
import com.scrafms.service.NoShowService;
import com.scrafms.service.PenaltyService;
import com.scrafms.service.WaitingQueueService;

import java.util.List;

/**
 * NoShowController — background daemon controller that detects expired check-in windows and applies penalties.
 *
 * GRASP Pattern: Controller — orchestrates the no-show detection system operation
 * GoF Pattern: N/A
 * Layer: Business Logic (Controller)
 *
 * UC: UC-07 (Receive No-Show Penalty)
 */
public class NoShowController {

    private final FairnessPolicyRepository policyRepo = new FairnessPolicyRepository();
    private final BookingRepository bookingRepo = new BookingRepository();
    private final TimeSlotRepository slotRepo = new TimeSlotRepository();
    private final NoShowService noShowService = new NoShowService();
    private final PenaltyService penaltyService = new PenaltyService();
    private final WaitingQueueService waitingQueueService = new WaitingQueueService();
    private final ActivityLogRepository activityLog = new ActivityLogRepository();

    public void processExpiredCheckInWindows() {
        FairnessPolicy policy = policyRepo.getCurrentPolicy();
        List<Booking> expired = noShowService.getExpiredBookings(policy.getCheckInWindowMinutes());
        for (Booking booking : expired) {
            if (noShowService.validateNoManagerOverride(booking.getBookingId())) {
                String slotId = booking.getSlotId();

                // a. Mark booking as NO_SHOW
                bookingRepo.updateStatus(booking.getBookingId(), "NO_SHOW");

                // b. Free the slot so promoteNext() can assign it
                slotRepo.updateAvailability(slotId, true);

                // c. Apply penalty (deduct score, increment noShowCount, run handler chain)
                penaltyService.applyPenalty(booking.getStudentId(), booking.getBookingId());

                // d. Promote the next eligible student from the waiting queue
                waitingQueueService.promoteNext(slotId);

                activityLog.logEvent("NO_SHOW_PROCESSED", booking.getBookingId());
            }
        }
    }
}
