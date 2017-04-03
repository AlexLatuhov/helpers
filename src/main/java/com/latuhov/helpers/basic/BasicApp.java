package com.latuhov.helpers.basic;

import android.support.multidex.MultiDexApplication;

import com.basic.helpers.R;
import com.latuhov.helpers.ToastHelper;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by Latuhov on 12/18/16.
 */

public class BasicApp extends MultiDexApplication {
    private ToastHelper toastHelper;
    private static boolean isDebug = false;

    public static boolean isDebug() {
        return isDebug;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        toastHelper = new ToastHelper();
        toastHelper.setContext(this);
        toastHelper.start();
    }

    protected void setupCalligraphy(String defaultFont) {
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath(defaultFont)
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }

    protected void setDebug(boolean debug) {
        isDebug = debug;
    }

    protected void setupApp(boolean debug, String defaultFont, BasicPrefHelper instance) {
        setDebug(debug);
        setupCalligraphy(defaultFont);
    }
}
