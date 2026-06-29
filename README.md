# SCRAFMS
**Smart Campus Resource Allocation and Fair-Use Management System**

SE2004 – Software Design & Architecture | Spring 2026 | FAST-NUCES, Islamabad

| Team Member | Roll Number | Main Use Cases |
|---|---|---|
| Mussa Raza | 24I-3022 | UC-MR-01, UC-MR-03, UC-MR-05 |
| Ahmed Jamal | 24I-3006 | UC-12, UC-14, UC-15 |
| Muhammad Zain | 24I-3126 | UC-07, UC-08, UC-11 |

---

## Prerequisites

| Requirement | Version |
|---|---|
| Java (OpenJDK Temurin) | 21 |
| Apache Maven | 3.9+ |
| SQL Server Express | Any recent |
| SQL Server Management Studio (SSMS) | Any |
| Web Browser | Chrome recommended |

---

## Setup Instructions

### 1. Database Setup
1. Open SSMS and connect to your local SQL Server instance
2. Open `src/main/resources/sql/schema.sql`
3. Press **F5** — creates the database, all 15 tables, and seed data

### 2. Configure Database Connection
Open `src/main/resources/db.properties` and update the server name:
```
db.url=jdbc:sqlserver://YOUR-PC-NAME\\SQLEXPRESS;databaseName=scrafms_db;encrypt=false;trustServerCertificate=true;
db.user=scrafms_user
db.password=Scrafms@2026
```

### 3. Build and Run
```bash
mvn compile
mvn exec:java "-Dexec.mainClass=com.scrafms.Main"
```

Server starts at: **http://localhost:8080**

### 4. Open the App
Open Chrome and go to: `http://localhost:8080/login.html`

---

## Login Accounts

| Role | Email | Password |
|---|---|---|
| System Admin | admin@scrafms.com | admin123 |
| Resource Manager | manager@scrafms.com | manager123 |
| Student – Ahmed Jamal | ahmed@scrafms.com | student123 |
| Student – Muhammad Zain | zain@scrafms.com | student123 |
| Student – Mussa Raza | mussa@scrafms.com | student123 |

---

## Before Every Demo / Test Session

Open `SCRAFMS_TestSetup.sql` in SSMS and run **Section 2 (Quick Clear)**.
This resets all bookings, queues, penalties, and logs while keeping
accounts, rooms, and time slots intact.

> **Note on time slots:** Slots are set to May 15, 2026 so they remain
> valid and bookable on demo day. Do not run Section 1 again as it
> will regenerate the slots.

---

## Use Case Testing Guide

### UC-MR-01: Request Study Room *(Mussa Raza)*

**Tests:** Student books a room. System checks eligibility, calculates
fairness score, confirms booking or queues student.

1. Login as `ahmed@scrafms.com` / `student123`
2. Click **Request Room** in the sidebar
3. Click **Study Room A** — time slots appear on the right
4. Click any available slot
5. Click **Request Room**

**Expected:** Green **CONFIRMED** card

6. Go back to Request Room, select the **same slot** again
7. Click **Request Room**

**Expected:** Blue **QUEUED** card (slot is now taken)

---

### UC-MR-03: Cancel Booking *(Mussa Raza)*

**Tests:** Student cancels a confirmed booking. Slot is freed and the
next queued student is automatically promoted.

*Prerequisite: A CONFIRMED booking must exist (complete UC-MR-01 first)*

1. Login as `ahmed@scrafms.com` / `student123`
2. Click **My Bookings** in the sidebar
3. Find a **CONFIRMED** booking and click **Cancel**

**Expected:** Green toast, booking status changes to CANCELLED

---

### UC-MR-05: Join Waiting List *(Mussa Raza)*

**Tests:** Student joins the queue for a slot already booked by someone else.

1. Login as `ahmed@scrafms.com` / `student123`
2. Book any slot via Request Room (it becomes unavailable)
3. Click **Sign Out**
4. Login as `mussa@scrafms.com` / `student123`
5. Click **Waiting List** in the sidebar
6. Select **Study Room A** — select the same slot Ahmed booked
7. Click **Join Queue**

