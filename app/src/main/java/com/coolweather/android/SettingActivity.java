package com.coolweather.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import com.coolweather.android.service.AutoUpdateService;

public class SettingActivity extends AppCompatActivity {

    Switch isAutoRefresh;           //选择自动刷新

    public TextView refNumberText;  //显示刷新时间

    RelativeLayout refreshLayout;   //选择刷新时间布局

    SharedPreferences pref;

    SharedPreferences.Editor editor;

    boolean refresh_is;              //是否自动刷新

    public static int refresh_num;   //刷新时间

    Button backButtonSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        //缓存前值
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = pref.edit();
        refresh_is = pref.getBoolean("is_auto_refresh", true);
        refresh_num = pref.getInt("refresh_num", 8);

        //switch监听
        isAutoRefresh = (Switch)findViewById(R.id.is_auto_refresh);
        isAutoRefresh.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Intent intent = new Intent(SettingActivity.this, AutoUpdateService.class);
                if (isChecked){
                    refresh_is = true;
                    editor.putBoolean("is_auto_refresh", true);
                    editor.apply();
                    startService(intent);
                }else {
                    refresh_is = false;
                    editor.putBoolean("is_auto_refresh", false);
                    editor.apply();
                    stopService(intent);
                }
            }
        });

        //显示原来值
        refNumberText = (TextView)findViewById(R.id.refresh_num);
        isAutoRefresh.setChecked(refresh_is);
        refNumberText.setText(String.valueOf(refresh_num));

        //选择刷新时间
        refreshLayout = (RelativeLayout)findViewById(R.id.layout_refresh);
        refreshLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SelectDialog dialog = new SelectDialog(SettingActivity.this);
                //按下确定键
                dialog.setOnPositiveListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int input_refresh_num = dialog.getRefreshNumber();
                        if(refresh_num != input_refresh_num) {    //输入的更新值是否和当前的更新值相等
                            refresh_num = input_refresh_num;      //不相等则更新
                            refNumberText.setText(String.valueOf(refresh_num));
                            editor.putInt("refresh_num", refresh_num);
                            editor.apply();
                            if(refresh_is){      //如果当前为自动更新则重新开启后台更新服务
                                Intent intent = new Intent(SettingActivity.this, AutoUpdateService.class);
                                startService(intent);
                            }
                        }
                        dialog.dismiss();
                    }
                });
                //按下取消键
                dialog.setOnNegativeListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        backButtonSetting = (Button)findViewById(R.id.back_button_setting);
        backButtonSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
