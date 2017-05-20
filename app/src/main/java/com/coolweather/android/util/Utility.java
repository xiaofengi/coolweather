package com.coolweather.android.util;
/*
 * Created by xiaofeng on 2017/4/2.
 */

import android.text.TextUtils;

import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;
import com.coolweather.android.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utility {

    //解析省级数据
    public static boolean handleProvinceResponse(String response){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray allProvinces = new JSONArray(response);  //转化成json数组
                for(int i = 0;i < allProvinces.length();i++){
                    JSONObject provinceObject = allProvinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();  //保存数据在litepal中
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }


    //解析市级数据
    public static boolean handleCityResponse(String response, int provinceId){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray allCities = new JSONArray(response);
                for(int i = 0;i < allCities.length();i++){
                    JSONObject cityObject = allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }


    //解析县级数据
    public static boolean handleCountyResponse(String response, int cityId){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray allCounties = new JSONArray(response);
                for(int i = 0;i < allCounties.length();i++){
                    JSONObject countyObject = allCounties.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.setSelect(false);
                    county.save();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    //解析天气信息,将json数据解析成Weather实体类
    public static Weather handleWeatherResponse(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");    //json键值
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent, Weather.class);     //gson解析
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
