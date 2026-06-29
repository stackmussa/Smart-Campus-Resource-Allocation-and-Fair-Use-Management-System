package com.scrafms.command;

import com.scrafms.service.OverrideCompletionFacade;

/**
 * OverrideBookingCommand — encapsulates the override action as a Command object.
 *
 * GRASP Pattern: N/A
 * GoF Pattern: Command — wraps bookingId + adminId + reason; delegates execution to OverrideCompletionFacade
 * Layer: Business Logic (Command)
 *
 * UC: UC-08 (Override Booking Decision)
 */
public class OverrideBookingCommand implements Command {

    private final String bookingId;
    private final String adminId;
    private final String reason;
    private final OverrideCompletionFacade facade = new OverrideCompletionFacade();

    public OverrideBookingCommand(String bookingId, String adminId, String reason) {
        this.bookingId = bookingId;
        this.adminId = adminId;
        this.reason = reason;
    }

    @Override
    public void execute() throws Exception {
        boolean success = facade.completeOverride(bookingId, adminId, reason);
        if (!success) throw new Exception("Override failed: booking not found or already cancelled.");
    }
}
