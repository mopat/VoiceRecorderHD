package com.mopat.patrick.voicerecorderhd;

import android.os.Environment;

import java.io.File;

/**
 * Created by Patrick on 10.01.2016.
 */
public class Absolutes {
    public static final File DIRECTORY = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/HDVoiceRecorder");
    public static final String RECORDING_DEF_NAME = "recording_";
    public static final String ADMOB_ID = "ca-app-pub-1405686491469404~7524297652";
    public static final String AD_UNIT_ID = "ca-app-pub-1405686491469404/8074961990";
    public static final boolean IS_PRO = false;
   // public static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712";
}
