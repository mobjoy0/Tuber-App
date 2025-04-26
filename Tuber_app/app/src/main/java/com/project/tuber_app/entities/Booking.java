package com.project.tuber_app.entities;

public class Booking {
    private int id;
    private Ride ride;
    private User passenger;
    private int seatsBooked;
    private Status status;

    public enum Status {
        PENDING,
        CONFIRMED,
        CANCELED,
        COMPLETED
    }

    // Default constructor
    public Booking() {
        this.status = Status.PENDING;
    }

    // Parameterized constructor
    public Booking(int id, Ride ride, User passenger, int seatsBooked) {
        this.id = id;
        this.ride = ride;
        this.passenger = passenger;
        this.seatsBooked = seatsBooked;
        this.status = Status.PENDING;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Ride getRide() {
        return ride;
    }

    public void setRide(Ride ride) {
        this.ride = ride;
    }

    public User getPassenger() {
        return passenger;
    }

    public void setPassenger(User passenger) {
        this.passenger = passenger;
    }

    public int getSeatsBooked() {
        return seatsBooked;
    }

    public void setSeatsBooked(int seatsBooked) {
        this.seatsBooked = seatsBooked;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Booking{" +
                "id=" + id +
                ", ride=" + (ride != null ? ride.getId() : "null") +
                ", passenger=" + (passenger != null ? passenger.getId() : "null") +
                ", seatsBooked=" + seatsBooked +
                ", status=" + status +
                '}';
    }
}