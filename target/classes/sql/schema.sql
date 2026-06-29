-- ============================================================
-- SCRAFMS Database Schema
-- SQL Server Express
-- ============================================================

-- Migration: widen ActivityLogs.eventType if the table already exists with VARCHAR(50)
IF OBJECT_ID('dbo.ActivityLogs', 'U') IS NOT NULL
    AND EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_NAME='ActivityLogs' AND COLUMN_NAME='eventType'
          AND CHARACTER_MAXIMUM_LENGTH < 200
    )
BEGIN
    ALTER TABLE ActivityLogs ALTER COLUMN eventType VARCHAR(200) NOT NULL;
END
GO

-- Drop tables in reverse FK dependency order
IF OBJECT_ID('dbo.AuditLogs',             'U') IS NOT NULL DROP TABLE dbo.AuditLogs;
IF OBJECT_ID('dbo.ActivityLogs',          'U') IS NOT NULL DROP TABLE dbo.ActivityLogs;
IF OBJECT_ID('dbo.Notifications',         'U') IS NOT NULL DROP TABLE dbo.Notifications;
IF OBJECT_ID('dbo.ViolationLogs',         'U') IS NOT NULL DROP TABLE dbo.ViolationLogs;
IF OBJECT_ID('dbo.AllocationConfigs',     'U') IS NOT NULL DROP TABLE dbo.AllocationConfigs;
IF OBJECT_ID('dbo.FairnessPolicies',      'U') IS NOT NULL DROP TABLE dbo.FairnessPolicies;
IF OBJECT_ID('dbo.FairnessScores',        'U') IS NOT NULL DROP TABLE dbo.FairnessScores;
IF OBJECT_ID('dbo.WaitingQueueEntries',   'U') IS NOT NULL DROP TABLE dbo.WaitingQueueEntries;
IF OBJECT_ID('dbo.Bookings',              'U') IS NOT NULL DROP TABLE dbo.Bookings;
IF OBJECT_ID('dbo.TimeSlots',             'U') IS NOT NULL DROP TABLE dbo.TimeSlots;
IF OBJECT_ID('dbo.StudyRooms',            'U') IS NOT NULL DROP TABLE dbo.StudyRooms;
IF OBJECT_ID('dbo.SystemAdministrators',  'U') IS NOT NULL DROP TABLE dbo.SystemAdministrators;
IF OBJECT_ID('dbo.ResourceManagers',      'U') IS NOT NULL DROP TABLE dbo.ResourceManagers;
IF OBJECT_ID('dbo.Students',              'U') IS NOT NULL DROP TABLE dbo.Students;
IF OBJECT_ID('dbo.Persons',               'U') IS NOT NULL DROP TABLE dbo.Persons;
GO

-- ============================================================
-- 1. Persons
-- ============================================================
CREATE TABLE Persons (
    personId     VARCHAR(50)  NOT NULL,
    name         VARCHAR(100) NOT NULL,
    email        VARCHAR(100) NOT NULL,
    passwordHash VARCHAR(255) NOT NULL,
    role         VARCHAR(20)  NOT NULL,
    CONSTRAINT PK_Persons  PRIMARY KEY (personId),
    CONSTRAINT UQ_Persons_Email UNIQUE (email)
);
GO

-- ============================================================
-- 2. Students
-- ============================================================
CREATE TABLE Students (
    studentId          VARCHAR(50)  NOT NULL,
    rollNumber         VARCHAR(50)  NOT NULL,
    department         VARCHAR(100) NOT NULL,
    fairnessScore      FLOAT        NOT NULL DEFAULT 100.0,
    noShowCount        INT          NOT NULL DEFAULT 0,
    isRestricted       BIT          NOT NULL DEFAULT 0,
    restrictionEndDate DATETIME     NULL,
    CONSTRAINT PK_Students       PRIMARY KEY (studentId),
    CONSTRAINT UQ_Students_Roll  UNIQUE (rollNumber),
    CONSTRAINT FK_Students_Person FOREIGN KEY (studentId) REFERENCES Persons(personId)
);
GO

