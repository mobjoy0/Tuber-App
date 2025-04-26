package com.project.tuber_app.databases;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RideHistoryDao {

    @Insert
    void insertRide(RideHistory ride);

    @Query("SELECT * FROM rideHistory WHERE userId =:id")
    List<RideHistory> getRideHistory(int id);

}
