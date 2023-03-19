package com.seu.smartfarm.modules.userapi;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.seu.smartfarm.R;
import com.seu.smartfarm.modules.bean.VideoBean;
import com.seu.smartfarm.modules.bean.VideoListBean;
import com.seu.smartfarm.modules.utils.HttpUtil;

import java.util.ArrayList;
import java.util.List;

public class TGetDataList {
    private final static String TAG = "TGetDataList";

    public static List<VideoBean> getVideoList(Context context) {
        List<VideoBean> videoList = new ArrayList<>();
        try {
            String url = context.getResources().getString(R.string.DataUrl);
            String resp = HttpUtil.httpGet(url);
            if (!resp.isEmpty()) {
                VideoListBean res = new Gson().fromJson(resp, VideoListBean.class);
                videoList = res.getLinks();
            }
        } catch (Exception er) {
            Log.e(TAG, "checkUserToken: " + er.getMessage());
        }
        return videoList;
    }
}
