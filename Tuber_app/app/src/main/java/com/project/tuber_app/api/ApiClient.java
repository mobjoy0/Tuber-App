package com.project.tuber_app.api;

import android.content.Context;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "https://b5e3-102-109-27-78.ngrok-free.app/api/";

    private static Retrofit retrofit = null;
    private static OkHttpClient client = null;

    private static Retrofit getRetrofitInstance(Context context) {
        if (retrofit == null) {
            if (client == null) {
                OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                        .addInterceptor(new AuthInterceptor(context));

                // Uncomment this if you want to see request/response logs

                HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                logging.setLevel(HttpLoggingInterceptor.Level.NONE);
                clientBuilder.addInterceptor(logging);


                client = clientBuilder.build();
            }

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }

        return retrofit;
    }

    public static RideApi getRideApi(Context context) {
        return getRetrofitInstance(context).create(RideApi.class);
    }

    public static UserApi getUserApi(Context context) {
        return getRetrofitInstance(context).create(UserApi.class);
    }

    public static BookingApi getBookingApi(Context context) {
        return getRetrofitInstance(context).create(BookingApi.class);
    }

    public static LoginApi getLoginApi(Context context) {
        return getRetrofitInstance(context).create(LoginApi.class);
    }
}
