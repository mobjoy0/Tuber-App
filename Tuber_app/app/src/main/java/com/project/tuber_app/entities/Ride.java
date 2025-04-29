package com.project.tuber_app.entities;

import android.os.Parcel;
import android.os.Parcelable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Ride implements Parcelable {
    private int id;
    private User driver;
    private String startLocation;
    private String endLocation;
    private String departureTime; // Use String to avoid time parsing issues
    private int availableSeats;
    private int totalSeats;
    private double price;
    private int eta;
    private int distance;
    private String polyline;

    public Ride() {
    }

    public Ride(int id, String startLocation, String endLocation, String departureTime, double price, int eta, int distance, String polyline) {
        this.id = id;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.departureTime = departureTime;
        this.price = price;
        this.eta = eta;
        this.distance = distance;
        this.polyline = polyline;
    }

    public Ride(int id, String departureTime, String startLocation, int eta, String endLocation, double price) {
        this.id = id;
        this.departureTime = departureTime;
        this.startLocation = startLocation;
        this.eta = eta;
        this.endLocation = endLocation;
        this.price = price;
    }
    public void setTotalSeats(int totalSeats){
        this.totalSeats = totalSeats;
    }

    public int getTotalSeats(){
        return totalSeats;
    }

    public String getRideDate() {
        if (departureTime != null && departureTime.contains("T")) {
            return departureTime.split("T")[0];
        }
        return "";
    }


    public String getRideTime() {
        if (departureTime != null && departureTime.contains("T")) {
            String timePart = departureTime.split("T")[1];
            if (timePart.length() >= 5) {
                return timePart.substring(0,5); // Only return HH:mm
            }
            return timePart;
        }
        return "";
    }


    public String getRideEndTime() {
        if (departureTime != null && departureTime.contains("T")) {
            try {
                // Parse the departureTime string to LocalDateTime
                LocalDateTime departureDateTime = LocalDateTime.parse(departureTime);

                // Add eta (minutes)
                LocalDateTime endDateTime = departureDateTime.plusMinutes(eta);

                // Format to only show time (HH:mm)
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                String formattedTime = endDateTime.format(timeFormatter);

                // Check if it's the next day
                if (endDateTime.getDayOfMonth() != departureDateTime.getDayOfMonth()) {
                    formattedTime += " +1d";
                }

                return formattedTime;

            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }
        return "";
    }


    public String getRideDuration() {
        int hours = eta / 60;
        int minutes = eta % 60;
        return String.format("%02d:%02d", hours, minutes);
    }



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(String startLocation) {
        this.startLocation = startLocation;
    }

    public String getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(String endLocation) {
        this.endLocation = endLocation;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getEta() {
        return eta;
    }

    public void setEta(int eta) {
        this.eta = eta;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public String getPolyline() {
        return polyline;
    }

    public void setPolyline(String polyline) {
        this.polyline = polyline;
    }
    public User getDriver(){
        return driver;
    }

    public void setDriver(User driver){
        this.driver = driver;
    }

    public int getAvailableSeats(){
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats){
        this.availableSeats = availableSeats;
    }

    protected Ride(Parcel in) {
        id = in.readInt();
        departureTime = in.readString();
        startLocation = in.readString();
        eta = in.readInt();
        endLocation = in.readString();
        price = in.readDouble();
    }

    public static final Creator<Ride> CREATOR = new Creator<Ride>() {
        @Override
        public Ride createFromParcel(Parcel in) {
            return new Ride(in);
        }

        @Override
        public Ride[] newArray(int size) {
            return new Ride[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(departureTime);
        parcel.writeString(startLocation);
        parcel.writeInt(eta);
        parcel.writeString(endLocation);
        parcel.writeDouble(price);
        parcel.writeInt(availableSeats);
    }

    @Override
    public String toString() {
        return "Ride{" +
                "id=" + id +
                ", driver=" + driver +
                ", startLocation='" + startLocation + '\'' +
                ", endLocation='" + endLocation + '\'' +
                ", departureTime='" + departureTime + '\'' +
                ", availableSeats=" + availableSeats +
                ", price=" + price +
                ", eta=" + eta +
                ", distance=" + distance +
                ", polyline='" + polyline + '\'' +
                '}';
    }
}