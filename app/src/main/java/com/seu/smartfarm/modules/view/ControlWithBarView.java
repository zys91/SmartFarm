package com.seu.smartfarm.modules.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.Nullable;

import com.seu.smartfarm.R;


public class ControlWithBarView extends LinearLayout {
    private boolean isEnableBar;
    private TextView tv_controlTitle;
    private ToggleButton btn_switch;
    private SeekBar bar_percent;
    private TextView tv_controlLastUpdate;
    private itemClickListener listener;
    private TextView tv_showPercent;
    private boolean disableCallback;

    public ControlWithBarView(Context context) {
        this(context, null);
    }

    public ControlWithBarView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ControlWithBarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //添加布局文件
        View view = LayoutInflater.from(context).inflate(R.layout.item_controlwithbar, null);
        addView(view);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ControlWithBarView);
        //找到控件
        tv_controlTitle = findViewById(R.id.control_bar_title);
        btn_switch = findViewById(R.id.btn_control);
        bar_percent = findViewById(R.id.bar_control);
        tv_controlLastUpdate = findViewById(R.id.controlLastUpdate);
        tv_showPercent = findViewById(R.id.show_percent);

        isEnableBar = ta.getBoolean(R.styleable.ControlWithBarView_enable_bar, true);
        tv_controlTitle.setText(ta.getString(R.styleable.ControlWithBarView_control_bar_title));
        bar_percent.setMin(ta.getInt(R.styleable.ControlWithBarView_bar_minvalue, 0));
        bar_percent.setMax(ta.getInt(R.styleable.ControlWithBarView_bar_maxvalue, 100));

        //给开关设置点击事件
        btn_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (isEnableBar) {
                        setShowBar(true);
                    }
                    if (!disableCallback)
                        listener.itemClick("on");
                    else
                        disableCallback = false;
                } else {
                    if (isEnableBar) {
                        setShowBar(false);
                    }
                    if (!disableCallback)
                        listener.itemClick("off");
                    else
                        disableCallback = false;
                }
            }
        });

        //给进度条设置点击事件
        bar_percent.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv_showPercent.setText(String.format("%d%%", progress));
                listener.itemClick("progress:" + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                listener.itemClick("start");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                listener.itemClick("end");
            }
        });

        ta.recycle();
    }

    public void setSwitchClickable(boolean Clickable) {
        btn_switch.setClickable(Clickable);
    }

    public void setBarClickable(boolean Clickable) {
        bar_percent.setClickable(Clickable);
    }

    public void setUpdateTime(String value) {
        tv_controlLastUpdate.setText(value);
    }

    public void setShowBar(boolean value) {
        bar_percent.setVisibility(value ? View.VISIBLE : View.INVISIBLE);
        tv_showPercent.setVisibility(value ? View.VISIBLE : View.INVISIBLE);
    }

    public int getBarProgress() {
        return bar_percent.getProgress();
    }

    public void setBarProgress(int value, boolean disableCB) {
        if (value == 0) {
            if (btn_switch.isChecked()) {
                disableCallback = disableCB;
                btn_switch.setChecked(false);
            }
        } else {
            bar_percent.setProgress(value);
            if (!btn_switch.isChecked()) {
                disableCallback = disableCB;
                btn_switch.setChecked(true);
            }
        }
    }

    public void setItemClickListener(itemClickListener listener) {
        this.listener = listener;
    }

    public interface itemClickListener {
        void itemClick(String text);
    }
}
