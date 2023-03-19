package com.seu.smartfarm.fragment.panel;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static com.seu.smartfarm.activity.LoginActivity.What_NetworkErr;
import static com.seu.smartfarm.app.MainApplication.app_did;
import static com.seu.smartfarm.app.MainApplication.app_token;
import static com.seu.smartfarm.app.MainApplication.app_uid;
import static com.seu.smartfarm.fragment.account.Account.What_UserError;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.seu.smartfarm.R;
import com.seu.smartfarm.activity.NavigationActivity;
import com.seu.smartfarm.activity.ScanActivity;
import com.seu.smartfarm.modules.asynctask.BitmapWorkerTask;
import com.seu.smartfarm.modules.mqtt.Publish;
import com.seu.smartfarm.modules.userapi.TBindDevice;
import com.seu.smartfarm.modules.userapi.TCheckDevice;
import com.seu.smartfarm.modules.view.ControlWithBarView;
import com.seu.smartfarm.modules.view.ControlWithBtnView;
import com.seu.smartfarm.modules.view.StatusView;
import com.seu.smartfarm.modules.widget.dialog.LineChartDialog;
import com.seu.smartfarm.modules.widget.dialog.SetTimeDialog;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class ControlPanel extends Fragment {
    public final static int REQUEST_CODE_SetTime = 140;
    public final static int What_BindSuccess = 3035;
    public final static int What_BindFailed = 3040;
    public final static int What_CheckOnline = 3045;
    public final static int What_CheckOffline = 3050;
    public final static int What_CheckUnknown = 3055;
    private final static String TAG = "ControlPanel";
    private final static int PERMISSION_REQUESTCODE = 1024;
    private final static int SCAN_REQUEST_CODE = 100;
    private View root;
    private String scanResult;
    private LinearLayout frmBind;
    private LinearLayout frmControlPanel;
    private LinearLayout frmManual;
    private LinearLayout frmAuto;
    private int mViewStatus = 0;  //0-bind ; 1-controlPanel;
    private int mControlViewStatus = 0;  //0-manual ; 1-auto;
    private boolean disableCallback = false;
    private int startHour = 22;
    private int startMin = 0;
    private int endHour = 8;
    private int endMin = 0;
    private boolean sDeviceOnline = false;
    private TextView mTVDeviceID;
    private TextView mTVDeviceOnlineStatus;
    private StatusView mDeviceTemp;
    private StatusView mDeviceHumi;
    private StatusView mDeviceLux;
    private StatusView mDeviceWeight;
    private StatusView mDeviceSoilTemp;
    private StatusView mDeviceSoilHumi;
    private StatusView mDeviceSoilEC;
    private StatusView mDeviceSoilPH;
    private StatusView mDeviceWaterHeight;
    private ControlWithBarView mDeviceLight;
    private ControlWithBarView mDeviceFan;
    private ControlWithBarView mDeviceWaterPump;
    private ControlWithBtnView mDeviceNightMode;
    private ImageView mDeviceView;
    private Button mBtnGetLivePic;
    private ToggleButton mBtnModeChange;
    private Timer UpdateDataTimer;
    private Timer lastUpdateTimer;
    private Date lastUpdateDate;
    private SetTimeDialog setTimeDialog;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_panel, container, false);
        initView();
        return root;
    }

    private void initView() {
        Button mBtnScan = root.findViewById(R.id.btn_scan);
        frmBind = root.findViewById(R.id.frameBind);
        frmControlPanel = root.findViewById(R.id.frameControlPanel);
        frmManual = root.findViewById(R.id.manual_operate);
        frmAuto = root.findViewById(R.id.automatic_operate);
        mTVDeviceID = root.findViewById(R.id.device_id);
        mTVDeviceOnlineStatus = root.findViewById(R.id.device_online_status);
        mDeviceTemp = root.findViewById(R.id.device_temp);
        mDeviceHumi = root.findViewById(R.id.device_humi);
        mDeviceLux = root.findViewById(R.id.device_lux);
        mDeviceWeight = root.findViewById(R.id.device_weight);
        mDeviceSoilTemp = root.findViewById(R.id.device_soilTemp);
        mDeviceSoilHumi = root.findViewById(R.id.device_soilHumi);
        mDeviceSoilEC = root.findViewById(R.id.device_soilEC);
        mDeviceSoilPH = root.findViewById(R.id.device_soilPH);
        mDeviceWaterHeight = root.findViewById(R.id.device_waterHeight);
        mDeviceLight = root.findViewById(R.id.device_light);
        mDeviceFan = root.findViewById(R.id.device_fan);
        mDeviceWaterPump = root.findViewById(R.id.device_waterPump);
        mDeviceView = root.findViewById(R.id.device_view);
        mDeviceNightMode = root.findViewById(R.id.device_nightMode);
        mBtnGetLivePic = root.findViewById(R.id.btn_live_get);
        mBtnModeChange = root.findViewById(R.id.btn_controlMode);
        swipeRefreshLayout = root.findViewById(R.id.srl);
        swipeRefreshLayout.setColorSchemeResources(R.color.blue);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            refreshDeviceStatus();
            swipeRefreshLayout.setRefreshing(false);
        });
        mBtnScan.setOnClickListener(v -> {
            List<String> permissionLists = new ArrayList<>();

            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                permissionLists.add(Manifest.permission.CAMERA);
            }
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionLists.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }

            if (!permissionLists.isEmpty()) {
                ActivityCompat.requestPermissions(getActivity(), permissionLists.toArray(new String[permissionLists.size()]), PERMISSION_REQUESTCODE);
            } else {
                Intent intent = new Intent(getActivity(), ScanActivity.class);
                startActivityForResult(intent, SCAN_REQUEST_CODE);
            }
        });

        //声明及实现接口的方法
        NavigationActivity.MyOnReceive myOnReceive = new NavigationActivity.MyOnReceive() {
            @Override
            public void myListener(com.seu.smartfarm.modules.mqtt.Message msg) {
                setData(msg);
            }
        };
        ((NavigationActivity) getActivity()).setMqttMessageOnReceive(myOnReceive);

        mDeviceTemp.setItemClickListener(text -> {
            if (text.equals("clicked")) {
                getChartData("箱内温度","temp", "1d");
            }
        });
        mDeviceHumi.setItemClickListener(text -> {
            if (text.equals("clicked")) {
                getChartData("箱内湿度","humi", "1d");
            }
        });
        mDeviceLux.setItemClickListener(text -> {
            if (text.equals("clicked")) {
                getChartData("光照强度","lux", "1d");
            }
        });
        mDeviceWeight.setItemClickListener(text -> {
            if (text.equals("clicked")) {
                getChartData("植物称重","weight", "1d");
            }
        });
        mDeviceSoilTemp.setItemClickListener(text -> {
            if (text.equals("clicked")) {
                getChartData("土壤温度","soilTemp", "1d");
            }
        });
        mDeviceSoilHumi.setItemClickListener(text -> {
            if (text.equals("clicked")) {
                getChartData("土壤湿度","soilHumi", "1d");
            }
        });
        mDeviceSoilEC.setItemClickListener(text -> {
            if (text.equals("clicked")) {
                getChartData("土壤EC","soilEC", "1d");
            }
        });
        mDeviceSoilPH.setItemClickListener(text -> {
            if (text.equals("clicked")) {
                getChartData("土壤PH","soilPH", "1d");
            }
        });
        mDeviceWaterHeight.setItemClickListener(text -> {
            if (text.equals("clicked")) {
                getChartData("水位深度","waterHeight", "1d");
            }
        });
        mDeviceLight.setItemClickListener(text -> {
            switch (text) {
                case "on":
                    mDeviceLight.setBarProgress(50, false);
                    sendCmd("3", "50", true);
                    break;
                case "off":
                    sendCmd("3", "0", true);
                    break;
                case "end":
                    if (mDeviceLight.getBarProgress() == 0) {
                        mDeviceLight.setBarProgress(0, true);
                        sendCmd("3", "0", true);
                    } else
                        sendCmd("3", Integer.toString(mDeviceLight.getBarProgress()), true);
                    break;
            }
        });
        mDeviceFan.setItemClickListener(text -> {
            switch (text) {
                case "on":
                    mDeviceFan.setBarProgress(90, false);
                    sendCmd("4", "90", true);
                    break;
                case "off":
                    sendCmd("4", "0", true);
                    break;
                case "end":
                    if (mDeviceFan.getBarProgress() == 0) {
                        mDeviceFan.setBarProgress(0, true);
                        sendCmd("4", "0", true);
                    } else
                        sendCmd("4", Integer.toString(mDeviceFan.getBarProgress()), true);
                    break;
            }
        });
        mDeviceWaterPump.setItemClickListener(text -> {
            if (text.equals("on")) {
                sendCmd("5", "100", true);
            } else if (text.equals("off")) {
                sendCmd("5", "0", true);
            }
        });
        mDeviceNightMode.setItemClickListener(text -> {
            if (text.equals("on")) {
                sendCmd("7", "1", true);
            } else if (text.equals("off")) {
                sendCmd("7", "0", true);
            } else if (text.equals("clicked")) {
                setTimeDialog = new SetTimeDialog();
                setTimeDialog.setTargetFragment(ControlPanel.this, REQUEST_CODE_SetTime);
                setTimeDialog.setData(startHour, startMin, endHour, endMin);
                setTimeDialog.show(getFragmentManager(), "setTime");
            }
        });
        mBtnModeChange.setOnCheckedChangeListener((compoundButton, isChecked) -> {
                    if (isChecked) {
                        mBtnModeChange.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                        setControlViewStatus(1);
                        if (!disableCallback)
                            sendCmd("6", "1", true);
                        else
                            disableCallback = false;
                    } else {
                        mBtnModeChange.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                        mDeviceLight.setBarProgress(0, true);
                        mDeviceFan.setBarProgress(0, true);
                        mDeviceWaterPump.setBarProgress(0, true);
                        setControlViewStatus(0);
                        if (!disableCallback)
                            sendCmd("6", "0", true);
                        else
                            disableCallback = false;
                    }
                }
        );
        mBtnGetLivePic.setOnClickListener(view -> {
                    if (sDeviceOnline) {
                        mBtnGetLivePic.setVisibility(View.INVISIBLE);
                        sendCmd("2", "null", false);
                    } else {
                        Toast.makeText(getActivity(), "设备不在线，操作失败", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        if (app_did.isEmpty()) {
            setViewStatus(0);
        } else {
            setViewStatus(1);
        }
    }

    private void getChartData(String title, String dataType, String period) {
        LineChartDialog lineChartDialog = new LineChartDialog(title, dataType, period);
        lineChartDialog.setTargetFragment(ControlPanel.this, 0);
        lineChartDialog.show(getFragmentManager(), "lineChartDialog");
    }

    private void refreshDeviceStatus() {
        mTVDeviceID.setText(app_did);
        Thread aCheckDeviceThread = new Thread() {
            @Override
            public void run() {
                super.run();
                Map<String, String> checkRes = TCheckDevice.checkDeviceStatus(getActivity(), app_uid, app_token, app_did);
                Message msg = new Message();
                if (checkRes != null) {
                    if (Objects.equals(checkRes.get("status"), "1")) {
                        switch (checkRes.get("connection")) {
                            case "online":
                                msg.what = What_CheckOnline;
                                break;
                            case "offline":
                                msg.what = What_CheckOffline;
                                break;
                            case "unknown":
                                msg.what = What_CheckUnknown;
                                break;
                        }
                    } else if (Objects.equals(checkRes.get("status"), "0"))
                        msg.what = What_UserError;
                } else
                    msg.what = What_NetworkErr;
                mHandler.sendMessage(msg);
            }
        };
        aCheckDeviceThread.setDaemon(true);
        aCheckDeviceThread.start();
    }

    private void startTimer() {
        if (UpdateDataTimer == null) {
            UpdateDataTimer = new Timer();
            UpdateDataTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    refreshData();
                }
            }, 800, 30 * 1000);
        }
        if (lastUpdateTimer == null) {
            lastUpdateTimer = new Timer();
            lastUpdateTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    refreshLastUpdate();
                }
            }, 800, 1000);
        }
    }

    private void setData(@NonNull com.seu.smartfarm.modules.mqtt.Message msg) {
//        Toast.makeText(getActivity(), msg.getTopic(), Toast.LENGTH_SHORT).show();
//        Toast.makeText(getActivity(),msg.getMessage().toString(), Toast.LENGTH_LONG).show();
        Map<String, String> res;
        Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        res = new Gson().fromJson(msg.getMessage().toString(), type);
        if (Objects.equals(msg.getTopic(), String.format("topic/%s/data", app_did))) {
            if (Objects.equals(res.get("dataType"), "1")) {
                mDeviceTemp.setStatusValue(res.get("temp"));
                mDeviceHumi.setStatusValue(res.get("humi"));
                mDeviceLux.setStatusValue(res.get("lux"));
                mDeviceWeight.setStatusValue(res.get("weight"));
                mDeviceSoilTemp.setStatusValue(res.get("soilTemp"));
                mDeviceSoilHumi.setStatusValue(res.get("soilHumi"));
                mDeviceSoilEC.setStatusValue(res.get("soilEC"));
                mDeviceSoilPH.setStatusValue(res.get("soilPH"));
                mDeviceWaterHeight.setStatusValue(res.get("waterHeight"));
                mDeviceLight.setBarProgress(Integer.parseInt(Objects.requireNonNull(res.get("light"))), true);
                mDeviceFan.setBarProgress(Integer.parseInt(Objects.requireNonNull(res.get("fan"))), true);
                mDeviceWaterPump.setBarProgress(Integer.parseInt(Objects.requireNonNull(res.get("waterPump"))), true);
                if (Objects.equals(res.get("operateMode"), "1")) {
                    if (!mBtnModeChange.isChecked()) {
                        disableCallback = true;
                        mBtnModeChange.setChecked(true);
                    }
                } else if (Objects.equals(res.get("operateMode"), "0")) {
                    if (mBtnModeChange.isChecked()) {
                        disableCallback = true;
                        mBtnModeChange.setChecked(false);
                    }
                }
                if (Objects.equals(res.get("nightMode"), "1")) {
                    mDeviceNightMode.setSwitchStatus(true, true);
                } else if (Objects.equals(res.get("nightMode"), "0")) {
                    mDeviceNightMode.setSwitchStatus(false, true);
                }
                startHour = Integer.parseInt(Objects.requireNonNull(res.get("startHour")));
                startMin = Integer.parseInt(Objects.requireNonNull(res.get("startMin")));
                endHour = Integer.parseInt(Objects.requireNonNull(res.get("endHour")));
                endMin = Integer.parseInt(Objects.requireNonNull(res.get("endMin")));
                lastUpdateDate = new Date();
                refreshLastUpdate();
            } else if (Objects.equals(res.get("dataType"), "2")) {
                BitmapWorkerTask bitmapWorkerTask = new BitmapWorkerTask(mDeviceView);
                bitmapWorkerTask.execute(res.get("frame"));
                mBtnGetLivePic.setVisibility(View.VISIBLE);
            }
        } else if (msg.getTopic().contains("/connected")) {
            sDeviceOnline = true;
            mTVDeviceOnlineStatus.setText("设备在线");
            mTVDeviceOnlineStatus.setTextColor(getResources().getColor(R.color.colorOnline));
            setDeviceControlClickable(true);
            sendCmd("1", "null", false);
        } else if (msg.getTopic().contains("/disconnected")) {
            sDeviceOnline = false;
            mTVDeviceOnlineStatus.setText("设备离线");
            mTVDeviceOnlineStatus.setTextColor(getResources().getColor(R.color.colorOffline));
            setDeviceControlClickable(false);
        }
    }

    private void refreshData() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            refreshDeviceStatus();
        });
    }

    private void refreshLastUpdate() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            if (lastUpdateDate != null) {
                long diff = (new Date().getTime() - lastUpdateDate.getTime()) / 1000;
                if (diff < 5) {
                    setUpdateTime(getString(R.string.t_just_now));
                } else if (diff < 60) {
                    setUpdateTime(String.format(getString(R.string.t_sec_ago), diff));
                } else if (diff < 3600) {
                    setUpdateTime(String.format(getString(R.string.t_min_ago), diff / 60));
                } else if (diff < 86400) {
                    setUpdateTime(String.format(getString(R.string.t_hours_ago), diff / 3600));
                } else {
                    setUpdateTime(String.format(getString(R.string.t_days_ago), diff / 86400));
                }
            } else {
                setUpdateTime("");
            }
        });
    }

    void setUpdateTime(String time) {
        mDeviceTemp.setUpdateTime(time);
        mDeviceHumi.setUpdateTime(time);
        mDeviceLux.setUpdateTime(time);
        mDeviceSoilPH.setUpdateTime(time);
        mDeviceSoilEC.setUpdateTime(time);
        mDeviceSoilHumi.setUpdateTime(time);
        mDeviceSoilTemp.setUpdateTime(time);
        mDeviceWeight.setUpdateTime(time);
        mDeviceWaterHeight.setUpdateTime(time);
        mDeviceWaterPump.setUpdateTime(time);
        mDeviceFan.setUpdateTime(time);
        mDeviceLight.setUpdateTime(time);
        mDeviceNightMode.setUpdateTime(time);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == SCAN_REQUEST_CODE && resultCode == RESULT_OK) {
            scanResult = intent.getStringExtra(ScanActivity.INTENT_EXTRA_RESULT);
//            Toast.makeText(getActivity(), "扫描结果: " + scanResult, Toast.LENGTH_LONG).show();
            bindDevice(scanResult);
        } else if (requestCode == REQUEST_CODE_SetTime && resultCode == RESULT_OK) {
            startHour = intent.getIntExtra("startHour", startHour);
            startMin = intent.getIntExtra("startMin", startMin);
            endHour = intent.getIntExtra("endHour", endHour);
            endMin = intent.getIntExtra("endMin", endMin);
            setTimeDialog.dismiss();
            sendCmd("7", String.format("2 %02d %02d %02d %02d", startHour, startMin, endHour, endMin), true);
        }
    }

    void bindDevice(String DeviceID) {
        if (DeviceID.startsWith("Farm_")) {
            Thread aBindThread = new Thread() {
                @Override
                public void run() {
                    super.run();
                    Map<String, String> UnbindRes = TBindDevice.bindUserDevice(getActivity(), app_uid, app_token, DeviceID);
                    Message msg = new Message();
                    if (UnbindRes != null) {
                        if (Objects.equals(UnbindRes.get("status"), "1"))
                            msg.what = What_BindSuccess;
                        else if (Objects.equals(UnbindRes.get("status"), "2"))
                            msg.what = What_BindFailed;
                        else if (Objects.equals(UnbindRes.get("status"), "0"))
                            msg.what = What_UserError;
                    } else
                        msg.what = What_NetworkErr;
                    mHandler.sendMessage(msg);
                }
            };
            aBindThread.setDaemon(true);
            aBindThread.start();
        } else {
            Toast.makeText(getActivity(), "二维码无效，请重新扫码！", Toast.LENGTH_SHORT).show();
        }
    }

    void sendCmd(String cmdCode, String data, boolean showToast) {
        if (sDeviceOnline) {
            JSONObject messageData = new JSONObject();
            try {
                messageData.put("uid", app_uid);
                messageData.put("cmd", cmdCode);
                messageData.put("data", data);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            final Publish publish = new Publish(String.format("topic/%s/cmd", app_did), messageData.toString(), 1, false);
            ((NavigationActivity) getActivity()).publish(publish, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
//                    Toast.makeText(getActivity(), "指令发送成功！", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                    Toast.makeText(getActivity(), "指令发送失败！", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            if (showToast)
                Toast.makeText(getActivity(), "设备不在线，操作失败", Toast.LENGTH_SHORT).show();
        }
    }

    void activeDevice() {
        ((NavigationActivity) getActivity()).subscribeDevice();

        JSONObject messageData = new JSONObject();
        try {
            messageData.put("uid", app_uid);
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

    void setViewStatus(int viewStatus) {
        if (viewStatus != mViewStatus) {
            mViewStatus = viewStatus;
            if (mViewStatus == 0) {
                frmBind.setVisibility(View.VISIBLE);
                frmControlPanel.setVisibility(View.GONE);
            } else {
                frmBind.setVisibility(View.GONE);
                frmControlPanel.setVisibility(View.VISIBLE);
            }
        }
    }

    void setControlViewStatus(int viewStatus) {
        if (viewStatus != mControlViewStatus) {
            mControlViewStatus = viewStatus;
            if (mControlViewStatus == 0) {
                frmManual.setVisibility(View.VISIBLE);
                frmAuto.setVisibility(View.GONE);
            } else {
                frmManual.setVisibility(View.GONE);
                frmAuto.setVisibility(View.VISIBLE);
            }
        }
    }

    void setDeviceControlClickable(boolean clickable) {
        mDeviceLight.setSwitchClickable(clickable);
        mDeviceLight.setBarClickable(clickable);
        mDeviceFan.setSwitchClickable(clickable);
        mDeviceFan.setBarClickable(clickable);
        mDeviceWaterPump.setSwitchClickable(clickable);
        mDeviceWaterPump.setBarClickable(clickable);
        mDeviceNightMode.setSwitchClickable(clickable);
        mDeviceNightMode.setBtnClickable(clickable);
        mBtnModeChange.setClickable(clickable);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!app_did.isEmpty()) {
            startTimer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!app_did.isEmpty()) {
            if (UpdateDataTimer != null) {
                UpdateDataTimer.cancel();
                UpdateDataTimer = null;
            }
            if (lastUpdateTimer != null) {
                lastUpdateTimer.cancel();
                lastUpdateTimer = null;
            }
        }
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case What_NetworkErr:
                    sDeviceOnline = false;
                    mTVDeviceOnlineStatus.setText("状态未知");
                    mTVDeviceOnlineStatus.setTextColor(getResources().getColor(R.color.colorWarning));
                    setDeviceControlClickable(false);
                    Toast.makeText(getActivity(), "网络连接异常，请检查网络连接！", Toast.LENGTH_SHORT).show();
                    break;
                case What_UserError:
                    sDeviceOnline = false;
                    mTVDeviceOnlineStatus.setText("状态未知");
                    mTVDeviceOnlineStatus.setTextColor(getResources().getColor(R.color.colorWarning));
                    setDeviceControlClickable(false);
                    Toast.makeText(getActivity(), "用户登录已过期，请重新登录！", Toast.LENGTH_SHORT).show();
                    break;
                case What_BindSuccess:
                    Toast.makeText(getActivity(), "绑定成功！", Toast.LENGTH_SHORT).show();
                    app_did = scanResult;
                    // 把token保存到本地
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences("get_token", MODE_PRIVATE).edit();
                    editor.putString("did", app_did);
                    editor.apply();
                    activeDevice();
                    startTimer();
                    setViewStatus(1);
                    break;
                case What_BindFailed:
                    Toast.makeText(getActivity(), "绑定失败，该设备已被其他账号绑定！", Toast.LENGTH_SHORT).show();
                    break;
                case What_CheckOnline:
                    sDeviceOnline = true;
                    mTVDeviceOnlineStatus.setText("设备在线");
                    mTVDeviceOnlineStatus.setTextColor(getResources().getColor(R.color.colorOnline));
                    setDeviceControlClickable(true);
                    sendCmd("1", "null", false);
                    break;
                case What_CheckOffline:
                    sDeviceOnline = false;
                    mTVDeviceOnlineStatus.setText("设备离线");
                    mTVDeviceOnlineStatus.setTextColor(getResources().getColor(R.color.colorOffline));
                    setDeviceControlClickable(false);
                    break;
                case What_CheckUnknown:
                    sDeviceOnline = false;
                    mTVDeviceOnlineStatus.setText("状态未知");
                    mTVDeviceOnlineStatus.setTextColor(getResources().getColor(R.color.colorWarning));
                    setDeviceControlClickable(false);
                    break;
            }
            return false;
        }
    });
}
