package com.coolweather.android;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/*
 * Created by xiaofeng on 2017/4/2.
 */

public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE = 0;

    public static final int LEVEL_CITY = 1;

    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;

    private TextView titleText;

    private Button backButton;

    private ListView listView;

    private ArrayAdapter<String> adapter;

    private List<String> dataList = new ArrayList<>();  //当前页面list存储信息

    private List<Province> provinceList;

    private List<City> cityList;

    private List<County> countyList;

    private Province selectedProvince;

    private City selectedCity;

    private int currentLevel;

    @Nullable
    @Override  //加载列表
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        titleText =(TextView)view.findViewById(R.id.title_text);
        backButton = (Button)view.findViewById(R.id.back_button);
        listView = (ListView)view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        return view;
    }

    //初始化碎片
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(currentLevel == LEVEL_PROVINCE){
                    selectedProvince = provinceList.get(i);
                    queryCities();      //查询当前省份下的城市
                }else if(currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(i);
                    queryCounties();    //查询当前市下的县区
                }else if(currentLevel == LEVEL_COUNTY){
                    ConnectivityManager connectivityManager = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                    String weatherId = countyList.get(i).getWeatherId();
                    if(getActivity() instanceof MainActivity) {    //当前活动为主活动，即第一次打开应用，选择第一个城市
                        if(networkInfo == null || !networkInfo.isAvailable())
                        {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(), "无网络连接", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }else {
                            countyList.get(i).setSelect(true);       //为已选择的县添加标记
                            countyList.get(i).saveFast();            //立即保存
                            countyList.get(i).save();
                            Intent intent = new Intent(getActivity(), WeatherActivity.class);
                            intent.putExtra("weather_id", weatherId);
                            intent.putExtra("city_name", countyList.get(i).getCountyName());
                            startActivity(intent);
                            getActivity().finish();
                        }
                    }else if(getActivity() instanceof WeatherActivity){   //当前活动为天气活动，即选择其他的城市
                        WeatherActivity activity = (WeatherActivity) getActivity();    //判断有无网络
                        if(networkInfo == null || !networkInfo.isAvailable())
                        {
                            activity.drawerLayout.closeDrawers();
                            getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), "无网络连接", Toast.LENGTH_SHORT).show();
                            }
                        });
                        }else {
                            if(countyList.get(i).isSelect())
                            {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getActivity(), "已选择该城市", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }else {
                                countyList.get(i).setSelect(true);       //为已选择的县添加标记
                                countyList.get(i).saveFast();            //立即保存
                                countyList.get(i).save();
                                activity.drawerLayout.closeDrawers();
                                activity.swipeRefreshLayout.setRefreshing(true);
                                activity.requestWeather(weatherId, countyList.get(i).getCountyName());
                            }
                        }
                    }
                }
            }
        });
        //返回按钮
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLevel == LEVEL_COUNTY){
                    queryCities();
                }else if(currentLevel == LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();   //初始为查询省份
    }

    //查询省
    private void queryProvinces(){
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
        if(provinceList.size() > 0){       //缓存中有，即已经查询过
            dataList.clear();
            for(Province province:provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged(); //更新列表
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }else {                           //缓存没有
            String address = "http://guolin.tech/api/china";
            queryFromServe(address, "province");   //从服务器查询
        }
    }

    //查询市
    private void queryCities(){
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceId = ?", String.valueOf(selectedProvince.getId())).find(City.class);
        if(cityList.size() > 0){
            dataList.clear();
            for (City city:cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServe(address, "city");
        }

    }

    //查询县
    private void queryCounties(){
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityId = ?", String.valueOf(selectedCity.getId())).find(County.class);
        if(countyList.size() > 0){
            dataList.clear();
            for (County county:countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServe(address, "county");
        }
    }

    //从服务器查询省市县信息
    private void queryFromServe(String address,final String type){
        showProgressDialog();  //显示进度对话框
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if("province".equals(type)){      //类型为省份
                    result = Utility.handleProvinceResponse(responseText);
                }else if("city".equals(type)){    //类型为城市
                    result = Utility.handleCityResponse(responseText, selectedProvince.getId());
                }else if("county".equals(type)){  //类型为县区
                    result = Utility.handleCountyResponse(responseText, selectedCity.getId());
                }
                if(result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog(); //关闭进度对话框
                            if("province".equals(type)){
                                queryProvinces();    //本地查询省份的信息并显示
                            }else if("city".equals(type)){
                                queryCities();
                            }else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }

    //显示进度对话框
    private void showProgressDialog(){
        if(progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    //关闭进度对话框
    private void closeProgressDialog(){
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }
}
