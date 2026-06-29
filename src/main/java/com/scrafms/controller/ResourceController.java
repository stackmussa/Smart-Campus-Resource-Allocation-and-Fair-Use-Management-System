package com.scrafms.controller;

import com.scrafms.command.RegisterResourceCommand;
import com.scrafms.model.StudyRoom;
import com.scrafms.model.ValidationResult;
import com.scrafms.repository.ActivityLogRepository;
import com.scrafms.repository.ResourceRepository;
import com.scrafms.service.ResourceValidator;

import java.util.List;

/**
 * ResourceController — registers new study rooms and retrieves the room list.
 *
 * GRASP Pattern: Controller — entry point for resource registration; Creator — instantiates StudyRoom
 * GoF Pattern: N/A
 * Layer: Business Logic (Controller)
 *
 * UC: UC-11 (Register New Resource)
 */
public class ResourceController {

    private final ResourceValidator validator = new ResourceValidator();
    private final ActivityLogRepository activityLog = new ActivityLogRepository();
    private final ResourceRepository resourceRepo = new ResourceRepository();

    public ValidationResult registerResource(String roomId, String name, String building,
                                             String location, int capacity, String adminId) {
        ValidationResult vr = validator.validateDetails(roomId, name, building, capacity);
        if (!vr.isValid()) return vr;

        StudyRoom room = new StudyRoom();
        room.setRoomId(roomId);
        room.setName(name);
        room.setBuilding(building);
        room.setLocation(location);
        room.setCapacity(capacity);
        room.setStatus("AVAILABLE");
        room.setGeofenceRadius(50.0);

        RegisterResourceCommand cmd = new RegisterResourceCommand(room, adminId);
        try {
            cmd.execute();
            activityLog.logEvent("RESOURCE_REGISTERED", roomId);
            return ValidationResult.valid();
        } catch (Exception e) {
            return ValidationResult.invalid("Registration failed: " + e.getMessage());
        }
    }

    public List<StudyRoom> getAllRooms() {
        return resourceRepo.findAllRooms();
    }
}
