package com.scrafms.repository;

import com.scrafms.model.FairnessPolicy;
import com.scrafms.util.DatabaseConnection;

import java.sql.*;

/**
 * FairnessPolicyRepository — retrieves the active fairness policy (penalty values, time windows).
 *
 * GRASP Pattern: Information Expert — owns knowledge of fairness policy configuration
 * GoF Pattern: N/A
 * Layer: Data Access
 *
 * UC: UC-07 (Receive No-Show Penalty), UC-12 (Verify Check-In)
 */
public class FairnessPolicyRepository {

    public FairnessPolicy getCurrentPolicy() {
        String sql = "SELECT TOP 1 policyId, noShowPenaltyValue, restrictionThresholdDays, checkInWindowMinutes, usageWeight, reliabilityWeight FROM FairnessPolicies";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("[FairnessPolicyRepository] getCurrentPolicy error: " + e.getMessage());
        }
        return createDefault();
    }

    public void save(FairnessPolicy p) {
        String sql = "INSERT INTO FairnessPolicies(policyId, noShowPenaltyValue, restrictionThresholdDays, checkInWindowMinutes, usageWeight, reliabilityWeight) VALUES(?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getPolicyId());
            ps.setDouble(2, p.getNoShowPenaltyValue());
            ps.setInt(3, p.getRestrictionThresholdDays());
            ps.setInt(4, p.getCheckInWindowMinutes());
            ps.setDouble(5, p.getUsageWeight());
            ps.setDouble(6, p.getReliabilityWeight());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[FairnessPolicyRepository] save error: " + e.getMessage());
        }
    }

    private FairnessPolicy mapRow(ResultSet rs) throws SQLException {
        FairnessPolicy p = new FairnessPolicy();
        p.setPolicyId(rs.getString("policyId"));
        p.setNoShowPenaltyValue(rs.getDouble("noShowPenaltyValue"));
        p.setRestrictionThresholdDays(rs.getInt("restrictionThresholdDays"));
        p.setCheckInWindowMinutes(rs.getInt("checkInWindowMinutes"));
        p.setUsageWeight(rs.getDouble("usageWeight"));
        p.setReliabilityWeight(rs.getDouble("reliabilityWeight"));
        return p;
    }

    private FairnessPolicy createDefault() {
        FairnessPolicy p = new FairnessPolicy();
        p.setPolicyId("DEFAULT");
        p.setNoShowPenaltyValue(10.0);
        p.setRestrictionThresholdDays(7);
        p.setCheckInWindowMinutes(15);
        p.setUsageWeight(0.4);
        p.setReliabilityWeight(0.6);
        return p;
    }
}
