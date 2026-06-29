-- ============================================================
-- SCRAFMS – Test Setup Queries
-- SE2004 Software Design & Architecture | Spring 2026
-- FAST-NUCES, Islamabad
--
-- Team:
--   Mussa Raza    (24I-3022)
--   Ahmed Jamal   (24I-3006)
--   Muhammad Zain (24I-3126)
--
-- SECTIONS:
--   1. QUICK CLEAR    — run before every test / demo session
--   2. UC-07 SETUP    — run only when testing No-Show Penalty
--   3. UC-12 SETUP    — run only when testing Check-In
--   4. VERIFY QUERIES — run anytime to inspect current state


USE scrafms_db;
GO




-- ============================================================
-- SECTION 1: QUICK CLEAR
-- Run this at the START of every test or demo session.
-- Clears all bookings, queues, penalties, and logs.
-- Keeps: user accounts, rooms, time slots, policy, config.
-- ============================================================

DELETE FROM ActivityLogs;
DELETE FROM AuditLogs;
DELETE FROM Notifications;
DELETE FROM ViolationLogs;
DELETE FROM WaitingQueueEntries;
DELETE FROM Bookings;

-- Free all time slots for booking again
UPDATE TimeSlots SET isAvailable = 1;

-- Reset all student scores and restriction status
UPDATE Students
SET fairnessScore      = 100.0,
    noShowCount        = 0,
    isRestricted       = 0,
    restrictionEndDate = NULL;

-- Reset allocation mode to Fair Use
UPDATE AllocationConfigs SET activeMode = 'FAIR_USE';




-- ============================================================
-- SECTION 2: UC-07 SETUP — No-Show Penalty (Muhammad Zain)
--
-- WHY: The no-show detection thread fires on server startup.
-- It looks for CONFIRMED bookings whose check-in window
-- has already passed and checkInTime is NULL.
-- This cannot be created through the UI (UI only creates
-- future bookings), so we insert one manually here.
--
-- HOW TO USE:
--   1. Run Section 1 (Quick Clear) first
--   2. Run this section
--   3. Stop the server (Ctrl+C in terminal)
--   4. Start the server again (mvn exec:java ...)
-- ============================================================


SELECT fairnessScore, noShowCount FROM Students WHERE studentId='student001'

------

INSERT INTO Bookings
    (bookingId, studentId, roomId, slotId,
     status, startTime, endTime, checkInTime, allocationMode)
VALUES
    (NEWID(),
     'student001',                          -- Ahmed Jamal
     'ROOM001',                             -- Study Room A
     (SELECT TOP 1 slotId FROM TimeSlots),  -- any slot ID
     'CONFIRMED',
     DATEADD(minute, -30, GETDATE()),       -- started 30 min ago
     DATEADD(minute, -15, GETDATE()),       -- ended 15 min ago
     NULL,                                  -- never checked in
     'FAIR_USE');




------
SELECT status FROM Bookings WHERE studentId='student001'

SELECT fairnessScore, noShowCount FROM Students WHERE studentId='student001'

SELECT violationType, penaltyApplied FROM ViolationLogs WHERE studentId='student001'




-- ============================================================
-- SECTION 3: UC-12 SETUP — Verify Check-In (Ahmed Jamal)
--
-- WHY: The check-in window is startTime -60min to +30min.
-- The slots are set to May 15 which is in the future,
-- so they will not appear in the check-in tab during demo.
-- This query shifts an existing booking to 5 min ago so
-- the check-in window is open RIGHT NOW.
--
-- HOW TO USE:
--   1. First book a room through the UI as ahmed@scrafms.com
--   2. Then run this query in SSMS
--   3. Immediately go to Check In tab in the browser
--      The booking will appear with the window open
-- ============================================================

UPDATE Bookings
SET startTime   = DATEADD(minute, -5,  GETDATE()),
    endTime     = DATEADD(minute,  55, GETDATE()),
    checkInTime = NULL
WHERE studentId = 'student001'
  AND status    = 'CONFIRMED'
  AND checkInTime IS NULL;




-- ============================================================
-- SECTION 5: VERIFY QUERIES
-- Run any of these at any point to inspect the database state.
-- ============================================================

-- All bookings with student and room names
SELECT b.bookingId, p.name AS student, r.name AS room,
       b.status, b.startTime, b.endTime, b.checkInTime
FROM Bookings b
JOIN Persons    p ON b.studentId = p.personId
JOIN StudyRooms r ON b.roomId    = r.roomId
ORDER BY b.startTime DESC;

-- All queue entries
SELECT wq.position, p.name AS student,
       r.name AS room, wq.priorityScore
FROM WaitingQueueEntries wq
JOIN Persons    p  ON wq.studentId = p.personId
JOIN StudyRooms r  ON wq.roomId    = r.roomId;

-- Student scores and restriction status
SELECT p.name, s.fairnessScore, s.noShowCount, s.isRestricted
FROM Students s
JOIN Persons p ON s.studentId = p.personId;

-- Violation log (for UC-07 verification)
SELECT p.name AS student, vl.violationType,
       vl.penaltyApplied, vl.occuredAt
FROM ViolationLogs vl
JOIN Persons p ON vl.studentId = p.personId
ORDER BY vl.occuredAt DESC;

-- Audit log (for UC-08 and UC-14 verification)
SELECT auditId, actionId, reason, timestamp
FROM AuditLogs
ORDER BY timestamp DESC;

-- Slot availability
SELECT r.name AS room, ts.startTime,
       ts.endTime, ts.isAvailable
FROM TimeSlots ts
JOIN StudyRooms r ON ts.roomId = r.roomId
ORDER BY r.name, ts.startTime;

-- Current allocation mode
SELECT activeMode, changedAt FROM AllocationConfigs;
GO


