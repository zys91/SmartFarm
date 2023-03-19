package com.seu.smartfarm.modules.widget.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.seu.smartfarm.R;
import com.seu.smartfarm.fragment.account.Account;

import java.io.FileNotFoundException;
import java.io.OutputStream;

public class SetNetDialog extends DialogFragment implements View.OnClickListener {
    private Button btn;
    private ImageView iv_quit;
    private ImageView iv_pic;

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
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_setnetwork, null);
        iv_quit = view.findViewById(R.id.iv_setNet_quit);
        iv_pic = view.findViewById(R.id.iv_setNet_pic);
        btn = view.findViewById(R.id.btn_save_pic);
        iv_quit.setOnClickListener(this);
        btn.setOnClickListener(this);
        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_save_pic) {
            iv_pic.buildDrawingCache();
            Bitmap bitmap = iv_pic.getDrawingCache();
            saveImage(bitmap);
        } else if (v.getId() == R.id.iv_setNet_quit) {
            dismiss();
        }
    }

    /**
     * API29 中的最新保存图片到相册的方法
     */
    private void saveImage(Bitmap toBitmap) {
        //开始一个新的进程执行保存图片的操作
        Uri insertUri = getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
        //使用use可以自动关闭流
        try {
            OutputStream outputStream = getContext().getContentResolver().openOutputStream(insertUri, "rw");
            if (toBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)) {
                Toast.makeText(getActivity(), "图片保存成功！", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                getTargetFragment().onActivityResult(Account.REQUEST_CODE_SavePic, Activity.RESULT_OK, intent);
            } else {
                Toast.makeText(getActivity(), "图片保存失败！请自行截图保存！", Toast.LENGTH_SHORT).show();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
