package com.mopat.patrick.voicerecorderhd;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Patrick on 10.01.2016.
 */
public class Recording {
    private String filepPath;
    private byte[] byteData;
    private int fileLength, lastPlayed;
    private File file;
    private AudioManager am;
    private boolean isPaused;
    private int state;
    private List<PlaybackListener> playbackListener = new ArrayList<>();
    private List<CompletionListener> completionListener = new ArrayList<>();

    public Recording(String filePath, Context context) {
        am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.filepPath = filePath;
        this.file = new File(filePath);
        this.fileLength = (int) file.length();
        this.byteData = new byte[fileLength];
        this.state = 0;
    }

    public void stop() {
        state = 0;
        playback(0);
        lastPlayed = 0;
    }

    public void pause() {
        state = 2;
        playback(lastPlayed);
        Log.d("LASTPLAYED", String.valueOf(lastPlayed));
    }

    public void play(final double skip) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("SKIP", String.valueOf(skip));
                if (filepPath == null)
                    return;
                int intSize = AudioTrack.getMinBufferSize(Config.sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT);

                final AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC, Config.sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT, intSize, AudioTrack.MODE_STREAM);

                if (at == null) {
                    Log.d("TCAudio", "audio track is not initialised ");
                    return;
                }

                int count = 256; // 512 kb
//Reading the file..

                FileInputStream in = null;
                try {
                    in = new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                int toSkip = (int) skip / 1000 * Config.sampleRate;

                toSkip = (toSkip / 2 != 0 ? toSkip-- : toSkip);
                int bytesread = (int) (skip / 1000 * Config.sampleRate), ret = 0;

                //Log.d("SKIP", String.valueOf(bytesread));
                try {
                    in.skip((toSkip));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (state != 1) {
                    state = 1;
                    while (bytesread <= fileLength) {
                        if (state == 1) {
                            try {
                                ret = in.read(byteData, 0, count);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (ret != -1) { // Write the byte array to the track
                                //update(readBytes);
                                at.write(byteData, 0, count);
                                at.play();

                                byte[] readBytes = new byte[ret];
                                System.arraycopy(byteData, 0, readBytes, 0, readBytes.length);
                                bytesread += ret;
                                lastPlayed = bytesread;
                                playback(bytesread);
                                //Log.d("BYTESREAD", String.valueOf(bytesread));
                            } else {
                                playbackComplete();
                                break;
                            }
                        }
                        else if (state == 2) {
                            playback(lastPlayed);
                            break;
                        }
                        else if (state == 0) {
                            lastPlayed = 0;
                            playback(0);
                            break;
                        }
                    }
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if(state == 1)playbackComplete();
                    at.stop();
                    at.release();
                }
            }
        }).start();
    }

    public int getFileSize() {
        return fileLength;
    }

    public int getState() {
        return state;
    }

    public int getLastPlayed(){
        return lastPlayed;
    }
    public void addPlaybackListener(PlaybackListener listener) {
        playbackListener.add(listener);
    }

    private void playback(int bytesread) {
        for (PlaybackListener listener : playbackListener) {
            listener.playback(bytesread);
        }
    }

    public void addCompletionListener(CompletionListener listener) {
        completionListener.add(listener);
    }

    public int getDurationInMs() {
        return (getFileSize() * 1000) / (Config.sampleRate * 2);
    }

    private void playbackComplete() {
        state = 0;
        lastPlayed = 0;
        for (CompletionListener listener : completionListener) {
            listener.playbackComplete();
        }
    }
}
