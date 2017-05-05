package com.coolweather.android.gson;
/*
 * Created by xiaofeng on 2017/4/2.
 */

import com.google.gson.annotations.SerializedName;

public class Now {

    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    public class  More{

        @SerializedName("code")
        public String weatherCode;

        @SerializedName("txt")
        public String info;
    }
}
