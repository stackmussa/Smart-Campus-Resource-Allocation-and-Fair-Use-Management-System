package com.scrafms.strategy;

/**
 * FairUseStrategy — allocates rooms to the student with the highest fairness score.
 *
 * GRASP Pattern: N/A
 * GoF Pattern: Strategy — concrete strategy; queue ordered by priorityScore DESC
 * Layer: Business Logic (Strategy)
 *
 * UC: UC-MR-01 (Request Study Room), UC-14 (Switch Allocation Mode)
 */
public class FairUseStrategy implements AllocationStrategy {

    @Override
    public String allocate(boolean slotAvailable, double fairnessScore) {
        if (slotAvailable) {
            return "CONFIRMED";
        }
        return "QUEUED";
    }

    @Override
    public String getQueueOrderBy() {
        return "priorityScore DESC";
    }

    @Override
    public String getStrategyName() {
        return "FAIR_USE";
    }
}
