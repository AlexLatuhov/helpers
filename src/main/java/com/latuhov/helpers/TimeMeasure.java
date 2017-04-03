package com.latuhov.helpers;

import java.util.HashMap;

/**
 * Created by Latuhov on 1/26/17.
 */

public class TimeMeasure {
    public static final String TIME = "TIME";
    public static HashMap<String, Long> startTime = new HashMap<>();

    public static void start(String name) {
        startTime.put(name, System.currentTimeMillis());
        AppLog.d(TIME, "started " + name);
    }

    public static void end(String name) {
        if (startTime.get(name) == null) return;
        AppLog.d(TIME, "finished " + name + " " + (System.currentTimeMillis() - startTime.get(name)));
    }
}
