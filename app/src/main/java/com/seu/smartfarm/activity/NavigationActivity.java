package com.seu.smartfarm.activity;

import static com.seu.smartfarm.app.MainApplication.app_did;
import static com.seu.smartfarm.app.MainApplication.app_tel;
import static com.seu.smartfarm.app.MainApplication.app_token;
import static com.seu.smartfarm.app.MainApplication.app_uid;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.seu.smartfarm.R;
import com.seu.smartfarm.modules.mqtt.Connection;
import com.seu.smartfarm.modules.mqtt.Message;
import com.seu.smartfarm.modules.mqtt.Publish;
import com.seu.smartfarm.modules.mqtt.Subscription;
import com.seu.smartfarm.modules.userapi.TAutoUpdater;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class NavigationActivity extends AppCompatActivity {

    public final static String TAG = "NavigationActivity";
    private static final int PERMISSION_REQUESTCODE = 1024;
    private long mExitTime; // 声明一个long类型变量：用于存放上一点击“返回键”的时刻
    private MqttAndroidClient mClient;
    private MyOnReceive MqttMessageReceived;// 声明回调接口
    private BackPressedListener BackPressed = () -> false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        setContentView(R.layout.activity_navigation);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_videolist, R.id.navigation_panel, R.id.navigation_account)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }

    private void init() {
        try {
            Map<String, String> res;
            Type type = new TypeToken<Map<String, String>>() {
            }.getType();
            Intent intent = getIntent();
            String userInfoJson = intent.getStringExtra("userInfo");
            if (!userInfoJson.isEmpty()) {
                res = new Gson().fromJson(userInfoJson, type);
                if (res.get("uid") != null)
                    app_uid = res.get("uid");
                if (res.get("token") != null)
                    app_token = res.get("token");
                if (res.get("tel") != null)
                    app_tel = res.get("tel");
                if (res.get("did") != null)
                    app_did = res.get("did");
                else
                    app_did = "";
                // 把token保存到本地
                SharedPreferences.Editor editor = getSharedPreferences("get_token", MODE_PRIVATE).edit();
                editor.putString("uid", app_uid);
                editor.putString("token", app_token);
                editor.putString("tel", app_tel);
                editor.putString("did", app_did);
                editor.apply();
            }

            List<String> permissionLists = new ArrayList<>();
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                permissionLists.add(Manifest.permission.CAMERA);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionLists.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (permissionLists.isEmpty()) {
                //说明权限都已经通过，可以做你想做的事情去
                //自动更新
                TAutoUpdater manager = new TAutoUpdater(NavigationActivity.this);
                manager.CheckUpdate();
            } else {
                //存在未允许的权限
                ActivityCompat.requestPermissions(this, permissionLists.toArray(new String[permissionLists.size()]), PERMISSION_REQUESTCODE);
            }

        } catch (Exception er) {
            Log.e(TAG, "init" + er.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUESTCODE:
                if (grantResults.length > 0) {
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "请允许所有权限！", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            Fragment fm = getSupportFragmentManager().findFragmentByTag("ControlPanel");
            fm.onActivityResult(requestCode, resultCode, data);
        } catch (Exception er) {
            Log.e(TAG, "onActivityResult: " + er.getMessage());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sp = getSharedPreferences("get_token", MODE_PRIVATE);
        app_uid = sp.getString("uid", "");
        app_token = sp.getString("token", "");
        app_tel = sp.getString("tel", "");
        app_did = sp.getString("did", "");
        if (mClient == null)
            mqttConnectionBuild();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnect();
    }

    @Override
    public void onBackPressed() {
        if (!BackPressed.handleBackPressed()) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                //大于2000ms则认为是误操作，使用Toast进行提示
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                //并记录下本次点击“返回键”的时刻，以便下次进行判断
                mExitTime = System.currentTimeMillis();
            } else {
                //小于2000ms则认为是用户确实希望退出程序-调用System.exit()方法进行退出
                disconnect();
                System.exit(0);
            }
        }
    }

    // 定义set方法将接口从fragment中传递过来
    public void setMqttMessageOnReceive(MyOnReceive myOnReceive) {
        this.MqttMessageReceived = myOnReceive;
    }

    public void setBackPressed(BackPressedListener backPressedListener) {
        this.BackPressed = backPressedListener;
    }

    public void mqttConnectionBuild() {
        Connection connection = new Connection(this, getString(R.string.mqtt_server_host), 8083, app_uid, app_uid, "", 3);
        connect(connection, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Log.d(TAG, "Connected to: " + asyncActionToken.getClient().getServerURI());
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                exception.printStackTrace();
                Toast.makeText(getApplicationContext(), "服务连接失败！", Toast.LENGTH_SHORT).show();
            }

        });
    }

    public void subscribeDevice() {
        if (!app_did.isEmpty()) {
            final Subscription subscription_status = new Subscription(String.format(getString(R.string.emqxSubscribeToSys), app_did), 1);
            subscribe(subscription_status, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    exception.printStackTrace();
                }
            });
            final Subscription subscription_data = new Subscription(String.format(getString(R.string.emqxSubscribeToData), app_did), 0);
            subscribe(subscription_data, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    exception.printStackTrace();
                }
            });
        }
    }

    public void unsubscribeDevice() {
        if (!app_did.isEmpty()) {
            final Subscription subscription_status = new Subscription(String.format(getString(R.string.emqxSubscribeToSys), app_did), 1);
            unsubscribe(subscription_status, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    exception.printStackTrace();
                }
            });
            final Subscription subscription_data = new Subscription(String.format(getString(R.string.emqxSubscribeToData), app_did), 0);
            unsubscribe(subscription_data, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    exception.printStackTrace();
                }
            });
        }
    }

    public void connect(Connection connection, IMqttActionListener listener) {
        mClient = connection.getMqttAndroidClient(this);
        try {
            mClient.connect(connection.getMqttConnectOptions(), null, listener);
            mClient.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    subscribeDevice();
                }

                @Override
                public void connectionLost(Throwable cause) {
                    Toast.makeText(getApplicationContext(), "网络异常，连接丢失！", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    Log.d(TAG, "messageArrived: " + topic);
                    MqttMessageReceived.myListener(new Message(topic, message));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
            Toast.makeText(this, "服务连接失败！", Toast.LENGTH_SHORT).show();
        }

    }

    public void disconnect() {
        if (notConnected(false)) {
            return;
        }
        try {
            mClient.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void subscribe(Subscription subscription, IMqttActionListener listener) {
        if (notConnected(false)) {
            return;
        }
        try {
            mClient.subscribe(subscription.getTopic(), subscription.getQos(), null, listener);
        } catch (MqttException e) {
            e.printStackTrace();
            Toast.makeText(this, "订阅失败！", Toast.LENGTH_SHORT).show();
        }
    }

    public void unsubscribe(Subscription subscription, IMqttActionListener listener) {
        if (notConnected(false)) {
            return;
        }
        try {
            mClient.unsubscribe(subscription.getTopic(), null, listener);
        } catch (MqttException e) {
            e.printStackTrace();
            Toast.makeText(this, "取消订阅失败！", Toast.LENGTH_SHORT).show();
        }
    }

    public void publish(Publish publish, IMqttActionListener callback) {
        if (notConnected(true)) {
            return;
        }
        try {
            mClient.publish(publish.getTopic(), publish.getPayload().getBytes(), publish.getQos(), publish.isRetained(), null, callback);
        } catch (MqttException e) {
            e.printStackTrace();
            Toast.makeText(this, "发布失败！", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean notConnected(boolean showNotify) {
        if (mClient == null || !mClient.isConnected()) {
            if (showNotify) {
                Toast.makeText(this, "网络繁忙，请稍后再试！", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return false;
    }

    //定义回调接口
    public interface MyOnReceive {
        void myListener(Message msg);
    }

    //监听activity的onBackPress事件
    public interface BackPressedListener {
        /**
         * @return true代表响应back键点击，false代表不响应
         */
        Boolean handleBackPressed();
    }
}
