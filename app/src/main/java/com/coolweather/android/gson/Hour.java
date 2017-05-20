package com.coolweather.android.gson;
/*
 * Created by xiaofeng on 2017/5/20.
 */

import com.google.gson.annotations.SerializedName;

public class Hour {

    public String date;

    @SerializedName("cond")
    public More more;

    @SerializedName("tmp")
    public String temperature;

    public class More{

        @SerializedName("txt")
        public String info;
    }

}
