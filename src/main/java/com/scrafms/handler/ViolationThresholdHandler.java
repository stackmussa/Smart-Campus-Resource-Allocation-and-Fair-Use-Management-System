package com.scrafms.handler;

import com.scrafms.model.PenaltyContext;

/**
 * ViolationThresholdHandler — first link in the penalty chain; flags restriction when no-show count hits threshold.
 *
 * GRASP Pattern: N/A
 * GoF Pattern: Chain of Responsibility (concrete handler) — sets restrictionApplied flag then passes to next
 * Layer: Business Logic (Handler)
 *
 * UC: UC-07 (Receive No-Show Penalty)
 */
public class ViolationThresholdHandler extends PenaltyHandler {

    @Override
    public void handle(PenaltyContext context) {
        int threshold = 3;
        // Link 1 of chain: check if student has hit the no-show threshold
        if (context.getNoShowCount() >= threshold) {
            context.setRestrictionApplied(true); // flag for RestrictionHandler to act on
        }
        // Pass context to next handler (RestrictionHandler) regardless of threshold result
        if (next != null) {
            next.handle(context);
        }
    }
}
