package com.example.arcadeapp;

import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {
    @POST("register")
    Call<JsonObject> register(@Body JsonObject userData);

    @POST("login")
    Call<JsonObject> login(@Body JsonObject credentials);

    @GET("get_score")
    Call<JsonObject> getScore(@Header("Authorization") String token);

    @POST("update_score")
    Call<JsonObject> updateScore(@Header("Authorization") String token, @Body JsonObject scoreData);
} 