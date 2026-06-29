package com.scrafms.service;

import com.scrafms.handler.NotificationHandler;
import com.scrafms.handler.PenaltyHandler;
import com.scrafms.handler.RestrictionHandler;
import com.scrafms.handler.ViolationThresholdHandler;
import com.scrafms.model.FairnessPolicy;
import com.scrafms.model.PenaltyContext;
import com.scrafms.model.Student;
import com.scrafms.model.ViolationLog;
import com.scrafms.repository.FairnessPolicyRepository;
import com.scrafms.repository.StudentRepository;
import com.scrafms.repository.ViolationLogRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * PenaltyService — applies no-show penalties and fires the Chain of Responsibility handler chain.
 *
 * GRASP Pattern: Pure Fabrication — penalty logic isolated from domain objects
 * GoF Pattern: Chain of Responsibility (builds and fires the chain: ViolationThreshold → Restriction → Notification)
 * Layer: Business Logic (Service)
 *
 * UC: UC-07 (Receive No-Show Penalty)
 */
public class PenaltyService {

    private final StudentRepository studentRepo = new StudentRepository();
    private final FairnessPolicyRepository policyRepo = new FairnessPolicyRepository();
    private final ViolationLogRepository violationRepo = new ViolationLogRepository();

    public void applyPenalty(String studentId, String bookingId) {
        Optional<Student> opt = studentRepo.findById(studentId);
        if (opt.isEmpty()) return;
        Student s = opt.get();
        FairnessPolicy policy = policyRepo.getCurrentPolicy();
        double penaltyValue = policy.getNoShowPenaltyValue();

        // DB handles the increment atomically: noShowCount+1, fairnessScore-penaltyValue
        System.out.println("[PenaltyService] Applying penalty to: " + studentId + " value: " + penaltyValue);
        studentRepo.updatePenalty(studentId, penaltyValue);

        // Compute expected new values for the handler chain context (not written to DB here)
        int expectedNoShowCount = s.getNoShowCount() + 1;
        double expectedScore = Math.max(0.0, s.getFairnessScore() - policy.getNoShowPenaltyValue());

        ViolationLog log = new ViolationLog();
        log.setLogId(UUID.randomUUID().toString());
        log.setStudentId(studentId);
        log.setViolationType("NO_SHOW");
        log.setOccuredAt(LocalDateTime.now());
        log.setPenaltyApplied(policy.getNoShowPenaltyValue());
        violationRepo.saveViolation(log);

        PenaltyContext ctx = new PenaltyContext();
        ctx.setStudentId(studentId);
        ctx.setBookingId(bookingId);
        ctx.setNoShowCount(expectedNoShowCount);
        ctx.setCurrentFairnessScore(expectedScore);
        ctx.setPolicy(policy);
        ctx.setNotificationMessage("No-show penalty applied. Your fairness score is now " + expectedScore +
                ". No-show count: " + expectedNoShowCount);

        PenaltyHandler chain = new ViolationThresholdHandler();
        chain.setNext(new RestrictionHandler()).setNext(new NotificationHandler());
        chain.handle(ctx);
    }
}
