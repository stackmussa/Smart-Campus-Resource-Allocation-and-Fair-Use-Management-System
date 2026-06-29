package com.scrafms.handler;

import com.scrafms.model.Notification;
import com.scrafms.model.PenaltyContext;
import com.scrafms.repository.NotificationRepository;

/**
 * NotificationHandler — final link in the penalty chain; persists the penalty notification message.
 *
 * GRASP Pattern: N/A
 * GoF Pattern: Chain of Responsibility (concrete handler) — saves notification then passes to next (if any)
 * Layer: Business Logic (Handler)
 *
 * UC: UC-07 (Receive No-Show Penalty)
 */
public class NotificationHandler extends PenaltyHandler {

    @Override
    public void handle(PenaltyContext context) {
        // Link 3 of chain (terminal): persist the penalty notification message for the student
        String msg = context.getNotificationMessage();
        if (msg != null && !msg.isEmpty()) {
            Notification notification = new Notification(
                    context.getStudentId(),
                    "PENALTY",
                    msg
            );
            NotificationRepository notificationRepository = new NotificationRepository();
            notificationRepository.saveNotification(notification);
        }
        // Continue chain in case future handlers are appended (currently the last link)
        if (next != null) {
            next.handle(context);
        }
    }
}
