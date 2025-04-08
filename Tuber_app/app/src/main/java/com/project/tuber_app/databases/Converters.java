package com.project.tuber_app.databases;

import androidx.room.TypeConverter;

import java.util.Date;

public class Converters {

    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static String fromGender(UserEntity.Gender gender) {
        return gender == null ? null : gender.name();
    }

    @TypeConverter
    public static UserEntity.Gender toGender(String value) {
        return value == null ? null : UserEntity.Gender.valueOf(value);
    }

    @TypeConverter
    public static String fromRole(UserEntity.Role role) {
        return role == null ? null : role.name();
    }

    @TypeConverter
    public static UserEntity.Role toRole(String value) {
        return value == null ? null : UserEntity.Role.valueOf(value);
    }
}