-- ============================================================
-- 3. ResourceManagers
-- ============================================================
CREATE TABLE ResourceManagers (
    managerId   VARCHAR(50)  NOT NULL,
    department  VARCHAR(100) NOT NULL,
    accessLevel VARCHAR(50)  NOT NULL,
    CONSTRAINT PK_ResourceManagers       PRIMARY KEY (managerId),
    CONSTRAINT FK_ResourceManagers_Person FOREIGN KEY (managerId) REFERENCES Persons(personId)
);
GO

-- ============================================================
-- 4. SystemAdministrators
-- ============================================================
CREATE TABLE SystemAdministrators (
    adminId    VARCHAR(50) NOT NULL,
    adminLevel VARCHAR(50) NOT NULL,
    lastLogin  DATETIME    NULL,
    CONSTRAINT PK_SystemAdministrators       PRIMARY KEY (adminId),
    CONSTRAINT FK_SystemAdministrators_Person FOREIGN KEY (adminId) REFERENCES Persons(personId)
);
GO

-- ============================================================
-- 5. StudyRooms
-- ============================================================
CREATE TABLE StudyRooms (
    roomId         VARCHAR(50)  NOT NULL,
    name           VARCHAR(100) NOT NULL,
    building       VARCHAR(100) NOT NULL,
    location       VARCHAR(200) NOT NULL DEFAULT '',
    capacity       INT          NOT NULL,
    status         VARCHAR(20)  NOT NULL DEFAULT 'AVAILABLE',
    geofenceRadius FLOAT        NOT NULL DEFAULT 50.0,
    CONSTRAINT PK_StudyRooms PRIMARY KEY (roomId)
);
GO

-- ============================================================
-- 6. TimeSlots
-- ============================================================
CREATE TABLE TimeSlots (
    slotId      VARCHAR(50) NOT NULL,
    roomId      VARCHAR(50) NOT NULL,
    startTime   DATETIME    NOT NULL,
    endTime     DATETIME    NOT NULL,
    isAvailable BIT         NOT NULL DEFAULT 1,
    CONSTRAINT PK_TimeSlots       PRIMARY KEY (slotId),
    CONSTRAINT FK_TimeSlots_Room  FOREIGN KEY (roomId) REFERENCES StudyRooms(roomId)
);
GO

-- ============================================================
-- 7. Bookings
-- ============================================================
CREATE TABLE Bookings (
    bookingId      VARCHAR(50) NOT NULL,
    studentId      VARCHAR(50) NOT NULL,
    roomId         VARCHAR(50) NOT NULL,
    slotId         VARCHAR(50) NOT NULL,
    status         VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    startTime      DATETIME    NOT NULL,
    endTime        DATETIME    NOT NULL,
    checkInTime    DATETIME    NULL,
    allocationMode VARCHAR(30) NOT NULL,
    CONSTRAINT PK_Bookings         PRIMARY KEY (bookingId),
    CONSTRAINT FK_Bookings_Student FOREIGN KEY (studentId) REFERENCES Students(studentId),
    CONSTRAINT FK_Bookings_Room    FOREIGN KEY (roomId)    REFERENCES StudyRooms(roomId),
    CONSTRAINT FK_Bookings_Slot    FOREIGN KEY (slotId)    REFERENCES TimeSlots(slotId)
);
GO

-- ============================================================
-- 8. WaitingQueueEntries
-- ============================================================
CREATE TABLE WaitingQueueEntries (
    queueId       VARCHAR(50) NOT NULL,
    studentId     VARCHAR(50) NOT NULL,
    roomId        VARCHAR(50) NOT NULL,
    slotId        VARCHAR(50) NOT NULL,
    position      INT         NOT NULL,
    requestedAt   DATETIME    NOT NULL DEFAULT GETDATE(),
    priorityScore FLOAT       NOT NULL,
    CONSTRAINT PK_WaitingQueueEntries         PRIMARY KEY (queueId),
    CONSTRAINT FK_WaitingQueue_Student FOREIGN KEY (studentId) REFERENCES Students(studentId),
    CONSTRAINT FK_WaitingQueue_Room    FOREIGN KEY (roomId)    REFERENCES StudyRooms(roomId),
    CONSTRAINT FK_WaitingQueue_Slot    FOREIGN KEY (slotId)    REFERENCES TimeSlots(slotId)
);
GO

