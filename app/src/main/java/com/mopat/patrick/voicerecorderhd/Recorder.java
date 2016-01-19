package com.mopat.patrick.voicerecorderhd;

/**
 * Created by Patrick on 10.01.2016.
 */

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.text.Editable;
import android.util.Log;

import org.cmc.music.common.ID3WriteException;
import org.cmc.music.metadata.IMusicMetadata;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Patrick on 04.01.2016.
 */
public class Recorder {
    private Context context;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord recorder = null;
    private Thread recordingThread, stopRecordingThread = null;
    private boolean isRecording = false;
    private String recordingPath = null, recordingFilename = null;
    long recordTime, st;
    int bufferSize = AudioRecord.getMinBufferSize(Config.sampleRate,
            RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
    private List<RecordingListener> recordingListenerList = new ArrayList<>();

    private int samplerate;
    int BufferElements2Rec = 512 / 2; // want to play 2048 (2K) since 2 bytes we use only 1024
    int BytesPerElement = 2; // 2 bytes in 16bit format
    private File recording;

    public Recorder(Context context) {
        this.context = context;
    }

    public void startRecording() {
        this.samplerate = Config.sampleRate;
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                samplerate, RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING, BufferElements2Rec * BytesPerElement);


        recorder.startRecording();

        recordingThread = new Thread(new Runnable() {
            public void run() {
                isRecording = true;
                writeAudioDataToFile();
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }

    //convert short to byte
    private byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];
        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;
    }

    private void setMetadata() {
        MusicMetadataSet srcSet = null;
        try {
            srcSet = new MyID3().read(recording);
        } catch (IOException e) {
            e.printStackTrace();
        }
        MusicMetadata musicMetadata = new MusicMetadata("name");
        musicMetadata.setArtist(context.getResources().getString(R.string.app_name));
        musicMetadata.setComment(String.valueOf(Config.sampleRate));

        try {
            new MyID3().update(recording, srcSet, musicMetadata);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ID3WriteException e) {
            e.printStackTrace();
        }
    }

    private void writeAudioDataToFile() {
        // Write the output audio in byte
        int numOfFilesInDirectory = Absolutes.DIRECTORY.list().length;
        recordingFilename = Absolutes.RECORDING_DEF_NAME + String.valueOf(numOfFilesInDirectory + Config.filetype);
        recording = new File(Absolutes.DIRECTORY + "/" + recordingFilename);
        int written = 0;
        short sData[] = new short[BufferElements2Rec];
        recordingPath = recording.getAbsolutePath();
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(recordingPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while (isRecording) {
            recorder.read(sData, 0, BufferElements2Rec);
            try {
                byte bData[] = short2byte(sData);

                os.write(bData, 0, BufferElements2Rec * BytesPerElement);
                written += bData.length;
                triggerWrittenBytes(written, bData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        setMetadata();
    }

    public void renameFile(String filename) {
        recordingFilename = filename;
        File from = new File(Absolutes.DIRECTORY, recordingFilename + Config.filetype);
        File to = new File(Absolutes.DIRECTORY, recordingFilename);
        from.renameTo(to);
    }

    public void deleteFile() {
        recording.delete();
    }

    public String getRecordingFilename() {
        return recordingFilename;
    }

    public void stopRecording() {
        stopRecordingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // stops the recording activity
                if (null != recorder) {
                    isRecording = false;
                    recorder.stop();
                    recorder.release();
                    recorder = null;
                    recordingThread = null;
                }
                recordTime = System.currentTimeMillis() - st;
                Log.d("RECORDTIME", String.valueOf(recordTime));
                stopRecordingThread = null;
            }
        });
        stopRecordingThread.start();
    }

    public boolean isRecording() {
        return isRecording;
    }

    public String getFilePath() {
        return recordingPath;
    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public int getAudioSessionId(){
        return recorder.getAudioSessionId();
    }

    public int getSamplerate() {
        return samplerate;
    }

    public void addRecordingListener(RecordingListener recordingListener) {
        recordingListenerList.add(recordingListener);
    }

    private void triggerWrittenBytes(final int written, final byte[] bytes) {
        for (RecordingListener listener : recordingListenerList) {
            listener.recordedBytes(written, bytes);
        }
    }
}
