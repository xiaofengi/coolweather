package com.coolweather.android.gson;
/*
 * Created by xiaofeng on 2017/4/2.
 */

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Weather {

    public String status;

    public AQI aqi;

    public Basic basic;

    public Now now;

    public Suggestion suggestion;

    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;
}
