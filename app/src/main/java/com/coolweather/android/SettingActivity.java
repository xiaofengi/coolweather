package com.coolweather.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.coolweather.android.service.AutoUpdateService;

public class SettingActivity extends AppCompatActivity {

    CheckBox isAutoRefresh;

    EditText refNumber;

    Button confButton;

    Button canButton;

    SharedPreferences pref;

    SharedPreferences.Editor editor;

    boolean before_is;

    int before_num;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        //缓存前值
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = pref.edit();
        before_is = pref.getBoolean("is_auto_refresh", true);
        before_num = pref.getInt("refresh_num", 8);

        isAutoRefresh = (CheckBox)findViewById(R.id.is_auto_refresh);
        //checkbox监听
        isAutoRefresh.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isAutoRefresh.isChecked()){
                    editor.putBoolean("is_auto_refresh", true);
                    editor.apply();
                }else {
                    editor.putBoolean("is_auto_refresh", false);
                    editor.apply();
                }
            }
        });

        refNumber = (EditText)findViewById(R.id.refresh_num);
        confButton = (Button)findViewById(R.id.confirm);
        canButton = (Button)findViewById(R.id.cancel);

        //显示原来值
        isAutoRefresh.setChecked(before_is);
        refNumber.setText(String.valueOf(before_num));

        //按下取消按钮重置值
        canButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putBoolean("is_auto_refresh", before_is);
                editor.apply();
                finish();
            }
        });

        //按下确定按钮
        confButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //值改变则存入
                if(Integer.parseInt(refNumber.getText().toString()) != before_num) {
                    editor.putInt("refresh_num", Integer.valueOf(refNumber.getText().toString()));
                    editor.apply();
                }
                //状态不变，值不变则直接结束
                if( isAutoRefresh.isChecked() == before_is &&
                        Integer.parseInt(refNumber.getText().toString()) == before_num){
                    finish();
                }
                //当前选中自动更新并且值不一样或者以前是不更新的则启动服务
                if(isAutoRefresh.isChecked() &&
                        (Integer.parseInt(refNumber.getText().toString()) != before_num) || !before_is){
                    Intent intent = new Intent(SettingActivity.this, AutoUpdateService.class);
                    startService(intent);
                    finish();
                }
                //当前为不后台更新并且以前是更新的则关闭服务
                if(!isAutoRefresh.isChecked() && before_is) {
                    Intent intent = new Intent(SettingActivity.this, AutoUpdateService.class);
                    stopService(intent);
                    finish();
                }
            }
        });
    }
}
