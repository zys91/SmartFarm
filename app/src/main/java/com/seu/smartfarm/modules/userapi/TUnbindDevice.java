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

public class TUnbindDevice {
    private final static String TAG = "TUnbindDevice";

    public static Map<String, String> unbindUserDevice(Context context, String uid, String token, String did) {
        Map<String, String> res = new HashMap<>();
        try {
            String url = context.getResources().getString(R.string.apiUrl);
            url += "/userUnbindDevice?";
            url += "uid=" + uid;
            url += "&token=" + token;
            url += "&did=" + did;
            String resp = HttpUtil.httpGet(url);
            Type type = new TypeToken<Map<String, String>>() {
            }.getType();
            res = new Gson().fromJson(resp, type);
        } catch (Exception er) {
            Log.e(TAG, "unbindUserDevice: " + er.getMessage());
        }
        return res;
    }
}
