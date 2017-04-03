package com.latuhov.helpers.basic;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Latuhov on 11/25/16.
 */

public class BasicPrefHelper {
    protected static SharedPreferences prefs;
    private static final String PATH = "PATH";

    public BasicPrefHelper(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static SharedPreferences getPrefs() {
        return prefs;
    }

    public static SharedPreferences.Editor getEditor() {
        return prefs.edit();
    }

    public static String getPhotoPath() {
        return prefs.getString(PATH, "");
    }

    public static void setPhotoPath(String path) {
        prefs.edit().putString(PATH, path).apply();
    }
}
