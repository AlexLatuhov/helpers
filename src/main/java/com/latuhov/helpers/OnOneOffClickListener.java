package com.latuhov.helpers;

import android.os.SystemClock;
import android.view.View;

/**
 * Created by Latuhov on 12/12/16.
 */

public abstract class OnOneOffClickListener implements View.OnClickListener {

    private long mLastClickTime = 0;
    private final int TIME_TO_WAIT;

    @Override
    public void onClick(View v) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        onOneClick(v);
    }

    public abstract void onOneClick(View v);

    public OnOneOffClickListener(int timeToWait) {
        TIME_TO_WAIT = timeToWait;
    }

    public OnOneOffClickListener() {
        TIME_TO_WAIT = 1000;
    }
}