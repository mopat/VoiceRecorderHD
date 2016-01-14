package com.mopat.patrick.voicerecorderhd;

/**
 * Created by Patrick on 12.01.2016.
 */
public class MyRecordingsListitem {
    private String filename, samplerate;
    private boolean checked;

    public MyRecordingsListitem(String filename, String samplerate, boolean checked){
        this.filename = filename;
        this.samplerate = samplerate;
        this.checked = checked;
    }

    public int getValue() {
        return Integer.valueOf(samplerate);
    }

    public String getName() {
        return String.valueOf(filename);
    }

    public boolean isChecked(){
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
    public void toggleChecked() {
        checked = !checked ;
    }
}
