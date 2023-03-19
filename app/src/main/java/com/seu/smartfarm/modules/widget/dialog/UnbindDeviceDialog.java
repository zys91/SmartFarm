package com.seu.smartfarm.modules.widget.dialog;

import static com.seu.smartfarm.app.MainApplication.app_did;

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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.seu.smartfarm.R;
import com.seu.smartfarm.fragment.account.Account;

public class UnbindDeviceDialog extends DialogFragment implements View.OnClickListener {
    private TextView tvDevice;
    private Button btn;
    private ImageView iv;

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
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_unbinddevice, null);
        iv = view.findViewById(R.id.iv_unbind_quit);
        tvDevice = view.findViewById(R.id.tv_device_hint);
        btn = view.findViewById(R.id.btn_unbind_submit);
        iv.setOnClickListener(this);
        tvDevice.setText(app_did);
        btn.setOnClickListener(this);
        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_unbind_submit) {
            if (getTargetFragment() == null) {
                return;
            }
            Intent intent = new Intent();
            getTargetFragment().onActivityResult(Account.REQUEST_CODE_UnbindDevice, Activity.RESULT_OK, intent);
        } else if (v.getId() == R.id.iv_unbind_quit) {
            dismiss();
        }
    }
}
