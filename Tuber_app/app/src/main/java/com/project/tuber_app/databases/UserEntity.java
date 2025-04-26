package com.project.tuber_app.databases;


import android.util.Log;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.annotation.Nullable;
import androidx.room.TypeConverters;

import com.project.tuber_app.entities.User;

import java.util.Date;

@Entity(tableName = "users")
@TypeConverters({Converters.class})
public class UserEntity {

    @PrimaryKey
    @ColumnInfo(name = "userID")
    public int id;

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

    public UserEntity(){}
    public UserEntity(User user) {
        this.id = user.getId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.phoneNumber = user.getPhoneNumber();
        this.cin = user.getCin();
        this.password = user.getPassword();
        this.gender = user.getGender() != null ? Gender.valueOf(user.getGender().toString()) : null;
        this.role = user.getRole() != null ? Role.valueOf(user.getRole().toString()) : null;
        this.verified = user.getVerified();
        Log.wtf("ee", "bd is :"+ user.getBirthDate());
        this.birthDate = null;
        this.userImage = user.getUserImage();
    }
}
