package com.project.tuber_app.api;

import okhttp3.ResponseBody;
import retrofit2.http.PUT;
import retrofit2.http.Query;

import com.project.tuber_app.entities.Booking;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface BookingApi {

    @POST("booking/create")
    Call<Void> createBooking(@Body Booking booking);

    @GET("booking/requests/confirmed")
    Call<List<Booking>> getConfirmedBooking(@Query("rideId") int rideId);

    @GET("booking/requests/pending")
    Call<List<Booking>> getPendingBooking(@Query("rideId") int rideId);

    @PUT("booking/update-status")
    Call<ResponseBody> changeRequestStatus(
            @Query("bookingId") int bookingId,
            @Query("bookingStatus") String bookingStatus
    );

    @GET("booking/get-booking")
    Call<List<Booking>> getBooking(@Query("status") String status);

}
