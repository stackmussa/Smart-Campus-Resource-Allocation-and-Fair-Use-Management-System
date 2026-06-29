package com.scrafms.model;

public class StudyRoom {

    private String roomId;
    private String name;
    private String building;
    private String location;
    private int capacity;
    private String status;
    private double geofenceRadius;

    public StudyRoom() {}

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getGeofenceRadius() { return geofenceRadius; }
    public void setGeofenceRadius(double geofenceRadius) { this.geofenceRadius = geofenceRadius; }

    @Override
    public String toString() {
        return "StudyRoom{" +
                "roomId='" + roomId + '\'' +
                ", name='" + name + '\'' +
                ", building='" + building + '\'' +
                ", capacity=" + capacity +
                ", status='" + status + '\'' +
                '}';
    }
}
