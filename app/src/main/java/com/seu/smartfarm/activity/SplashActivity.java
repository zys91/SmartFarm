package com.seu.smartfarm.activity;

import static com.seu.smartfarm.activity.LoginActivity.What_NetworkErr;
import static com.seu.smartfarm.app.MainApplication.app_did;
import static com.seu.smartfarm.app.MainApplication.app_tel;
import static com.seu.smartfarm.app.MainApplication.app_token;
import static com.seu.smartfarm.app.MainApplication.app_uid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.seu.smartfarm.R;
import com.seu.smartfarm.modules.userapi.TCheckToken;
import com.seu.smartfarm.modules.userapi.TLoginVideo;

import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends AppCompatActivity {
    public final static String TAG = "SplashActivity";
    public final static int What_TokenInvalid = 1045;
    public final static int What_TokenValid = 1050;
    private final int[] playIndex = {0, 3300, 5400, 7300, 10600, 13900, 18800, 20500, 23000, 24600, 26300, 27900, 29400, 31100, 36100, 39800, 42600, 45700, 49900, 53600, 56200, 58800, 63600, 67900, 73000, 77000, 82600};
    private TLoginVideo video_view;
    private LinearLayout splash_ll;
    private int duration;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == What_TokenValid) {
                Toast.makeText(getApplicationContext(), "登录成功！", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), NavigationActivity.class);
                String userInfoJson = "";
                intent.putExtra("userInfo", userInfoJson);
                startActivity(intent);
                finish();
            } else if (msg.what == What_TokenInvalid) {
                Toast.makeText(getApplicationContext(), "用户登录已过期，请重新登录！", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            } else if (msg.what == What_NetworkErr) {
                Toast.makeText(getApplicationContext(), "网络连接异常，请重试！", Toast.LENGTH_SHORT).show();
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        splash_ll = findViewById(R.id.splash_ll);
        //获得ActionBar实例
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            //调用hide()方法将标题栏隐藏起来
            supportActionBar.hide();
        }
        // 设置渐变效果
        initVideoView();
        setAnimation();
    }

    private void initVideoView() {
        //找VideoView控件
        video_view = findViewById(R.id.login_video);
        //加载视频文件
        video_view.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video1));
        //播放
        Random random = new Random();
        int randomIndex = random.nextInt(playIndex.length);
        if (randomIndex != playIndex.length - 1)
            duration = playIndex[randomIndex + 1] - playIndex[randomIndex];
        else
            duration = 4500;
        video_view.seekTo(playIndex[randomIndex]);
        video_view.start();
    }

    private void setAnimation() {
        AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(duration);
        splash_ll.setAnimation(animation);
        // 设置动画监听
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                video_view.stopPlayback();
                Timer timer = new Timer();
                TimerTask task = new TimerTask() {
                    public void run() {
                        intentActivity();
                    }
                };
                timer.schedule(task, 500);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    /**
     * 根据首次启动是否跳转到对应的界面
     */
    private void intentActivity() {
        SharedPreferences sp = getSharedPreferences("get_token", MODE_PRIVATE);
        app_uid = sp.getString("uid", "");
        app_token = sp.getString("token", "");
        app_tel = sp.getString("tel", "");
        app_did = sp.getString("did", "");

        if (app_token.isEmpty()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            try {
                Thread aCheckTokenThread = new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        Map<String, String> checkRes = TCheckToken.checkUserToken(SplashActivity.this, app_uid, app_token);
                        Message msg = new Message();
                        if (checkRes != null) {
                            if (Objects.equals(checkRes.get("status"), "0")) {
                                msg.what = What_TokenInvalid;
                            } else if (Objects.equals(checkRes.get("status"), "1")) {
                                msg.what = What_TokenValid;
                            } else
                                msg.what = What_NetworkErr;
                        } else
                            msg.what = What_NetworkErr;
                        mHandler.sendMessage(msg);
                    }
                };
                aCheckTokenThread.setDaemon(true);
                aCheckTokenThread.start();
            } catch (Exception er) {
                Log.e(TAG, "CheckToken: " + er.getMessage());
            }
        }
    }

    //防止锁屏或者切出的时候，音乐在播放
    @Override
    protected void onStop() {
        super.onStop();
        video_view.stopPlayback();
    }

}