package com.scrafms.command;

import com.scrafms.model.AuditLog;
import com.scrafms.model.StudyRoom;
import com.scrafms.model.TimeSlot;
import com.scrafms.repository.AuditLogRepository;
import com.scrafms.repository.ResourceRepository;
import com.scrafms.repository.TimeSlotRepository;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * RegisterResourceCommand — encapsulates room registration (save room + generate slots + write audit log).
 *
 * GRASP Pattern: N/A
 * GoF Pattern: Command — bundles StudyRoom + adminId; execute() performs the full registration transaction
 * Layer: Business Logic (Command)
 *
 * UC: UC-11 (Register New Resource)
 */
public class RegisterResourceCommand implements Command {

    private final StudyRoom room;
    private final String adminId;
    private final ResourceRepository resourceRepo = new ResourceRepository();
    private final TimeSlotRepository slotRepo = new TimeSlotRepository();
    private final AuditLogRepository auditRepo = new AuditLogRepository();

    public RegisterResourceCommand(StudyRoom room, String adminId) {
        this.room = room;
        this.adminId = adminId;
    }

    @Override
    public void execute() throws Exception {
        resourceRepo.saveRoom(room);

        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        String[] slotNames = {"Morning", "Afternoon", "Evening"};
        int[][] hours = {{8, 10}, {12, 14}, {18, 20}};
        for (int i = 0; i < 3; i++) {
            TimeSlot slot = new TimeSlot();
            slot.setSlotId(UUID.randomUUID().toString());
            slot.setRoomId(room.getRoomId());
            slot.setStartTime(tomorrow.plusHours(hours[i][0]));
            slot.setEndTime(tomorrow.plusHours(hours[i][1]));
            slot.setAvailable(true);
            slotRepo.saveSlot(slot);
        }

        AuditLog audit = new AuditLog();
        audit.setAuditId(UUID.randomUUID().toString());
        audit.setActionId(room.getRoomId());
        audit.setColor("GREEN");
        audit.setReason("Resource registered by admin " + adminId);
        audit.setTimestamp(LocalDateTime.now());
        auditRepo.saveAuditEntry(audit);
    }
}