-- ============================================================
-- 9. FairnessScores
-- ============================================================
CREATE TABLE FairnessScores (
    scoreId              VARCHAR(50) NOT NULL,
    studentId            VARCHAR(50) NOT NULL,
    totalScore           FLOAT       NOT NULL,
    usageFrequencyWeight FLOAT       NOT NULL,
    reliabilityWeight    FLOAT       NOT NULL,
    penaltyDeduction     FLOAT       NOT NULL,
    lastUpdated          DATETIME    NOT NULL DEFAULT GETDATE(),
    CONSTRAINT PK_FairnessScores         PRIMARY KEY (scoreId),
    CONSTRAINT FK_FairnessScores_Student FOREIGN KEY (studentId) REFERENCES Students(studentId)
);
GO

-- ============================================================
-- 10. FairnessPolicies
-- ============================================================
CREATE TABLE FairnessPolicies (
    policyId                   VARCHAR(50) NOT NULL,
    noShowPenaltyValue         FLOAT       NOT NULL DEFAULT 10.0,
    restrictionThresholdDays   INT         NOT NULL DEFAULT 7,
    checkInWindowMinutes       INT         NOT NULL DEFAULT 15,
    usageWeight                FLOAT       NOT NULL DEFAULT 0.4,
    reliabilityWeight          FLOAT       NOT NULL DEFAULT 0.6,
    CONSTRAINT PK_FairnessPolicies PRIMARY KEY (policyId)
);
GO

-- ============================================================
-- 11. AllocationConfigs
-- ============================================================
CREATE TABLE AllocationConfigs (
    configId   VARCHAR(50) NOT NULL,
    activeMode VARCHAR(30) NOT NULL DEFAULT 'FAIR_USE',
    changedAt  DATETIME    NOT NULL DEFAULT GETDATE(),
    CONSTRAINT PK_AllocationConfigs PRIMARY KEY (configId)
);
GO

-- ============================================================
-- 12. ViolationLogs
-- ============================================================
CREATE TABLE ViolationLogs (
    logId          VARCHAR(50)  NOT NULL,
    studentId      VARCHAR(50)  NOT NULL,
    violationType  VARCHAR(50)  NOT NULL,
    occuredAt      DATETIME     NOT NULL DEFAULT GETDATE(),
    penaltyApplied FLOAT        NOT NULL,
    CONSTRAINT PK_ViolationLogs         PRIMARY KEY (logId),
    CONSTRAINT FK_ViolationLogs_Student FOREIGN KEY (studentId) REFERENCES Students(studentId)
);
GO

-- ============================================================
-- 13. Notifications
-- ============================================================
CREATE TABLE Notifications (
    notifId        VARCHAR(50)  NOT NULL,
    studentId      VARCHAR(50)  NOT NULL,
    type           VARCHAR(50)  NOT NULL,
    message        VARCHAR(500) NOT NULL,
    sentAt         DATETIME     NOT NULL DEFAULT GETDATE(),
    deliveryStatus VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    CONSTRAINT PK_Notifications         PRIMARY KEY (notifId),
    CONSTRAINT FK_Notifications_Student FOREIGN KEY (studentId) REFERENCES Students(studentId)
);
GO

-- ============================================================
-- 14. AuditLogs
-- ============================================================
CREATE TABLE AuditLogs (
    auditId   VARCHAR(50)  NOT NULL,
    actionId  VARCHAR(50)  NOT NULL,
    color     VARCHAR(50)  NOT NULL DEFAULT '',
    reason    VARCHAR(500) NOT NULL DEFAULT '',
    timestamp DATETIME     NOT NULL DEFAULT GETDATE(),
    CONSTRAINT PK_AuditLogs PRIMARY KEY (auditId)
);
GO

