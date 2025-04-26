package com.project.tuber_app.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final Context context;

    public AuthInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        SharedPreferences prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);


        Request originalRequest = chain.request();
        Request.Builder builder = originalRequest.newBuilder();
        if (token != null) {
            Log.wtf("e", "token = "+ token);
            builder.header("Authorization", "Bearer " + token);
        }

        Request modifiedRequest = builder.build();
        Log.d("AuthInterceptor", "Final Request Headers: " + modifiedRequest.headers().toString());
        return chain.proceed(modifiedRequest);
    }
}
