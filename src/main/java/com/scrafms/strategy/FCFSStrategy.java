package com.scrafms.strategy;

/**
 * FCFSStrategy — allocates rooms on a first-come, first-served basis.
 *
 * GRASP Pattern: N/A
 * GoF Pattern: Strategy — concrete strategy; queue ordered by requestedAt ASC
 * Layer: Business Logic (Strategy)
 *
 * UC: UC-MR-01 (Request Study Room), UC-14 (Switch Allocation Mode)
 */
public class FCFSStrategy implements AllocationStrategy {

    @Override
    public String allocate(boolean slotAvailable, double fairnessScore) {
        if (slotAvailable) {
            return "CONFIRMED";
        }
        return "QUEUED";
    }

    @Override
    public String getQueueOrderBy() {
        return "requestedAt ASC";
    }

    @Override
    public String getStrategyName() {
        return "FCFS";
    }
}
