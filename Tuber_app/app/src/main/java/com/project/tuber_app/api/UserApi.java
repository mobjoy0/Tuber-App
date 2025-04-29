package com.project.tuber_app.api;


import com.project.tuber_app.activites.ResetPasswordActivity;
import com.project.tuber_app.entities.ResetPasswordRequest;
import com.project.tuber_app.entities.User;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface UserApi {
    @Multipart
    @POST("/api/users/profile/upload")
    Call<ResponseBody> uploadProfileImage(@Part MultipartBody.Part image
    );

    @GET("/api/users/profile")
    Call<User> getUserProfile();

    @PUT("/api/users/profile/update")
    Call<ResponseBody> updateProfile(@Body User user);

    @POST("/api/auth/send-verification-code")
    Call<ResponseBody> sendVerificationCode(@Query("email") String email);

    @POST("/api/auth/verify-code")
    Call<ResponseBody> verifyCodeEmail(@Query("email") String email, @Query("code") String code);

    @PATCH("/api/auth/reset-password")
    Call<ResponseBody> resetPassword(@Body ResetPasswordRequest resetPasswordRequest);

    @PATCH("/api/users/reset-password")
    Call<ResponseBody> resetEmail(@Body ResetPasswordRequest resetPasswordRequest);
}