**Expected:** Toast shows queue position (e.g. "Position 1")

8. Check **Your Queue Positions** section at the bottom

**Expected:** Mussa's entry appears with position and priority score

---

### UC-07: Receive No-Show Penalty *(Muhammad Zain)*

**Tests:** System automatically detects a missed booking, reduces the
student's fairness score, and logs a violation.

> This UC requires an SSMS query and a server restart.
> It cannot be tested through the browser alone.

**Step 1 — Run Quick Clear (Section 2 of SCRAFMS_TestSetup.sql)**

**Step 2 — Run UC-07 Setup (Section 3 of SCRAFMS_TestSetup.sql)**

This inserts a booking that started 30 minutes ago with no check-in.

**Step 3 — Verify Ahmed's score before (should be 100.0)**

Run in SSMS:
```sql
SELECT fairnessScore, noShowCount FROM Students WHERE studentId = 'student001';
```

**Step 4 — Restart the server**

In the terminal press `Ctrl+C`, then run:
```bash
mvn exec:java "-Dexec.mainClass=com.scrafms.Main"
```

The no-show detection thread fires automatically on startup.

**Step 5 — Verify in SSMS (Section 5 verify queries)**

```sql
-- Booking should now show NO_SHOW
SELECT status FROM Bookings WHERE studentId = 'student001';

-- Score should be 90.0, count should be 1
SELECT fairnessScore, noShowCount FROM Students WHERE studentId = 'student001';

-- Violation log should have a new entry
SELECT violationType, penaltyApplied FROM ViolationLogs WHERE studentId = 'student001';
```

**Step 6 — Verify in browser**

Login as `ahmed@scrafms.com` → Click **My Penalties**

**Expected:** Fairness score ring shows **90**, violations table shows one entry

---

### UC-08: Override Booking Decision *(Muhammad Zain)*

**Tests:** Admin overrides a booking with a mandatory reason. Action is audit-logged.

*Prerequisite: A CONFIRMED booking must exist (complete UC-MR-01 first)*

1. Login as `admin@scrafms.com` / `admin123`
2. Click **Override Bookings** in the sidebar
3. Click **Override** on any booking
4. Type a reason **under 10 characters** (e.g. "short")

**Expected:** Confirm Override button stays **disabled**, counter turns red

5. Clear and type a valid reason (10+ chars, e.g. "Emergency room maintenance")

**Expected:** Confirm Override button becomes **enabled**

6. Click **Confirm Override**

**Expected:** Modal closes, green toast, status changes to **OVERRIDDEN**

---

### UC-11: Register New Resource *(Muhammad Zain)*

**Tests:** Admin registers a new study room with time slots.

1. Login as `admin@scrafms.com` / `admin123`
2. Click **Register Resource** in the sidebar
3. Fill in:
   - Room ID: `ROOM003`
   - Name: `Study Room C`
   - Building: `CS Block`
   - Location: `Second Floor`
   - Capacity: `8`
4. Click **Add Slot** and set any start and end time
5. Click **Register Room**

**Expected:** Green toast, ROOM003 appears in the table below the form

6. Try registering ROOM003 again with the same Room ID

**Expected:** Error message — duplicate Room ID rejected

---

### UC-12: Verify Check-In *(Ahmed Jamal)*

**Tests:** Student checks in to their booking within the allowed time window.

> The time slots are set to May 15, 2026. To demo check-in, you must
> first book a room and then run an SSMS query to shift that booking's
> start time to right now so the check-in window is open.

**Step 1 — Book a room**

Login as `ahmed@scrafms.com` → Click **Request Room** → Book any slot

**Step 2 — Run UC-12 Setup (Section 4 of SCRAFMS_TestSetup.sql)**

