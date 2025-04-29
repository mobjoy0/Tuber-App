package com.project.tuber_app.databases;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "locations")
public class Location {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public String locationName;

}
