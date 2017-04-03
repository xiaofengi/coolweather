package com.coolweather.android.gson;
/*
 * Created by xiaofeng on 2017/4/2.
 */

import com.google.gson.annotations.SerializedName;

public class AQI {

    public AQICity city;

    public class AQICity{

        public String aqi;

        public String pm25;

    }
}
