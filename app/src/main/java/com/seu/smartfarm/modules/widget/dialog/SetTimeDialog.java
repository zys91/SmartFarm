package com.seu.smartfarm.modules.widget.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.seu.smartfarm.R;
import com.seu.smartfarm.fragment.panel.ControlPanel;

public class SetTimeDialog extends DialogFragment implements View.OnClickListener {
    private Button btn;
    private ImageView iv_quit;
    private TimePicker mStartTime;
    private TimePicker mEndTime;
    private int sh;
    private int sm;
    private int eh;
    private int em;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //设置背景透明
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_settime, null);
        iv_quit = view.findViewById(R.id.iv_setTime_quit);
        btn = view.findViewById(R.id.btn_save_setting);
        mStartTime = view.findViewById(R.id.startTime);
        mEndTime = view.findViewById(R.id.endTime);
        mStartTime.setIs24HourView(true);
        mEndTime.setIs24HourView(true);
        iv_quit.setOnClickListener(this);
        btn.setOnClickListener(this);
        mStartTime.setHour(sh);
        mStartTime.setMinute(sm);
        mEndTime.setHour(eh);
        mEndTime.setMinute(em);

        mStartTime.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {  //获取当前选择的时间
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                sh = hourOfDay;
                sm = minute;
            }
        });
        mEndTime.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {  //获取当前选择的时间
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                eh = hourOfDay;
                em = minute;
            }
        });


        builder.setView(view);
        return builder.create();
    }

    public void setData(int nsh, int nsm, int neh, int nem) {
        sh = nsh;
        sm = nsm;
        eh = neh;
        em = nem;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_setTime_quit) {
            dismiss();
        } else if (v.getId() == R.id.btn_save_setting) {
            if (getTargetFragment() == null) {
                return;
            }
            Intent intent = new Intent();
            intent.putExtra("startHour", sh);
            intent.putExtra("startMin", sm);
            intent.putExtra("endHour", eh);
            intent.putExtra("endMin", em);
            getTargetFragment().onActivityResult(ControlPanel.REQUEST_CODE_SetTime, Activity.RESULT_OK, intent);
        }
    }


}
