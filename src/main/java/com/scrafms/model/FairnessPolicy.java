package com.scrafms.model;

public class FairnessPolicy {

    private String policyId;
    private double noShowPenaltyValue;
    private int restrictionThresholdDays;
    private int checkInWindowMinutes;
    private double usageWeight;
    private double reliabilityWeight;

    public FairnessPolicy() {}

    public String getPolicyId() { return policyId; }
    public void setPolicyId(String policyId) { this.policyId = policyId; }

    public double getNoShowPenaltyValue() { return noShowPenaltyValue; }
    public void setNoShowPenaltyValue(double noShowPenaltyValue) { this.noShowPenaltyValue = noShowPenaltyValue; }

    public int getRestrictionThresholdDays() { return restrictionThresholdDays; }
    public void setRestrictionThresholdDays(int restrictionThresholdDays) { this.restrictionThresholdDays = restrictionThresholdDays; }

    public int getCheckInWindowMinutes() { return checkInWindowMinutes; }
    public void setCheckInWindowMinutes(int checkInWindowMinutes) { this.checkInWindowMinutes = checkInWindowMinutes; }

    public double getUsageWeight() { return usageWeight; }
    public void setUsageWeight(double usageWeight) { this.usageWeight = usageWeight; }

    public double getReliabilityWeight() { return reliabilityWeight; }
    public void setReliabilityWeight(double reliabilityWeight) { this.reliabilityWeight = reliabilityWeight; }

    @Override
    public String toString() {
        return "FairnessPolicy{" +
                "policyId='" + policyId + '\'' +
                ", noShowPenaltyValue=" + noShowPenaltyValue +
                ", restrictionThresholdDays=" + restrictionThresholdDays +
                ", checkInWindowMinutes=" + checkInWindowMinutes +
                ", usageWeight=" + usageWeight +
                ", reliabilityWeight=" + reliabilityWeight +
                '}';
    }
}
