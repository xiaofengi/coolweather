package com.coolweather.android.db;
/*
 * Created by xiaofeng on 2017/4/2.
 */

import org.litepal.crud.DataSupport;

public class County extends DataSupport {

    private int id;

    private String countyName;

    private String weatherId;  //和风天气api上的cityId

    private int cityId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public String getWeatherId() {
        return weatherId;
    }

    public void setWeatherId(String weatherId) {
        this.weatherId = weatherId;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }
}
