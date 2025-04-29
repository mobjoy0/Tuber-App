package com.project.tuber_app.databases;

import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;

@androidx.room.Database(entities = {UserEntity.class, Location.class}, version = 6)
public abstract class Database extends RoomDatabase {
    public static volatile Database instance;

    public  abstract UserDao userDao();
    public  abstract LocationsDao locationsDao();


    public static Database getInstance(Context context) {
        if (instance == null) {
            synchronized (Database.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    Database.class, "DataBase")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}
