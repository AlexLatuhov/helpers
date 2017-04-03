package com.latuhov.helpers;

import android.util.Log;

import com.latuhov.helpers.basic.BasicApp;

/**
 * Created by Latuhov on 11/25/16.
 */

public class AppLog {
    public static void d(String tag, String message) {
        if (!BasicApp.isDebug()) return;
        log(tag, message);
    }

    public static void d(String message) {
        if (!BasicApp.isDebug()) return;
        log("TAG", message);
    }

    public static void e(String tag, String message) {
        if (!BasicApp.isDebug()) return;
        Log.e(tag, message);
    }

    public static void log(String tag, String message) {
        Log.d(tag, message);
    }
}
