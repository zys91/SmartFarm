package com.seu.smartfarm.fragment.account;

import static android.content.Context.MODE_PRIVATE;
import static com.seu.smartfarm.activity.LoginActivity.What_NetworkErr;
import static com.seu.smartfarm.app.MainApplication.app_did;
import static com.seu.smartfarm.app.MainApplication.app_tel;
import static com.seu.smartfarm.app.MainApplication.app_token;
import static com.seu.smartfarm.app.MainApplication.app_uid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.seu.smartfarm.R;
import com.seu.smartfarm.activity.LoginActivity;
import com.seu.smartfarm.activity.NavigationActivity;
import com.seu.smartfarm.modules.glide.GlideApp;
import com.seu.smartfarm.modules.mqtt.Publish;
import com.seu.smartfarm.modules.userapi.TAutoUpdater;
import com.seu.smartfarm.modules.userapi.TChangePwd;
import com.seu.smartfarm.modules.userapi.TLogout;
import com.seu.smartfarm.modules.userapi.TUnbindDevice;
import com.seu.smartfarm.modules.view.InfoView;
import com.seu.smartfarm.modules.widget.dialog.ChangePwdDialog;
import com.seu.smartfarm.modules.widget.dialog.SetNetDialog;
import com.seu.smartfarm.modules.widget.dialog.UnbindDeviceDialog;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.Objects;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class Account extends Fragment {
    public final static int REQUEST_CODE_ChangePwd = 110;
    public final static int REQUEST_CODE_UnbindDevice = 120;
    public final static int REQUEST_CODE_SavePic = 130;
    public final static int What_ChangeSuccess = 3000;
    public final static int What_ChangeFailed = 3010;
    public final static int What_UserError = 3015;
    public final static int What_UnbindSuccess = 3020;
    public final static int What_UnbindFailed = 3025;
    public final static int What_LogoutSuccess = 3030;
    private final static String TAG = "Account";
    private ImageView mHBack;
    private ImageView mHHead;
    private InfoView mPass;
    private InfoView mPhone;
    private InfoView mDevice;
    private InfoView mLogout;
    private InfoView mAbout;
    private InfoView mNet;
    private View root;
    private ChangePwdDialog changePwdDialog;
    private UnbindDeviceDialog unbindDeviceDialog;
    private SetNetDialog setNetDialog;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case What_ChangeSuccess:
                    Toast.makeText(getActivity(), "密码修改成功!", Toast.LENGTH_SHORT).show();
                    break;
                case What_ChangeFailed:
                    Toast.makeText(getActivity(), "修改失败！原密码错误！", Toast.LENGTH_SHORT).show();
                    break;
                case What_NetworkErr:
                    Toast.makeText(getActivity(), "网络连接异常，请检查网络连接！", Toast.LENGTH_SHORT).show();
                    break;
                case What_UserError:
                    Toast.makeText(getActivity(), "用户登录已过期，请重新登录！", Toast.LENGTH_SHORT).show();
                    break;
                case What_UnbindSuccess: {
                    Toast.makeText(getActivity(), "设备解绑成功！", Toast.LENGTH_SHORT).show();
                    deactivateDevice();
                    app_did = "";
                    mDevice.setRightDesc(app_did);
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences("get_token", MODE_PRIVATE).edit();
                    editor.putString("did", app_did);
                    editor.apply();
                    break;
                }
                case What_UnbindFailed:
                    Toast.makeText(getActivity(), "设备解绑失败！", Toast.LENGTH_SHORT).show();
                    break;
                case What_LogoutSuccess: {
                    Toast.makeText(getActivity(), "账号退出成功，请重新登录！", Toast.LENGTH_SHORT).show();
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences("get_token", MODE_PRIVATE).edit();
                    editor.clear();
                    editor.apply();
                    app_uid = "";
                    app_tel = "";
                    app_token = "";
                    app_did = "";
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                    break;
                }
            }
            return false;
        }
    });

    public static String getLocalVersion(Context ctx) {
        String localVersion = "0.0.0";
        try {
            PackageInfo packageInfo = ctx.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(ctx.getPackageName(), 0);
            localVersion = packageInfo.versionName;
            Log.d(TAG, "当前版本: " + localVersion);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return localVersion;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_account, container, false);
        initView();
        return root;
    }

    private void initView() {
        //顶部头像控件
        mHBack = root.findViewById(R.id.h_back);
        mHHead = root.findViewById(R.id.h_head);
        TextView mUserName = root.findViewById(R.id.user_name);
        TextView mUserVal = root.findViewById(R.id.user_val);
        //下面item控件
        mPass = root.findViewById(R.id.user_pass);
        mPhone = root.findViewById(R.id.user_phone);
        mDevice = root.findViewById(R.id.user_device);
        mLogout = root.findViewById(R.id.user_logout);
        mAbout = root.findViewById(R.id.about);
        mNet = root.findViewById(R.id.set_network);
        mUserName.setText(app_uid);
        mUserVal.setText("新手上路");
        mPhone.setRightDesc(app_tel);
        mDevice.setRightDesc(app_did);
        mAbout.setRightDesc(getLocalVersion(root.getContext()));
        mNet.setRightDesc("点击配网");

        //设置背景磨砂效果
        GlideApp.with(this).load(R.drawable.backpic)
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(25, 3)).centerCrop())
                .into(mHBack);
        //设置圆形图像
        GlideApp.with(this).load(R.drawable.headpic)
                .apply(RequestOptions.bitmapTransform(new CircleCrop()).placeholder(R.drawable.headpic))
                .into(mHHead);

        mPass.setItemClickListener(new InfoView.itemClickListener() {
            @Override
            public void itemClick(String text) {
                //Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
                changePwdDialog = new ChangePwdDialog();
                changePwdDialog.setTargetFragment(Account.this, REQUEST_CODE_ChangePwd);
                changePwdDialog.show(getFragmentManager(), "ChangePwd");
            }
        });
        mPhone.setItemClickListener(new InfoView.itemClickListener() {
            @Override
            public void itemClick(String text) {
                Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
            }
        });
        mDevice.setItemClickListener(new InfoView.itemClickListener() {
            @Override
            public void itemClick(String text) {
                //Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
                if (app_did.isEmpty()) {
                    Toast.makeText(getActivity(), "您还没有绑定的设备，先去绑定吧!", Toast.LENGTH_SHORT).show();
                } else {
                    unbindDeviceDialog = new UnbindDeviceDialog();
                    unbindDeviceDialog.setTargetFragment(Account.this, REQUEST_CODE_UnbindDevice);
                    unbindDeviceDialog.show(getFragmentManager(), "UnbindDevice");
                }
            }
        });
        mLogout.setItemClickListener(new InfoView.itemClickListener() {
            @Override
            public void itemClick(String text) {
                Thread aLogoutThread = new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        Map<String, String> LogoutRes = TLogout.logoutUser(getActivity(), app_uid, app_token);
                        Message msg = new Message();
                        if (LogoutRes != null) {
                            if (Objects.equals(LogoutRes.get("status"), "1"))
                                msg.what = What_LogoutSuccess;
                            else if (Objects.equals(LogoutRes.get("status"), "0"))
                                msg.what = What_LogoutSuccess;
                        } else
                            msg.what = What_NetworkErr;
                        mHandler.sendMessage(msg);
                    }
                };
                aLogoutThread.setDaemon(true);
                aLogoutThread.start();
            }
        });
        mAbout.setItemClickListener(new InfoView.itemClickListener() {
            @Override
            public void itemClick(String text) {
//                Toast.makeText(getActivity(), "当前版本: " + text, Toast.LENGTH_SHORT).show();
                //自动更新
                TAutoUpdater manager = new TAutoUpdater(getContext());
                manager.CheckUpdate();
            }
        });
        mNet.setItemClickListener(new InfoView.itemClickListener() {
            @Override
            public void itemClick(String text) {
                setNetDialog = new SetNetDialog();
                setNetDialog.setTargetFragment(Account.this, REQUEST_CODE_SavePic);
                setNetDialog.show(getFragmentManager(), "setNet");
            }
        });
    }

    void deactivateDevice() {
        ((NavigationActivity) getActivity()).unsubscribeDevice();

        JSONObject messageData = new JSONObject();
        try {
            messageData.put("uid", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final Publish publish = new Publish(String.format("topic/%s/active", app_did), messageData.toString(), 1, true);
        ((NavigationActivity) getActivity()).publish(publish, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Toast.makeText(getActivity(), "发布失败！", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ChangePwd && resultCode == Activity.RESULT_OK) {
            try {
                String originPwd = data.getStringExtra("OriginPassword");
                String newPwd = data.getStringExtra("NewPassword");
                changePwdDialog.dismiss();
                Thread aChangePwdThread = new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        Map<String, String> ChangeRes = TChangePwd.changeUserPwd(getActivity(), app_uid, originPwd, newPwd);
                        Message msg = new Message();
                        if (ChangeRes != null) {
                            if (Objects.equals(ChangeRes.get("status"), "1"))
                                msg.what = What_ChangeSuccess;
                            else if (Objects.equals(ChangeRes.get("status"), "2"))
                                msg.what = What_ChangeFailed;
                            else if (Objects.equals(ChangeRes.get("status"), "0"))
                                msg.what = What_UserError;
                        } else
                            msg.what = What_NetworkErr;
                        mHandler.sendMessage(msg);
                    }
                };
                aChangePwdThread.setDaemon(true);
                aChangePwdThread.start();
            } catch (Exception er) {
                Log.e(TAG, "changePwd: " + er.getMessage());
            }
        } else if (requestCode == REQUEST_CODE_UnbindDevice && resultCode == Activity.RESULT_OK) {
            try {
                unbindDeviceDialog.dismiss();
                Thread aUnbindThread = new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        Map<String, String> UnbindRes = TUnbindDevice.unbindUserDevice(getActivity(), app_uid, app_token, app_did);
                        Message msg = new Message();
                        if (UnbindRes != null) {
                            if (Objects.equals(UnbindRes.get("status"), "1"))
                                msg.what = What_UnbindSuccess;
                            else if (Objects.equals(UnbindRes.get("status"), "0"))
                                msg.what = What_UnbindFailed;
                        } else
                            msg.what = What_NetworkErr;
                        mHandler.sendMessage(msg);
                    }
                };
                aUnbindThread.setDaemon(true);
                aUnbindThread.start();
            } catch (Exception er) {
                Log.e(TAG, "unbindDevice: " + er.getMessage());
            }
        } else if (requestCode == REQUEST_CODE_SavePic && resultCode == Activity.RESULT_OK) {
            setNetDialog.dismiss();
        }
    }
}
