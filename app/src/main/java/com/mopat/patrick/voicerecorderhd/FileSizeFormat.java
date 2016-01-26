package com.mopat.patrick.voicerecorderhd;

/**
 * Created by Patrick on 22.01.2016.
 */
public class FileSizeFormat {

    public static String getFormattedFileSize(int fileSize) {
        int length = (int) (Math.log10(fileSize) + 1);
        if (length > 6)
            return String.valueOf(twoDecimals((double) fileSize / (1000 * 1000)) + "MB") + "\n " + "Samplerate: " + String.valueOf(Config.sampleRate) + "Hz";
        else if (length > 3 && length < 7)
            return String.valueOf(fileSize / 1000 + "KB") + "\n " + "Samplerate: " + String.valueOf(Config.sampleRate) + "Hz";

        return "00:00:00";
    }

    private static double twoDecimals(double num) {
        return Math.round(num * 10.0) / 10.0;
    }

    public static String getFormattedFileSizeForList(int fileSize) {
        int length = (int) (Math.log10(fileSize) + 1);
        if (length > 6)
            return String.valueOf(twoDecimals((double) fileSize / (1000 * 1000)) + "MB");
        else if (length > 3 && length < 7)
            return String.valueOf(fileSize / 1000 + "KB");

        return "00:00:00";
    }

}
