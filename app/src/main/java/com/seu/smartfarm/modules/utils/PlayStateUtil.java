package com.seu.smartfarm.modules.utils;

import android.view.View;
import android.view.ViewParent;
import android.widget.FrameLayout;

public final class PlayStateUtil {

    private PlayStateUtil() {
    }

    /**
     * 将View从父控件中移除
     */
    public static void removeViewFormParent(View v) {
        if (v == null) return;
        ViewParent parent = v.getParent();
        if (parent instanceof FrameLayout) {
            ((FrameLayout) parent).removeView(v);
        }
    }

}
