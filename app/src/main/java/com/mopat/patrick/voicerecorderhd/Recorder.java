package com.mopat.patrick.voicerecorderhd;

/**
 * Created by Patrick on 10.01.2016.
 */

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Created by Patrick on 04.01.2016.
 */
public class Recorder {
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord recorder = null;
    private Thread recordingThread, stopRecordingThread = null;
    private boolean isRecording = false;
    private String recordingPath = null;
    long recordTime, st;
    int written = 0;
    int bufferSize = AudioRecord.getMinBufferSize(Config.sampleRate,
            RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);

    int BufferElements2Rec = 512 / 2; // want to play 2048 (2K) since 2 bytes we use only 1024
    int BytesPerElement = 2; // 2 bytes in 16bit format
    File recording;


    public void startRecording() {
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                Config.sampleRate, RECORDER_CHANNELS,
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

    private void writeAudioDataToFile() {
        // Write the output audio in byte
        recording = new File(Absolutes.DIRECTORY + "/" + String.valueOf(System.currentTimeMillis())+ ".wav");
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
}
