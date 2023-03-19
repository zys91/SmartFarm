package com.seu.smartfarm.modules.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.seu.smartfarm.R;


public class StatusView extends LinearLayout {
    private boolean isShowRendererPrefix;
    private boolean isShowRendererSuffix;
    private boolean isValueClickable;
    private TextView tv_statusTitle;
    private TextView tv_RendererPrefix;
    private TextView tv_RendererValue;
    private TextView tv_RendererSuffix;
    private TextView tv_statusLastUpdate;
    private itemClickListener listener;

    public StatusView(Context context) {
        this(context, null);
    }

    public StatusView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StatusView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //添加布局文件
        View view = LayoutInflater.from(context).inflate(R.layout.item_status, null);
        addView(view);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.StatusView);
        //找到控件
        tv_statusTitle = findViewById(R.id.statusTitle);
        tv_RendererPrefix = findViewById(R.id.textRendererPrefix);
        tv_RendererValue = findViewById(R.id.textRendererValue);
        tv_RendererSuffix = findViewById(R.id.textRendererSuffix);
        tv_statusLastUpdate = findViewById(R.id.statusLastUpdate);

        isShowRendererPrefix = ta.getBoolean(R.styleable.StatusView_show_value_prefix, false);
        isShowRendererSuffix = ta.getBoolean(R.styleable.StatusView_show_value_suffix, true);
        isValueClickable = ta.getBoolean(R.styleable.StatusView_value_clickable, false);
        tv_statusTitle.setText(ta.getString(R.styleable.StatusView_status_title));
        tv_RendererSuffix.setText(ta.getString(R.styleable.StatusView_value_suffix));
        tv_RendererPrefix.setVisibility(isShowRendererPrefix ? View.VISIBLE : View.INVISIBLE);
        tv_RendererSuffix.setVisibility(isShowRendererSuffix ? View.VISIBLE : View.INVISIBLE);
        tv_RendererValue.setClickable(isValueClickable);
        if (isValueClickable)
            tv_RendererValue.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.itemClick("clicked");
                }
            });
        ta.recycle();
    }

    public void setStatusValue(String value) {
        tv_RendererValue.setText(value);
    }

    public void setStatusValuePrefix(String value) {
        tv_RendererPrefix.setText(value);
    }

    public void setStatusValueUnit(String value) {
        tv_RendererSuffix.setText(value);
    }

    public void setUpdateTime(String value) {
        tv_statusLastUpdate.setText(value);
    }

    public void setItemClickListener(itemClickListener listener) {
        this.listener = listener;
    }

    public interface itemClickListener {
        void itemClick(String text);
    }
}
