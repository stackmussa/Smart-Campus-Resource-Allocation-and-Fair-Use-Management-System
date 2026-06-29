package com.scrafms.handler;

import com.scrafms.model.PenaltyContext;

/**
 * PenaltyHandler — abstract base for the no-show penalty Chain of Responsibility.
 *
 * GRASP Pattern: N/A
 * GoF Pattern: Chain of Responsibility (abstract handler) — subclasses linked via setNext(); each decides to act or pass
 * Layer: Business Logic (Handler)
 *
 * UC: UC-07 (Receive No-Show Penalty)
 */
public abstract class PenaltyHandler {

    protected PenaltyHandler next;

    public PenaltyHandler setNext(PenaltyHandler next) {
        this.next = next;
        return next;
    }

    public abstract void handle(PenaltyContext context);
}
