package com.mopat.patrick.voicerecorderhd;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.net.Uri;
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
    private Context context;
    private String filepPath;
    private byte[] byteData;
    private int fileLength, lastPlayed;
    private File file;
    private AudioManager am;
    private int state;
    private int samplerate;
    private List<PlaybackListener> playbackListener = new ArrayList<>();
    private List<CompletionListener> completionListener = new ArrayList<>();
    private List<PauseListener> pauseListener = new ArrayList<>();
    private List<PlayListener> playListener = new ArrayList<>();
    private List<StopListener> stopListener = new ArrayList<>();

    public Recording(String filePath, int samplerate, Context context) {
        this.context = context;
        am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.filepPath = filePath;
        this.file = new File(filePath);
        this.fileLength = (int) file.length();
        this.byteData = new byte[fileLength];
        this.state = 0;
        this.samplerate = samplerate;
    }

    public void stop() {
        playbackStop();
    }

    public void pause() {
        playbackPaused();
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

                int count = 512; // 512 kb
//Reading the file..

                FileInputStream in = null;
                try {
                    in = new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                int toSkip = skipToNumberOfBytes(skip);

                // toSkip = (toSkip % 2 != 0 ? toSkip-- : toSkip);
                if (toSkip % 2 != 0)
                    toSkip--;
                int bytesread = toSkip, ret = 0;

                Log.d("SKIP", String.valueOf(toSkip));
                try {
                    in.skip((toSkip));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                byte[] byties = invert(byteData);
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
                                playbackPlay();
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
                        } else if (state == 2) {
                            playback(lastPlayed);
                            playbackPaused();
                            break;
                        } else if (state == 0) {
                            playbackStop();
                            break;
                        }
                    }
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (state == 1)
                        playbackComplete();
                    at.stop();
                    at.release();
                }
            }
        }).start();
    }

    public void delete(){
        file.delete();
    }

    private byte[] invert(byte[] bytes) {
    byte[] invertedBytes = new byte[bytes.length];

        for(int i = 0; i < bytes.length; i++){
            invertedBytes[bytes.length-i-1] = bytes[i];
        }
        return invertedBytes;
    }

    public String getFilepPath(){
        return filepPath;
    }

    public int getFileSize() {
        return fileLength;
    }

    public int getState() {
        return state;
    }

    public int getDurationInMs() {
        return (getFileSize() * 1000) / (Config.sampleRate * 2);
    }

    private int skipToNumberOfBytes(double skip) {
        return (int) (skip / 1000 * Config.sampleRate);
    }

    /* Methods for interfaces*/
    //region PlaybackListener
    public void addPlaybackListener(PlaybackListener listener) {
        playbackListener.add(listener);
    }

    private void playback(int bytesread) {
        for (PlaybackListener listener : playbackListener) {
            listener.playback(bytesread);
        }
    }
    //endregion

    //region CompletionListener
    public void addCompletionListener(CompletionListener listener) {
        completionListener.add(listener);
    }

    private void playbackComplete() {
        state = 0;
        lastPlayed = 0;
        for (CompletionListener listener : completionListener) {
            listener.playbackComplete();
        }
    }
    //endregion

    //region PauseListener
    public void addPauseListener(PauseListener listener) {
        pauseListener.add(listener);
    }

    private void playbackPaused() {
        state = 2;
        for (PauseListener listener : pauseListener) {
            listener.playbackPaused();
        }
    }
    //endregion

    //region PlayListener
    public void addPlayListener(PlayListener listener) {
        playListener.add(listener);
    }

    private void playbackPlay() {
        for (PlayListener listener : playListener) {
            listener.playbackPlay();
        }
    }
    //endregion

    //region StopListener
    public void addStopListener(StopListener listener) {
        stopListener.add(listener);
    }

    private void playbackStop() {
        playback(0);
        state = 0;
        lastPlayed = 0;
        for (StopListener listener : stopListener) {
            listener.playbackStop();
        }
    }
    //endregion
}
