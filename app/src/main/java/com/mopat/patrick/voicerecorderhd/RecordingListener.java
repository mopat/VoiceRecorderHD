package com.mopat.patrick.voicerecorderhd;

/**
 * Created by Patrick on 19.01.2016.
 */
interface RecordingListener {
    void recordedBytes(int recordedBytes, byte[] bytes);

    void maximumRecordingSizeReached();
}
