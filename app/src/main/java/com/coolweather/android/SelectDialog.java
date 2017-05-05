package com.coolweather.android;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;

/*
 * Created by xiaofeng on 2017/5/5.
 */

public class SelectDialog extends Dialog {

    private NumberPicker numberPicker;
    private Button positiveButton;
    private Button negativeButton;

    public SelectDialog(Context context) {
        super(context);
        setSelectDialog();
    }

    private void setSelectDialog() {
        View mView = LayoutInflater.from(getContext()).inflate(R.layout.activity_select, null);
        numberPicker = (NumberPicker) mView.findViewById(R.id.select_refresh_num);
        numberPicker.setMaxValue(24);
        numberPicker.setMinValue(1);
        numberPicker.setValue(8);
        numberPicker.setEnabled(true);
        positiveButton = (Button) mView.findViewById(R.id.confirm);
        negativeButton = (Button) mView.findViewById(R.id.cancel);
        super.setContentView(mView);
    }

    public void setOnPositiveListener(View.OnClickListener listener){
        positiveButton.setOnClickListener(listener);
    }

    public void setOnNegativeListener(View.OnClickListener listener){
        negativeButton.setOnClickListener(listener);
    }

    public int getRefreshNumber(){
        return numberPicker.getValue();
    }

}
