package com.scrafms.controller;

import com.scrafms.model.ValidationResult;
import com.scrafms.repository.ActivityLogRepository;
import com.scrafms.repository.AllocationModeRepository;

/**
 * AllocationController — switches the active room allocation mode (FAIR_USE, FCFS, EXAM_MODE).
 *
 * GRASP Pattern: Controller — handles the switchMode system operation
 * GoF Pattern: N/A
 * Layer: Business Logic (Controller)
 *
 * UC: UC-14 (Switch Allocation Mode)
 */
public class AllocationController {

    private final AllocationModeRepository modeRepo = new AllocationModeRepository();
    private final ActivityLogRepository activityLog = new ActivityLogRepository();

    private static final java.util.Set<String> VALID_MODES =
            java.util.Set.of("FAIR_USE", "FCFS", "EXAM_MODE");

    public ValidationResult switchMode(String adminId, String newMode) {
        if (!VALID_MODES.contains(newMode))
            return ValidationResult.invalid("Invalid allocation mode: " + newMode);
        if (modeRepo.isSameMode(newMode))
            return ValidationResult.invalid("System is already in " + newMode + " mode.");
        modeRepo.saveActiveMode(newMode);
        activityLog.logEvent("MODE_SWITCH", adminId + ":" + newMode);
        return ValidationResult.valid();
    }

    public String fetchCurrentMode() {
        return modeRepo.getCurrentMode();
    }
}
