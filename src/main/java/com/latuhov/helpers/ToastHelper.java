package com.latuhov.helpers;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

/**
 * Created by Latuhov on 12/7/16.
 */

public class ToastHelper extends Thread {
    public static Handler mHandler;
    private static final String MESSAGE = "message";
    private Context context;

    public void setContext(Context context) {
        this.context = context;
    }

    public static void sendMessage(String string) {
        Message message = new Message();

        Bundle bd = new Bundle();
        bd.putString(MESSAGE, string);
        message.setData(bd);
        mHandler.sendMessage(message);
    }

    public void run() {
        if (mHandler != null) return;

        Looper.prepare();

        mHandler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(Message msg) {
                if (msg != null && context != null) {
                    Bundle bundle = msg.getData();
                    String message = bundle.getString(MESSAGE);
                    Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        };

        Looper.loop();
    }

}