```sql
UPDATE Bookings
SET startTime   = DATEADD(minute, -5,  GETDATE()),
    endTime     = DATEADD(minute,  55, GETDATE()),
    checkInTime = NULL
WHERE studentId = 'student001'
  AND status    = 'CONFIRMED'
  AND checkInTime IS NULL;
```

This sets the booking's start time to 5 minutes ago so the check-in
window is currently open.

**Step 3 — Immediately go to the browser**

Click **Check In** in the sidebar

**Expected:** The booking appears in the list with a time remaining countdown

**Step 4 — Click Check In**

**Expected:** Row updates to **ACTIVE** status

---

### UC-14: Switch Allocation Mode *(Ahmed Jamal)*

**Tests:** Admin switches allocation strategy at runtime. Change takes
effect immediately for all subsequent bookings.

1. Login as `admin@scrafms.com` / `admin123`
2. Click **System Config** in the sidebar

**Expected:** Three mode cards shown. **Fair Use** is highlighted.

3. Click **FCFS** card → Click **Apply Mode**

**Expected:** Green toast, current mode label updates to **FCFS**

4. Click **Exam Mode** card → Click **Apply Mode**

**Expected:** Mode updates to **EXAM_MODE**

5. Sign out → Login as `ahmed@scrafms.com` / `student123`
6. Click **Request Room** → select any room and slot → Click **Request Room**

**Expected:** Orange **Exam Mode Active — booking blocked** card

7. Sign out → Login as admin → Switch mode back to **Fair Use**

---

### UC-15: Auto-Promote Queue Member *(Ahmed Jamal)*

**Tests:** When a booking is cancelled, the system automatically promotes
the highest-priority student from the waiting queue.

1. Login as `ahmed@scrafms.com` / `student123`
2. Click **Request Room** → book any slot
3. Sign out → Login as `mussa@scrafms.com` / `student123`
4. Click **Waiting List** → select the same slot Ahmed booked → Click **Join Queue**

**Expected:** Mussa is at Position 1 in queue

5. Sign out → Login as `ahmed@scrafms.com` / `student123`
6. Click **My Bookings** → Click **Cancel** on that booking

**Expected:** Ahmed's booking is cancelled

7. Sign out → Login as `mussa@scrafms.com` / `student123`
8. Click **My Bookings**

**Expected:** Mussa now has a **CONFIRMED** booking — promoted automatically

**Verify in SSMS:**
```sql
SELECT b.status, p.name AS student
FROM Bookings b JOIN Persons p ON b.studentId = p.personId
WHERE b.studentId = 'student003';

SELECT COUNT(*) AS queueRemaining FROM WaitingQueueEntries;
```

---

## Architecture

```
Browser (HTML/CSS/JS)
        ↕ HTTP port 8080
Java HttpServer — Web Handlers
        ↕ method calls
Controllers → Services → Strategies / Handlers
        ↕ JDBC
SQL Server Express (scrafms_db)
```

**Layered Architecture:** Presentation → Business Logic → Data Access

---

## Design Patterns

| Pattern | Category | Key Classes |
|---|---|---|
| Strategy | GoF Behavioural | FairUseStrategy, FCFSStrategy, ExamModeStrategy |
| Observer | GoF Behavioural | BookingObserver, NotificationService |
| Chain of Responsibility | GoF Behavioural | PenaltyHandler → ViolationThresholdHandler → RestrictionHandler → NotificationHandler |
| Command | GoF Behavioural | OverrideBookingCommand, RegisterResourceCommand |
| Facade | GoF Structural | OverrideCompletionFacade |
| Template Method | GoF Behavioural | CheckInController.requestCheckIn() |
| Singleton | GoF Creational | DatabaseConnection |
| Factory | GoF Creational | NotificationMessageFactory |
| Controller | GRASP | BookingController, CheckInController, OverrideController, etc. |
| Information Expert | GRASP | All Repository classes |
| Pure Fabrication | GRASP | AllocationService, EligibilityService, PenaltyService, etc. |
