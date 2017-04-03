package com.latuhov.helpers;

import android.app.Activity;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.View;

/**
 * Created by Latuhov on 12/18/16.
 */

public class ViewUtils {

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static int getDisplayWidth(Activity activity) {
        return getDisplayMetrics(activity).widthPixels;
    }

    public static int getDisplayHeight(Activity activity) {
        return getDisplayMetrics(activity).heightPixels;
    }

    private static DisplayMetrics getDisplayMetrics(Activity activity) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        return displaymetrics;
    }

    public static boolean isPointInsideView(float x, float y, View view) {
        int location[] = new int[2];
        view.getLocationOnScreen(location);
        int viewX = location[0];
        int viewY = location[1];
        //point is inside view bounds
        return (x > viewX && x < (viewX + view.getWidth())) &&
                (y > viewY && y < (viewY + view.getHeight()));
    }

}
