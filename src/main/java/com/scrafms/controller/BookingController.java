package com.scrafms.controller;

import com.scrafms.model.AllocationResult;
import com.scrafms.model.Booking;
import com.scrafms.repository.ActivityLogRepository;
import com.scrafms.repository.BookingRepository;
import com.scrafms.service.AllocationService;
import com.scrafms.service.EligibilityService;
import com.scrafms.service.FairnessScoreCalculator;

import java.util.List;

/**
 * BookingController — handles study room booking requests and retrieval.
 *
 * GRASP Pattern: Controller — receives system operation requestRoom() from the UI/web layer
 * GoF Pattern: N/A
 * Layer: Business Logic (Controller)
 *
 * UC: UC-MR-01 (Request Study Room), UC-MR-03 (Cancel Booking — retrieval side)
 */
public class BookingController {

    private final EligibilityService eligibilityService = new EligibilityService();
    private final FairnessScoreCalculator scoreCalc = new FairnessScoreCalculator();
    private final AllocationService allocationService = new AllocationService();
    private final ActivityLogRepository activityLog = new ActivityLogRepository();
    private final BookingRepository bookingRepo = new BookingRepository();

    public AllocationResult requestRoom(String studentId, String roomId, String slotId) {
        String eligibility = eligibilityService.checkEligibility(studentId);
        if (!"ELIGIBLE".equals(eligibility)) {
            if ("RESTRICTED".equals(eligibility)) return AllocationResult.restricted();
            return AllocationResult.error(eligibility);
        }
        double score = scoreCalc.calculateScore(studentId);
        AllocationResult result = allocationService.allocate(studentId, roomId, slotId, score);
        activityLog.logEvent("BOOKING_REQUEST", studentId + ":" + roomId + ":" + slotId);
        return result;
    }

    public List<Booking> getStudentBookings(String studentId) {
        return bookingRepo.findByStudent(studentId);
    }
}
