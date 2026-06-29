package com.scrafms.handler;

import com.scrafms.model.PenaltyContext;
import com.scrafms.repository.StudentRepository;

import java.time.LocalDateTime;

/**
 * RestrictionHandler — second link in the penalty chain; writes the restriction to the database if flagged.
 *
 * GRASP Pattern: N/A
 * GoF Pattern: Chain of Responsibility (concrete handler) — acts only if restrictionApplied is true, then passes on
 * Layer: Business Logic (Handler)
 *
 * UC: UC-07 (Receive No-Show Penalty)
 */
public class RestrictionHandler extends PenaltyHandler {

    @Override
    public void handle(PenaltyContext context) {
        // Link 2 of chain: persist restriction only if ViolationThresholdHandler set the flag
        if (context.isRestrictionApplied()) {
            StudentRepository studentRepository = new StudentRepository();
            int restrictionDays = (context.getPolicy() != null)
                    ? context.getPolicy().getRestrictionThresholdDays()
                    : 7;
            LocalDateTime endDate = LocalDateTime.now().plusDays(restrictionDays);
            // Write isRestricted=1 and restrictionEndDate to the Students table
            studentRepository.applyRestriction(context.getStudentId(), endDate);
        }
        // Pass context to next handler (NotificationHandler)
        if (next != null) {
            next.handle(context);
        }
    }
}
