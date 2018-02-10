package com.mopat.patrick.voicerecorderhd;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
    private int filesize;
    public static int bufferSize = AudioRecord.getMinBufferSize(Config.sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
    private List<RecordingListener> recordingListenerList = new ArrayList<>();
    private int state = 0;

    private int samplerate;
    int BufferElements2Rec = bufferSize / 2; // want to play 2048 (2K) since 2 bytes we use only 1024
    int BytesPerElement = 2; // 2 bytes in 16bit format
    private File recording;
    private byte[] resetBytes = new byte[bufferSize];

    public Recorder(Context context) {
        this.context = context;
        Arrays.fill(resetBytes, (byte) 0);
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

    private void writeAudioDataToFile() {
        // Write the output audio in byte
        int numOfFilesInDirectory = Absolutes.DIRECTORY.list().length;
        recordingFilename = Absolutes.RECORDING_DEF_NAME + String.valueOf(numOfFilesInDirectory);
        recording = new File(Absolutes.DIRECTORY + "/" + recordingFilename + Config.filetype);
        Log.d("DIRECTORY", recordingFilename);
        int written = 0;
        short sData[] = new short[BufferElements2Rec];
        recordingPath = recording.getAbsolutePath();
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(recordingPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        state = 1;
  /*     try {
            os.write(getHeader(), 0, getHeader().length);
        } catch (IOException e) {
            e.printStackTrace();
        */
        ArrayList<Byte> bytesList = new ArrayList<>();
        for (int j = 0; j < getHeader().length; j++) {
            bytesList.add(getHeader()[j]);
        }
        while (isRecording) {
            if (state == 1) {
                try {
                    // create lots of objects here and stash them somewhere
                    recorder.read(sData, 0, BufferElements2Rec);

                    byte bData[] = short2byte(sData);
                    for (byte b : bData) {
                        bytesList.add(b);
                    }
//concat(bData);
                    //os.write(bData, 0, BufferElements2Rec * BytesPerElement);
                    written += bData.length;

                    triggerWrittenBytes(written, bData);
                    filesize = written;
                } catch (OutOfMemoryError E) {
                    maximumRecordingSizeReached();
//
                }
            }
        }
        //write header first

        try {
            os.write(getHeader(), 0, getHeader().length);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //write bytes
        try {
            //
            Byte[] byties = bytesList.toArray(new Byte[bytesList.size()]);
            byte[] bytes = toByte(byties);
            if(bytes != null){
                Toast.makeText(context, "Yo ran out of memory. Recording was stopped. ", Toast.LENGTH_LONG).show();
                stopRecording();
            }
            else{
                os.write(toByte(byties));
            }
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] toByte(Byte[] B) {
        try {
            byte[] b2 = new byte[B.length];
            for (int i = 0; i < B.length; i++) {
                b2[i] = B[i];
            }
            return b2;
        }
        catch(OutOfMemoryError e) {
            return null;
        }

    }

    private byte[] getHeader() {

        long longSampleRate = Config.sampleRate;
        int channels = 1;
        long byteRate = 192;

        //Stops playback at totalAudioLen
        int totalAudioLen = filesize;
        int totalDataLen = totalAudioLen + 36;
        byte[] header = new byte[44];

        header[0] = (byte) 'R'; // RIFF/WAVE header
        header[1] = (byte) 'I';
        header[2] = (byte) 'F';
        header[3] = (byte) 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = (byte) 'W';
        header[9] = (byte) 'A';
        header[10] = (byte) 'V';
        header[11] = (byte) 'E';
        header[12] = (byte) 'f'; // 'fmt ' chunk
        header[13] = (byte) 'm';
        header[14] = (byte) 't';
        header[15] = (byte) ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8); // block align
        header[33] = 0;
        header[34] = 16; // bits per sample
        header[35] = 0;
        header[36] = (byte) 'd';
        header[37] = (byte) 'a';
        header[38] = (byte) 't';
        header[39] = (byte) 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        return header;
    }

    byte[] allBytes = new byte[0];

    public void concat(byte[] b) {
        int aLen = allBytes.length;
        int bLen = b.length;
        byte[] c = new byte[aLen + bLen];
        System.arraycopy(allBytes, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        allBytes = c;
        Log.d("LENGTH", String.valueOf(allBytes.length));
    }

    public void renameFile(String filename) {
        File from = new File(Absolutes.DIRECTORY, recordingFilename + Config.filetype);
        File to = new File(Absolutes.DIRECTORY, filename + Config.filetype);
        recordingFilename = filename;
        from.renameTo(to);
    }

    public void deleteFile() {
        recording.delete();
    }

    public void pause() {
        state = 2;
    }

    public void continueRecording() {
        state = 1;
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
                    state = 0;
                    triggerWrittenBytes(0, resetBytes);
                }
                stopRecordingThread = null;
            }
        });
        stopRecordingThread.start();
    }


    public void cancelRecording() {
        state = 0;
        stopRecording();
        deleteFile();
    }

    public boolean isRecording() {
        return isRecording;
    }

    public int getState() {
        return state;
    }

    public String getFilePath() {
        return recordingPath;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public int getAudioSessionId() {
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

    private void maximumRecordingSizeReached() {
        for (RecordingListener listener : recordingListenerList) {
            state = 0;
            isRecording = false;
            listener.maximumRecordingSizeReached();
        }
    }
}
