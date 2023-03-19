package com.seu.smartfarm.activity;

import static com.seu.smartfarm.modules.utils.SMSUtil.getOperator;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.mob.MobSDK;
import com.seu.smartfarm.R;
import com.seu.smartfarm.modules.userapi.TLogin;
import com.seu.smartfarm.modules.userapi.TLoginVideo;
import com.seu.smartfarm.modules.userapi.TRegister;
import com.seu.smartfarm.modules.utils.CountDownTimerUtil;

import java.util.Map;
import java.util.Objects;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class LoginActivity extends AppCompatActivity {
    public final static String TAG = "LoginActivity";
    public final static int What_RegisterSuccess = 1000;
    public final static int What_RegisterFailed = 1010;
    public final static int What_RegisterClash = 1015;
    public final static int What_LoginSuccess = 1020;
    public final static int What_LoginUidErr = 1030;
    public final static int What_LoginPwdErr = 1035;
    public final static int What_NetworkErr = 1040;
    public final static int What_CodeSendSuccess = 2000;
    public final static int What_CodeSendFailed = 2010;
    public final static int What_CodeVerifySuccess = 2020;
    public final static int What_CodeVerifyFailed = 2030;
    private boolean granted = false;
    private int mViewStatus = 0;  //0-login; 1-register;
    private EditText mEditLoginUid;
    private EditText mEditLoginPwd;
    private EditText mEditRegisterUid;
    private EditText mEditRegisterPwd0;
    private EditText mEditRegisterPwd1;
    private EditText mEditRegisterTel;
    private EditText mEditRegisterCheckCode;
    private Button mBtnRegister;
    private Button mBtnLogin;
    private Button mBtnRegister2Login;
    private TextView mTextLogin2Register;
    private TextView mTextGetCode;
    private LinearLayout frmLogin;
    private LinearLayout frmRegister;
    private TLoginVideo video_view;
    private CheckBox mCBGranted;
    private boolean ready; //判断短信验证码服务注册事件是否成功
    private long mExitTime; //声明一个long类型变量：用于存放上一点击“返回键”的时刻


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //获得ActionBar实例
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            //调用hide()方法将标题栏隐藏起来
            supportActionBar.hide();
        }
        initActivity();
    }

    //返回重启加载
    @Override
    protected void onRestart() {
        super.onRestart();
        initVideoView();
    }

    //防止锁屏或者切出的时候，音乐在播放
    @Override
    protected void onStop() {
        super.onStop();
        video_view.stopPlayback();
    }

    protected void onDestroy() {
        super.onDestroy();
        if (ready) {
            // 销毁回调监听接口
            SMSSDK.unregisterAllEventHandler();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //判断用户是否点击了“返回键”
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //与上次点击返回键时刻作差
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                //大于2000ms则认为是误操作，使用Toast进行提示
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                //并记录下本次点击“返回键”的时刻，以便下次进行判断
                mExitTime = System.currentTimeMillis();
            } else {
                //小于2000ms则认为是用户确实希望退出程序-调用System.exit()方法进行退出
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void registerSDK() {
        MobSDK.init(this);
        MobSDK.submitPolicyGrantResult(true, null);
        EventHandler eventHandler = new EventHandler() {
            public void afterEvent(int event, int result, Object data) {
                Message msg = new Message();
                if (result == SMSSDK.RESULT_COMPLETE) {
                    //回调完成
                    if (event == 2)
                        msg.what = What_CodeSendSuccess;
                    else if (event == 3) {
                        msg.what = What_CodeVerifySuccess;
                    }
                } else {
                    if (event == 2)
                        msg.what = What_CodeSendFailed;
                    else if (event == 3)
                        msg.what = What_CodeVerifyFailed;
                    ((Throwable) data).printStackTrace();
                }
                mHandler.sendMessage(msg);
            }
        };
        // 注册回调监听接口
        SMSSDK.registerEventHandler(eventHandler);
        ready = true;
    }

    void initActivity() {
        try {
            mEditLoginUid = this.findViewById(R.id.editLoginUid);
            mEditLoginPwd = this.findViewById(R.id.editLoginPwd);
            mEditRegisterUid = this.findViewById(R.id.editRegisterUid);
            mEditRegisterPwd0 = this.findViewById(R.id.editRegisterPwd0);
            mEditRegisterPwd1 = this.findViewById(R.id.editRegisterPwd1);
            mEditRegisterTel = this.findViewById(R.id.editRegisterTel);
            mEditRegisterCheckCode = this.findViewById(R.id.editRegisterCheck);
            mBtnRegister = this.findViewById(R.id.btnRegister);
            mBtnLogin = this.findViewById(R.id.btnLogin);
            mBtnRegister2Login = this.findViewById(R.id.btnRegister2Login);
            mTextLogin2Register = this.findViewById(R.id.tvLogin2Register);
            mTextGetCode = this.findViewById(R.id.tvGetCode);
            frmLogin = this.findViewById(R.id.frameLogin);
            frmRegister = this.findViewById(R.id.frameRegister);
            mCBGranted = this.findViewById(R.id.cb_granted);
            initVideoView();
            mBtnRegister.setOnClickListener(this::onBtnClick);
            mBtnLogin.setOnClickListener(this::onBtnClick);
            mTextLogin2Register.setOnClickListener(this::onBtnClick);
            mBtnRegister2Login.setOnClickListener(this::onBtnClick);
            mTextGetCode.setOnClickListener(this::onBtnClick);
            mCBGranted.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    if (isChecked) {
                        granted = true;
                    } else {
                        granted = false;
                    }

                }
            });
            registerSDK();
        } catch (Exception er) {
            Log.e(TAG, "init: " + er.getMessage());
        }
    }

    void initVideoView() {
        //找VideoView控件
        video_view = findViewById(R.id.login_video);
        //加载视频文件
        video_view.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video2));
        //播放
        video_view.start();
        //循环播放
        video_view.setOnCompletionListener(mediaPlayer -> video_view.start());
    }

    void onBtnClick(View v) {
        try {
            int vId = v.getId();
            if (vId == R.id.btnRegister) {
                //Enable CheckCodeVerify:
                final String uid = this.mEditRegisterUid.getText().toString();
                final String pwd0 = this.mEditRegisterPwd0.getText().toString();
                final String pwd1 = this.mEditRegisterPwd1.getText().toString();
                final String tel = this.mEditRegisterTel.getText().toString();
                final String checkCode = this.mEditRegisterCheckCode.getText().toString();
                if (uid.isEmpty())
                    Toast.makeText(this, "账号不能为空!", Toast.LENGTH_SHORT).show();
                else if (uid.length() < 5)
                    Toast.makeText(this, "账号长度不得少于5位!", Toast.LENGTH_SHORT).show();
                else if (uid.length() > 18)
                    Toast.makeText(this, "账号长度太长!", Toast.LENGTH_SHORT).show();
                else if (pwd0.isEmpty())
                    Toast.makeText(this, "密码不能为空!", Toast.LENGTH_SHORT).show();
                else if (!pwd1.equals(pwd0))
                    Toast.makeText(this, "两次输入密码不一致!", Toast.LENGTH_SHORT).show();
                else if (tel.isEmpty())
                    Toast.makeText(this, "手机号码不能为空!", Toast.LENGTH_SHORT).show();
                else if (checkCode.isEmpty())
                    Toast.makeText(this, "验证码不能为空!", Toast.LENGTH_SHORT).show();
                else if (!granted)
                    Toast.makeText(this, "请授权免责说明!", Toast.LENGTH_SHORT).show();
                else
                    SMSSDK.submitVerificationCode("86", tel, checkCode);
                //Enable CheckCodeVerify：SMSSDK.submitVerificationCode("86", tel, checkCode);
                //Disable CheckCodeVerify: acRegister();
            } else if (vId == R.id.btnLogin) {
                acLogin();
            } else if (vId == R.id.btnRegister2Login) {
                setViewStatus(0);
            } else if (vId == R.id.tvLogin2Register) {
                setViewStatus(1);
            } else if (vId == R.id.tvGetCode) {
                final String tel = this.mEditRegisterTel.getText().toString();
                if (tel.isEmpty())
                    Toast.makeText(this, "手机号码不能为空!", Toast.LENGTH_SHORT).show();
                else {
                    if (getOperator(tel)) {//匹配手机号是否存在
                        Thread aGetCodeThread = new Thread() {
                            @Override
                            public void run() {
                                super.run();
                                SMSSDK.getVerificationCode("86", tel);
                            }
                        };
                        aGetCodeThread.setDaemon(true);
                        aGetCodeThread.start();
                        CountDownTimerUtil mCountDownTimerUtils = new CountDownTimerUtil(mTextGetCode, 60000, 1000);
                        mCountDownTimerUtils.start();
                    } else {
                        Toast.makeText(this, "手机号格式错误!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (Exception er) {
            Log.e(TAG, "onBtnClick: " + er.getMessage());
        }
    }

    void acLogin() {
        try {
            final String uid = this.mEditLoginUid.getText().toString();
            final String pwd = this.mEditLoginPwd.getText().toString();

            if (uid.isEmpty())
                Toast.makeText(this, "账号不能为空!", Toast.LENGTH_SHORT).show();
            else if (pwd.isEmpty())
                Toast.makeText(this, "密码不能为空!", Toast.LENGTH_SHORT).show();
            else {
                Thread aLoginThread = new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        Map<String, String> loginRes = TLogin.loginUser(LoginActivity.this, uid, pwd);
                        Message msg = new Message();
                        if (loginRes != null)
                            if (Objects.equals(loginRes.get("status"), "0")) {
                                msg.what = What_LoginUidErr;
                            } else if (Objects.equals(loginRes.get("status"), "1")) {
                                msg.what = What_LoginSuccess;
                                msg.obj = loginRes;
                            } else if (Objects.equals(loginRes.get("status"), "2")) {
                                msg.what = What_LoginPwdErr;
                            } else
                                msg.what = What_NetworkErr;
                        mHandler.sendMessage(msg);
                    }
                };
                aLoginThread.setDaemon(true);
                aLoginThread.start();
            }
        } catch (Exception er) {
            Log.e(TAG, "acLogin: " + er.getMessage());
        }
    }

    void acRegister() {
        try {
            final String uid = this.mEditRegisterUid.getText().toString();
            final String pwd = this.mEditRegisterPwd0.getText().toString();
            final String tel = this.mEditRegisterTel.getText().toString();
            Thread aRegisterThread = new Thread() {
                @Override
                public void run() {
                    super.run();
                    Map<String, String> registerRes = TRegister.registerUser(LoginActivity.this, uid, pwd, tel);
                    Message msg = new Message();
                    if (registerRes != null) {
                        if (Objects.equals(registerRes.get("status"), "1")) {
                            msg.what = What_RegisterSuccess;
                        } else if (Objects.equals(registerRes.get("status"), "2")) {
                            msg.what = What_RegisterClash;
                        } else
                            msg.what = What_RegisterFailed;
                    } else
                        msg.what = What_RegisterFailed;
                    mHandler.sendMessage(msg);
                }
            };
            aRegisterThread.setDaemon(true);
            aRegisterThread.start();
        } catch (Exception er) {
            Log.e(TAG, "acRegister: " + er.getMessage());
        }
    }

    void setViewStatus(int viewStatus) {
        if (viewStatus != mViewStatus) {
            mViewStatus = viewStatus;
            if (mViewStatus == 0) {
                frmLogin.setVisibility(View.VISIBLE);
                frmRegister.setVisibility(View.GONE);
            } else {
                frmLogin.setVisibility(View.GONE);
                frmRegister.setVisibility(View.VISIBLE);
            }
        }
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case What_RegisterSuccess:
                    Toast.makeText(getApplicationContext(), "注册成功!", Toast.LENGTH_SHORT).show();
                    setViewStatus(0);
                    break;
                case What_RegisterClash:
                    Toast.makeText(getApplicationContext(), "账号已被注册，请更换后重试!", Toast.LENGTH_SHORT).show();
                    break;
                case What_RegisterFailed:
                    Toast.makeText(getApplicationContext(), "注册失败,请检查网络连接!", Toast.LENGTH_SHORT).show();
                    break;
                case What_LoginSuccess:
                    Toast.makeText(getApplicationContext(), "登录成功!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), NavigationActivity.class);
                    String userInfoJson = new Gson().toJson(msg.obj);
                    intent.putExtra("userInfo", userInfoJson);
                    startActivity(intent);
                    finish();
                    break;
                case What_LoginPwdErr:
                    Toast.makeText(getApplicationContext(), "密码错误！", Toast.LENGTH_SHORT).show();
                    break;
                case What_LoginUidErr:
                    Toast.makeText(getApplicationContext(), "账号不存在, 请先注册！", Toast.LENGTH_SHORT).show();
                    break;
                case What_NetworkErr:
                    Toast.makeText(getApplicationContext(), "网络连接异常，请检查网络连接！", Toast.LENGTH_SHORT).show();
                    break;
                case What_CodeSendSuccess:
                    Toast.makeText(getApplicationContext(), "验证码已发送!", Toast.LENGTH_SHORT).show();
                    break;
                case What_CodeSendFailed:
                    Toast.makeText(getApplicationContext(), "验证码获取失败，请检查网络连接!", Toast.LENGTH_SHORT).show();
                    break;
                case What_CodeVerifySuccess:
                    //Toast.makeText(loginActivity, "验证码正确!", Toast.LENGTH_SHORT).show();
                    acRegister();
                    break;
                case What_CodeVerifyFailed:
                    Toast.makeText(getApplicationContext(), "验证码错误!", Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    });
}

