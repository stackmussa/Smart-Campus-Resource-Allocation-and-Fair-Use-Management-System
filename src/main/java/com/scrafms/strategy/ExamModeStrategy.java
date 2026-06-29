package com.scrafms.strategy;

/**
 * ExamModeStrategy — blocks all new room bookings during exam periods.
 *
 * GRASP Pattern: N/A
 * GoF Pattern: Strategy — concrete strategy; always returns EXAM_BLOCKED regardless of availability
 * Layer: Business Logic (Strategy)
 *
 * UC: UC-MR-01 (Request Study Room), UC-14 (Switch Allocation Mode)
 */
public class ExamModeStrategy implements AllocationStrategy {

    @Override
    public String allocate(boolean slotAvailable, double fairnessScore) {
        return "EXAM_BLOCKED";
    }

    @Override
    public String getQueueOrderBy() {
        return "requestedAt ASC";
    }

    @Override
    public String getStrategyName() {
        return "EXAM_MODE";
    }
}
