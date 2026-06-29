package com.scrafms.strategy;

public interface AllocationStrategy {

    String allocate(boolean slotAvailable, double fairnessScore);

    String getQueueOrderBy();

    String getStrategyName();
}
