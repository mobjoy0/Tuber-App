package com.project.tuber_app.databases;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Upsert;

import java.util.List;

@Dao
public interface UserDao {
    // Profile operations
    @Insert
    void insertUser(UserEntity userEntity);

    @Upsert
    void upsertUser(UserEntity userEntity);

    @Update
    void update(UserEntity user);

    @Query("DELETE FROM users")
    void clearUsers();

    @Query("SELECT * FROM users LIMIT 1")
    UserEntity getUser();



}


