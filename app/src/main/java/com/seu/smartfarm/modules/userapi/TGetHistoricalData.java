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

public class TGetHistoricalData {
    private final static String TAG = "TGetHistoricalData";

    public static Map<String, String> GetHistoricalData(Context context, String uid, String token, String did, String dataType, String period) {
        Map<String, String> res = new HashMap<>();
        try {
            String url = context.getResources().getString(R.string.apiUrl);
            url += "/userGetDeviceData?";
            url += "uid=" + uid;
            url += "&token=" + token;
            url += "&did=" + did;
            url += "&dataType=" + dataType;
            url += "&period=" + period;
            String resp = HttpUtil.httpGet(url);
            Type type = new TypeToken<Map<String, String>>() {
            }.getType();
            res = new Gson().fromJson(resp, type);
        } catch (Exception er) {
            Log.e(TAG, "GetHistoricalData: " + er.getMessage());
        }
        return res;
    }
}
