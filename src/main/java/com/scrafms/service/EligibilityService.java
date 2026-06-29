package com.scrafms.service;

import com.scrafms.model.Student;
import com.scrafms.repository.StudentRepository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * EligibilityService — checks whether a student is eligible to book a room.
 *
 * GRASP Pattern: Pure Fabrication — isolated eligibility rule with no domain object home
 * GoF Pattern: N/A
 * Layer: Business Logic (Service)
 *
 * UC: UC-MR-01 (Request Study Room)
 */
public class EligibilityService {

    private final StudentRepository studentRepo = new StudentRepository();

    public String checkEligibility(String studentId) {
        Optional<Student> opt = studentRepo.findById(studentId);
        if (opt.isEmpty()) return "STUDENT_NOT_FOUND";
        Student s = opt.get();
        if (s.isRestricted()) {
            LocalDateTime end = s.getRestrictionEndDate();
            if (end == null || end.isAfter(LocalDateTime.now())) {
                return "RESTRICTED";
            }
        }
        if (s.getNoShowCount() >= 3) return "RESTRICTED";
        return "ELIGIBLE";
    }
}
