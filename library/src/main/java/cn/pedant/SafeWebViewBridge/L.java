package cn.pedant.SafeWebViewBridge;

import android.util.Log;

/**
 * 日志工具类
 *
 * @author Liangzc
 *         created on 2016/4/20 11:29
 */
public class L {

    public static void d(String tag, String message) {
        if (BuildConfig.DEBUG)
            Log.d(tag, message);
    }

    public static void e(String tag, String message) {
        if (BuildConfig.DEBUG)
            Log.e(tag, message);
    }

    public static void w(String tag, String message) {
        if (BuildConfig.DEBUG)
            Log.w(tag, message);
    }
}