-- ============================================================
-- 15. ActivityLogs
-- ============================================================
CREATE TABLE ActivityLogs (
    logId     VARCHAR(50)  NOT NULL,
    eventType VARCHAR(200) NOT NULL,
    timestamp DATETIME     NOT NULL DEFAULT GETDATE(),
    CONSTRAINT PK_ActivityLogs PRIMARY KEY (logId)
);
GO

-- ============================================================
-- SEED DATA
-- ============================================================

-- Persons
-- SHA-256('admin123')   = 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
-- SHA-256('manager123') = 866485796cfa8d7c0cf7111640205b83076433547577511d81f8030ae99ecea5
-- SHA-256('student123') = 703b0a3d6ad75b649a28adde7d83c6251da457549263bc7ff45ec709b0a8448b
INSERT INTO Persons (personId, name, email, passwordHash, role) VALUES
('admin001',   'System Admin',     'admin@scrafms.com',   '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMIN'),
('manager001', 'Resource Manager', 'manager@scrafms.com', '866485796cfa8d7c0cf7111640205b83076433547577511d81f8030ae99ecea5', 'MANAGER'),
('student001', 'Ahmed Ali',        'ahmed@scrafms.com',   '703b0a3d6ad75b649a28adde7d83c6251da457549263bc7ff45ec709b0a8448b', 'STUDENT'),
('student002', 'Sara Khan',        'sara@scrafms.com',    '703b0a3d6ad75b649a28adde7d83c6251da457549263bc7ff45ec709b0a8448b', 'STUDENT');
GO

-- SystemAdministrators
INSERT INTO SystemAdministrators (adminId, adminLevel, lastLogin) VALUES
('admin001', 'SUPER', NULL);
GO

-- ResourceManagers
INSERT INTO ResourceManagers (managerId, department, accessLevel) VALUES
('manager001', 'Facilities', 'FULL');
GO

-- Students
INSERT INTO Students (studentId, rollNumber, department, fairnessScore, noShowCount, isRestricted, restrictionEndDate) VALUES
('student001', 'CS-2021-001', 'Computer Science', 100.0, 0, 0, NULL),
('student002', 'CS-2021-002', 'Computer Science', 100.0, 0, 0, NULL);
GO

-- Study Room
INSERT INTO StudyRooms (roomId, name, building, location, capacity, status, geofenceRadius) VALUES
('ROOM001', 'Study Room A', 'Main Block', 'Ground Floor, Block A', 6, 'AVAILABLE', 50.0);
GO

-- Time Slots for ROOM001 (tomorrow: morning 09-11, afternoon 13-15, evening 17-19)
DECLARE @tomorrow DATE = DATEADD(day, 1, CAST(GETDATE() AS DATE));
INSERT INTO TimeSlots (slotId, roomId, startTime, endTime, isAvailable) VALUES
('SLOT001', 'ROOM001',
    DATEADD(hour, 9,  CAST(@tomorrow AS DATETIME)),
    DATEADD(hour, 11, CAST(@tomorrow AS DATETIME)),
    1),
('SLOT002', 'ROOM001',
    DATEADD(hour, 13, CAST(@tomorrow AS DATETIME)),
    DATEADD(hour, 15, CAST(@tomorrow AS DATETIME)),
    1),
('SLOT003', 'ROOM001',
    DATEADD(hour, 17, CAST(@tomorrow AS DATETIME)),
    DATEADD(hour, 19, CAST(@tomorrow AS DATETIME)),
    1);
GO

-- Active FairnessPolicy
INSERT INTO FairnessPolicies (policyId, noShowPenaltyValue, restrictionThresholdDays, checkInWindowMinutes, usageWeight, reliabilityWeight) VALUES
('POLICY001', 10.0, 7, 15, 0.4, 0.6);
GO

-- Active AllocationConfig
INSERT INTO AllocationConfigs (configId, activeMode, changedAt) VALUES
('CONFIG001', 'FAIR_USE', GETDATE());
GO
