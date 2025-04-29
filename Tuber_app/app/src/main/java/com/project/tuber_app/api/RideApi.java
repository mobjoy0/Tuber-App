package com.project.tuber_app.api;

import com.project.tuber_app.entities.Booking;
import com.project.tuber_app.entities.Ride;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RideApi {

    @GET("rides/search")
    Call<List<Ride>> searchRides(
            @Query("origin") String origin,
            @Query("destination") String destination,
            @Query("departureTime") String departureTime
    );

    @GET("rides/getRideByDriver")
    Call<Ride> getRideByDriver();

    @POST("rides/create")
    Call<ResponseBody> createRide(@Body Ride ride);

    @PUT("rides/change-status/{status}")
    Call<ResponseBody> changeRideStatus(@Path("status") String status);

    @GET("rides/recommendations")
    Call<List<Ride>> getRideRecommendations();





}
