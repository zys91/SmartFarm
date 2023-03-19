package com.seu.smartfarm.modules.userapi;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.seu.smartfarm.R;
import com.seu.smartfarm.modules.utils.HttpUtil;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class TChangePwd {
    private final static String TAG = "TChangePwd";

    public static Map<String, String> changeUserPwd(Context context, String uid, String pwd, String newpwd) {
        Map<String, String> res = new HashMap<>();
        try {
            String url = context.getResources().getString(R.string.apiUrl);
            url += "/userChangePwd?";
            url += "uid=" + uid;
            url += "&pwd=" + pwd;
            url += "&newpwd=" + newpwd;
            String resp = HttpUtil.httpGet(url);
            Type type = new TypeToken<Map<String, String>>() {
            }.getType();
            res = new Gson().fromJson(resp, type);
        } catch (Exception er) {
            Log.e(TAG, "changeUserPwd: " + er.getMessage());
        }
        return res;
    }
}
