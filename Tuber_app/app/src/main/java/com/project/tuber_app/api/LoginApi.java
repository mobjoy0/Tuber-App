package com.project.tuber_app.api;

import com.project.tuber_app.activites.LoginRequest;
import com.project.tuber_app.activites.LoginResponse;
import com.project.tuber_app.entities.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface LoginApi {

    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @POST("auth/register")
    Call<Void> registerUser(@Body User user);


}
