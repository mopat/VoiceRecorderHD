package com.mopat.patrick.voicerecorderhd;

import java.util.concurrent.TimeUnit;

/**
 * Created by Patrick on 29.01.2016.
 */
public class TimeFormat {
    public static String formatTime(double timeInMs) {

        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes((int) timeInMs),
                TimeUnit.MILLISECONDS.toSeconds((int) timeInMs) - TimeUnit.MILLISECONDS.toMinutes((int) timeInMs) * 60,
                (TimeUnit.MILLISECONDS.toMillis((int) timeInMs) - TimeUnit.MILLISECONDS.toSeconds((int) timeInMs) * 1000) / 10
        );
    }
}
