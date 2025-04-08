package com.project.tuber_app.databases;


import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.annotation.Nullable;
import androidx.room.TypeConverters;

import java.util.Date;

@Entity(tableName = "users")
@TypeConverters({Converters.class})
public class UserEntity {

    @PrimaryKey
    @ColumnInfo(name = "userID")
    public int userId;

    @ColumnInfo(name = "first_name")
    public String firstName;

    @ColumnInfo(name = "last_name")
    public String lastName;

    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "phone_number")
    public String phoneNumber;

    @ColumnInfo(name = "CIN")
    public String cin;

    @ColumnInfo(name = "password")
    public String password;

    @ColumnInfo(name = "gender")
    public Gender gender;

    @Nullable
    @ColumnInfo(name = "verified")
    public Boolean verified;

    @ColumnInfo(name = "birth_date")
    public Date birthDate;

    @ColumnInfo(name = "role")
    public Role role;

    @ColumnInfo(name = "user_image", typeAffinity = ColumnInfo.BLOB)
    public byte[] userImage;

    public enum Gender {
        MALE, FEMALE
    }

    public enum Role {
        DRIVER, RIDER
    }
}
