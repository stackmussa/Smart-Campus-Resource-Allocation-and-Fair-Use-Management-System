package com.scrafms.service;

import com.scrafms.model.ValidationResult;
import com.scrafms.repository.ResourceRepository;

/**
 * ResourceValidator — validates room registration details before persisting a new resource.
 *
 * GRASP Pattern: Pure Fabrication — input validation extracted from ResourceController
 * GoF Pattern: N/A
 * Layer: Business Logic (Service)
 *
 * UC: UC-11 (Register New Resource)
 */
public class ResourceValidator {

    private final ResourceRepository resourceRepo = new ResourceRepository();

    public ValidationResult validateDetails(String roomId, String name, String building, int capacity) {
        if (roomId == null || roomId.isBlank()) return ValidationResult.invalid("Room ID is required.");
        if (name == null || name.isBlank()) return ValidationResult.invalid("Room name is required.");
        if (building == null || building.isBlank()) return ValidationResult.invalid("Building is required.");
        if (capacity <= 0) return ValidationResult.invalid("Capacity must be greater than 0.");
        if (resourceRepo.findByRoomId(roomId).isPresent()) return ValidationResult.invalid("Room ID already exists.");
        return ValidationResult.valid();
    }
}
