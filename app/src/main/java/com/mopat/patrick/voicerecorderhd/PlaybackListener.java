package com.mopat.patrick.voicerecorderhd;

/**
 * Created by Patrick on 10.01.2016.
 */
interface PlaybackListener {
    void playback(int bytesread, byte[] playedBytes);
}

interface CompletionListener {
    void playbackComplete();
}

interface PauseListener {
    void playbackPaused();
}

interface PlayListener {
    void playbackPlay();
}

interface StopListener {
    void playbackStop();
}