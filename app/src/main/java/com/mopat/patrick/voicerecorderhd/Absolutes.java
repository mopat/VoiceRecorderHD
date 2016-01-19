package com.mopat.patrick.voicerecorderhd;

import android.os.Environment;

import java.io.File;

/**
 * Created by Patrick on 10.01.2016.
 */
public class Absolutes {
    public static final File DIRECTORY =  new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/ClearVoiceRecorder");
    public static final String RECORDING_DEF_NAME = "recording_";
}
