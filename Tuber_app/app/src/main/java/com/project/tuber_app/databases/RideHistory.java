package com.project.tuber_app.databases;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.sql.Timestamp;

@Entity(tableName = "rideHistory")
public class RideHistory {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public int userId;
    public String startLocation;
    public String endLocation;
    public String time;

}
