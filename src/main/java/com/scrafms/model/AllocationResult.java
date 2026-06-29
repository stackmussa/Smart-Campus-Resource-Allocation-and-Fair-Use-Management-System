package com.scrafms.model;

public class AllocationResult {

    private String status;
    private String bookingId;
    private String queueId;
    private int queuePosition;
    private String message;

    public AllocationResult() {}

    public AllocationResult(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public static AllocationResult alreadyBooked() {
        AllocationResult r = new AllocationResult();
        r.setStatus("ALREADY_BOOKED");
        r.setMessage("You already have a booking for this slot");
        return r;
    }

    public static AllocationResult confirmed(String bookingId) {
        AllocationResult r = new AllocationResult("CONFIRMED", "Booking confirmed successfully.");
        r.setBookingId(bookingId);
        return r;
    }

    public static AllocationResult queued(String queueId, int position) {
        AllocationResult r = new AllocationResult("QUEUED", "Added to waiting queue at position " + position + ".");
        r.setQueueId(queueId);
        r.setQueuePosition(position);
        return r;
    }

    public static AllocationResult restricted() {
        return new AllocationResult("RESTRICTED", "Your account is currently restricted from making bookings.");
    }

    public static AllocationResult examBlocked() {
        return new AllocationResult("EXAM_BLOCKED", "Bookings are blocked during exam mode.");
    }

    public static AllocationResult error(String msg) {
        return new AllocationResult("ERROR", msg);
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getQueueId() { return queueId; }
    public void setQueueId(String queueId) { this.queueId = queueId; }

    public int getQueuePosition() { return queuePosition; }
    public void setQueuePosition(int queuePosition) { this.queuePosition = queuePosition; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    @Override
    public String toString() {
        return "AllocationResult{status='" + status + "', message='" + message + "'}";
    }
}
