package com.project.tuber_app.entities;

import com.google.gson.annotations.SerializedName;
import com.project.tuber_app.databases.UserEntity;

import lombok.ToString;

@ToString
public class User {

    private Integer id;

    private String firstName;


    private String lastName;

    private String email;


    private String phoneNumber;

    private String cin;

    private String password;


    private Gender gender;

    private Boolean verified = false;

    private String birthDate;


    private Role role;

    public User(int id){
        this.id = id;
    }

    public User(String firstName, String lastName, String email, String phoneNumber,
                String cin, String password, Gender gender, String birthDate,
                Role role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.cin = cin;
        this.password = password;
        this.gender = gender;
        this.birthDate = birthDate;
        this.role = role;
    }

    public User(UserEntity userEntity) {
        if (userEntity == null) {
            throw new IllegalArgumentException("UserEntity cannot be null");
        }

        this.id = userEntity.id;
        this.firstName = userEntity.firstName;
        this.lastName = userEntity.lastName;
        this.email = userEntity.email;
        this.phoneNumber = userEntity.phoneNumber;
        this.cin = userEntity.cin;
        this.password = userEntity.password;
        this.gender = userEntity.gender != null ? User.Gender.valueOf(userEntity.gender.toString()) : null;
        this.verified = userEntity.verified;
        this.birthDate = null;
        this.role = userEntity.role != null ? User.Role.valueOf(userEntity.role.toString()) : null;
        this.userImage = userEntity.userImage;
    }

    @SerializedName("user_image") private byte[] userImage;

    public enum Gender {
        MALE, FEMALE
    }

    public enum Role {
        DRIVER, RIDER
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCin() {
        return cin;
    }

    public void setCin(String cin) {
        this.cin = cin;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public byte[] getUserImage() {
        return userImage;
    }

    public void setUserImage(byte[] userImage) {
        this.userImage = userImage;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", cin='" + cin + '\'' +
                ", password=" + password+ // Masking password for security
                ", gender=" + gender +
                ", verified=" + verified +
                ", birthDate='" + birthDate + '\'' +
                ", role=" + role +
                ", userImage=" + (userImage != null ? "Exists" : "No Image") + // Avoid printing raw bytes
                '}';
    }


}