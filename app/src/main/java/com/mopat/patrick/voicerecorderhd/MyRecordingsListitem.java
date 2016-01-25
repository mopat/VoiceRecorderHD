package com.mopat.patrick.voicerecorderhd;

import android.util.Log;

/**
 * Created by Patrick on 12.01.2016.
 */
public class MyRecordingsListitem {
    private String filename, samplerate, filesize, duration, modifiedDate;
    private boolean checked, checkboxVisible;

    public MyRecordingsListitem(String filename, String samplerate, String filesize, String duration, String modifiedDate, boolean checked) {
        this.filename = filename;
        this.samplerate = samplerate;
        this.filesize = filesize;
        this.duration = duration;
        this.modifiedDate = modifiedDate;
        this.checked = checked;
    }

    public String getName() {
        return String.valueOf(filename);
    }

    public boolean isChecked() {
        return checked;
    }

    public String getSamplerate() {
        return samplerate;
    }

    public String getFilesize() {
        return filesize;
    }

    public String getDuration() {
        return duration;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public void uncheck() {
        checked = false;
    }

    public void toggleChecked() {
        checked = !checked;
    }

    public String getModifiedDate() {
        return modifiedDate;
    }

    public boolean isCheckboxVisible() {
        return checkboxVisible;
    }

    public void setCheckboxVisible(boolean checkboxVisible) {
        this.checkboxVisible = checkboxVisible;
    }
}
