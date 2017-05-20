package com.coolweather.android.gson;
/*
 * Created by xiaofeng on 2017/4/2.
 */

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Weather {

    public String status;                 //接口状态

    public AQI aqi;                       //空气质量

    public Basic basic;                   //基本信息

    public Now now;                       //实况天气

    public Suggestion suggestion;         //生活指数

    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;   //天气预报

    @SerializedName("hourly_forecast")
    public List<Hour> hourList;
}
