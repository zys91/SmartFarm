package com.seu.smartfarm.modules.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.Nullable;

import com.seu.smartfarm.R;


public class ControlWithBtnView extends LinearLayout {
    private boolean isEnableBtn;
    private TextView tv_controlTitle;
    private ToggleButton btn_switch;
    private Button btn_setting;
    private TextView tv_controlLastUpdate;
    private itemClickListener listener;
    private boolean disableCallback;

    public ControlWithBtnView(Context context) {
        this(context, null);
    }

    public ControlWithBtnView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ControlWithBtnView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //添加布局文件
        View view = LayoutInflater.from(context).inflate(R.layout.item_controlwithbtn, null);
        addView(view);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ControlWithBtnView);
        //找到控件
        tv_controlTitle = findViewById(R.id.control_btn_title);
        btn_switch = findViewById(R.id.btn_control);
        btn_setting = findViewById(R.id.btn_setting);
        tv_controlLastUpdate = findViewById(R.id.controlLastUpdate);

        isEnableBtn = ta.getBoolean(R.styleable.ControlWithBtnView_enable_btn, true);
        tv_controlTitle.setText(ta.getString(R.styleable.ControlWithBtnView_control_btn_title));
        btn_setting.setText(ta.getString(R.styleable.ControlWithBtnView_btn_text));

        //给开关设置点击事件
        btn_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (isEnableBtn) {
                        setShowBtn(true);
                    }
                    if (!disableCallback)
                        listener.itemClick("on");
                    else
                        disableCallback = false;
                } else {
                    if (isEnableBtn) {
                        setShowBtn(false);
                    }
                    if (!disableCallback)
                        listener.itemClick("off");
                    else
                        disableCallback = false;
                }
            }
        });
        btn_setting.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.itemClick("clicked");
            }
        });

        ta.recycle();
    }

    public void setSwitchClickable(boolean Clickable) {
        btn_switch.setClickable(Clickable);
    }

    public void setBtnClickable(boolean Clickable) {
        btn_setting.setClickable(Clickable);
    }

    public void setUpdateTime(String value) {
        tv_controlLastUpdate.setText(value);
    }

    public void setShowBtn(boolean value) {
        btn_setting.setVisibility(value ? View.VISIBLE : View.INVISIBLE);
    }

    public void setSwitchStatus(boolean status, boolean disableCB) {
        if (!status) {
            if (btn_switch.isChecked()) {
                disableCallback = disableCB;
                btn_switch.setChecked(false);
            }
        } else {
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
