package com.mopat.patrick.voicerecorderhd;

/**
 * Created by Patrick on 12.01.2016.
 */
public class SamplerateListitem {
    private String samplerate;
    private boolean checked;

    public SamplerateListitem(String samplerate, boolean checked){
        this.samplerate = samplerate;
        this.checked = checked;
    }

    public int getValue() {
        return Integer.valueOf(samplerate);
    }

    public String getName() {
        return String.valueOf(samplerate);
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
