package com.coolweather.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.coolweather.android.gson.Forecast;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.service.AutoUpdateService;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;

    private Spinner titleCity;

    private List<String> city_name_list = new ArrayList<>(0);

    public static int city_name_numbers;

    private TextView titleUpdateTime;

    private TextView degreeText;

    private TextView weatherInfoText;

    private LinearLayout forecastLayout;

    private TextView aqiText;

    private TextView pm25Text;

    private TextView comfortText;

    private TextView carWashText;

    private TextView sportText;

    private ImageView bingPicImg;

    public SwipeRefreshLayout swipeRefreshLayout;

    private String mWeatherId;

    public static String mCityName;

    public DrawerLayout drawerLayout;

    SharedPreferences preferences;

    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Button navButton;

        Button setButton;

        View decorView = getWindow().getDecorView();
        //设置系统状态栏透明
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE); //android5.0系统以上
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        setContentView(R.layout.activity_weather_acivity);
        //初始化控件
        weatherLayout = (ScrollView)findViewById(R.id.weather_layout);
        titleCity = (Spinner)findViewById(R.id.title_city);
        titleUpdateTime = (TextView)findViewById(R.id.title_update_time);
        degreeText = (TextView)findViewById(R.id.degree_text);
        weatherInfoText = (TextView)findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout)findViewById(R.id.forecast_layout);
        aqiText = (TextView)findViewById(R.id.aqi_text);
        pm25Text = (TextView)findViewById(R.id.pm25_text);
        comfortText = (TextView)findViewById(R.id.comfort_text);
        carWashText = (TextView)findViewById(R.id.car_wash_text);
        sportText = (TextView)findViewById(R.id.sport_text);
        bingPicImg = (ImageView)findViewById(R.id.bing_pic_img);
        setButton =(Button)findViewById(R.id.setting);

        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);                   //点击滑动菜单
        navButton = (Button)findViewById(R.id.nav_button);
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });


        preferences = PreferenceManager.getDefaultSharedPreferences(this);

         /*
        String bingPic =preferences.getString("bing_pic", null);     //加载必应图片
        if(bingPic != null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else {
            loadBingPic();
        }
        */

         city_name_numbers = preferences.getInt("cnn", 0);
         Log.d("main", "city name numbers are " + String.valueOf(city_name_numbers));
         for(int i = 0; i < city_name_numbers; i++){
             String cityName = preferences.getString("cn"+i, null);
             city_name_list.add(cityName);
             Log.d("main", "city name " + i + " is " + cityName);
         }
        adapter = new ArrayAdapter<>(WeatherActivity.this, R.layout.spinner_display_style,R.id.spinnerText, city_name_list);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_style);
        titleCity.setAdapter(adapter);
        //titleCity.setSelection(city_name_numbers-1);
        //切换城市
        titleCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("main","position is " + position);
                String weatherString = preferences.getString("weather"+String.valueOf(position), null);
                if(weatherString != null){                                                      //有缓存
                    Weather weather = Utility.handleWeatherResponse(weatherString);
                    mWeatherId = weather.basic.weatherId;
                    mCityName = weather.basic.cityName;
                    showWeatherInfo(weather);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        final String weatherString = preferences.getString("weather"+String.valueOf(city_name_numbers-1), null);
        if(weatherString != null){                                                      //有缓存
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeatherId = weather.basic.weatherId;
            mCityName = weather.basic.cityName;
            showWeatherInfo(weather);
        }else {
            mWeatherId = getIntent().getStringExtra("weather_id");                      //无缓存
            mCityName = getIntent().getStringExtra("city_name");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId, mCityName);
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {   //滑动刷新
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId, null);
            }
        });

        //点击设置
        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent setIntent = new Intent(WeatherActivity.this, SettingActivity.class);
                startActivity(setIntent);
            }
        });

    }

    //网上查询天气信息
    public void requestWeather(final String weatherId, String cityName){
        /*
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isAvailable())
        {
            Toast.makeText(WeatherActivity.this, "无网络连接", Toast.LENGTH_SHORT).show();
            return false;
        }
        */
        if(cityName!=null){
            mCityName = cityName;
            city_name_list.add(mCityName);
            city_name_numbers++;
            adapter.notifyDataSetChanged();
            titleCity.setSelection(city_name_numbers-1);
            Log.d("main", "mCityName is " + mCityName);
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
            editor.putInt("cnn", city_name_numbers);
            editor.putString("cn"+String.valueOf(city_name_numbers-1), mCityName);
            editor.apply();
            Log.d("main", "now cnn is " + city_name_numbers);
        }
        mWeatherId = weatherId;    //更新天气id
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=86e7876195234876993eddb4ce2a6175";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                    }
                });
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather != null && "ok".equals(weather.status)){
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("cn"+String.valueOf(city_name_numbers-1), weather.basic.cityName);
                            editor.putString("weather"+String.valueOf(city_name_numbers-1), responseText);    //利用SharedPreferences缓存数据
                            editor.apply();
                            showWeatherInfo(weather);
                        }
                        else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
    }

    //显示天气信息和启动后台更新服务
    public  void showWeatherInfo(Weather weather){
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        String weatherCode = weather.now.more.weatherCode;

        //显示天气信息
        loadBGI(weatherCode);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for (Forecast forecast:weather.forecastList){
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = (TextView)view.findViewById(R.id.date_text);
            TextView infoText = (TextView)view.findViewById(R.id.info_text);
            TextView maxText = (TextView)view.findViewById(R.id.max_text);
            TextView minText = (TextView)view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        if(weather.aqi != null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWah = "洗车指数: " + weather.suggestion.carWash.info;
        String sport = "运动建议: " + weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWah);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);

        //启动后台更新服务
        boolean isAutoRefresh = preferences.getBoolean("is_auto_refresh", true);
        if(isAutoRefresh) {
            Intent intent = new Intent(WeatherActivity.this, AutoUpdateService.class);
            startService(intent);
        }
    }

    //加载背景图
    private void loadBGI(String weatherCode){
        switch (weatherCode){
            case "100":
                bingPicImg.setImageResource(R.drawable.sunny);
                break;
            case "101":
                bingPicImg.setImageResource(R.drawable.cloudy);
                break;
            case "102":
                bingPicImg.setImageResource(R.drawable.fewclouds);
                break;
            case "103":
                bingPicImg.setImageResource(R.drawable.partlycloudy);
                break;
            case "104":
                bingPicImg.setImageResource(R.drawable.overcast);
                break;
            case "201":
                bingPicImg.setImageResource(R.drawable.calm);
                break;
            case "200":
            case "202":
            case "203":
            case "204":
            case "205":
            case "206":
            case "207":
            case "208":
            case "209":
            case "210":
            case "211":
            case "212":
            case "213":
                bingPicImg.setImageResource(R.drawable.gale);
                break;
            case "300":
            case "301":
            case "302":
            case "303":
            case "304":
            case "305":
            case "306":
            case "307":
            case "308":
            case "309":
            case "310":
            case "311":
            case "312":
            case "313":
                bingPicImg.setImageResource(R.drawable.rain);
                break;
            case "400":
            case "401":
            case "402":
            case "403":
            case "404":
            case "405":
            case "406":
            case "407":
                bingPicImg.setImageResource(R.drawable.snowstorm);
                break;
            case "500":
            case "501":
                bingPicImg.setImageResource(R.drawable.foggy);
                break;
            case "502":
                bingPicImg.setImageResource(R.drawable.haze);
                break;
            case "503":
            case "504":
                bingPicImg.setImageResource(R.drawable.foggy);
                break;
            case "507":
            case "508":
                bingPicImg.setImageResource(R.drawable.duststorm);
                break;
            case "900":
                bingPicImg.setImageResource(R.drawable.hot);
                break;
            case "901":
                bingPicImg.setImageResource(R.drawable.cold);
                break;
            case "999":
                bingPicImg.setImageResource(R.drawable.error);
                break;
            default:
                bingPicImg.setImageResource(R.drawable.error);
                break;
        }
    }

        /*
    //加载必应图片
    private void loadBingPic(){
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }
    */
}
