package com.scrafms.service;

import com.scrafms.model.Student;
import com.scrafms.repository.StudentRepository;

import java.util.Optional;

/**
 * FairnessScoreCalculator — computes a student's fairness score from their no-show history.
 *
 * GRASP Pattern: Pure Fabrication — calculation logic extracted to keep Student model lean
 * GoF Pattern: N/A
 * Layer: Business Logic (Service)
 *
 * UC: UC-MR-01 (Request Study Room)
 */
public class FairnessScoreCalculator {

    private final StudentRepository studentRepo = new StudentRepository();

    public double calculateScore(String studentId) {
        Optional<Student> opt = studentRepo.findById(studentId);
        if (opt.isEmpty()) return 100.0;
        Student s = opt.get();
        double score = 100.0 - (s.getNoShowCount() * 10.0);
        return Math.max(0.0, score);
    }
}
