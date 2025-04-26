package com.project.tuber_app.api;

import com.project.tuber_app.entities.User;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;

public interface UserApi {
    @Multipart
    @POST("/api/users/profile/upload")
    Call<String> uploadProfileImage(@Part MultipartBody.Part image
    );

    @PUT("/api/users/profile/update")
    Call<String> updateProfile(@Body User user);
}
