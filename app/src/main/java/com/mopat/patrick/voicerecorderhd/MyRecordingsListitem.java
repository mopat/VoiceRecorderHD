package com.mopat.patrick.voicerecorderhd;

/**
 * Created by Patrick on 12.01.2016.
 */
public class MyRecordingsListitem {
    private String filename, samplerate, filesize, duration;
    private boolean checked;

    public MyRecordingsListitem(String filename, String samplerate, String filesize, String duration, boolean checked) {
        this.filename = filename;
        this.samplerate = samplerate;
        this.filesize = filesize;
        this.duration = duration;
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

    public void toggleChecked() {
        checked = !checked;
    }
}
