package com.project.tuber_app.databases;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LocationsDao {

    @Insert
    void insertLocation(Location location);

    @Query("SELECT locationName FROM locations")
    List<String> getAllLocations();

    @Query("SELECT COUNT(*) FROM locations")
    int getSize();

}
