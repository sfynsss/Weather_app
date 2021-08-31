package com.example.weatherapp;

import retrofit2.Call;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface Api {

    @GET("https://api.openweathermap.org/data/2.5/onecall")
    Call<WeatherResponse> getWeather(
            @Query("lat") String lat,
            @Query("lon") String lon,
            @Query("appid") String appid
    );

}
