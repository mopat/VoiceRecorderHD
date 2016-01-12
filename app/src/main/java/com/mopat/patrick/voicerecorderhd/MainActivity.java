package com.mopat.patrick.voicerecorderhd;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements PlaybackListener, CompletionListener {

    private Button recordButton, playButton, stopButton, pauseButton;
    private Recorder recorder;
    private Recording recording;
    private SeekBar seekBar;
    private TextView playbackTime, durationTime;

    private int lastPlayedBytes;
    private boolean isPaused, isPlaying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createDirectory();
        init();
        initListeners();
        initSeekBar();
    }

    private void createDirectory() {
        if (!Absolutes.DIRECTORY.exists()) {
            if (!Absolutes.DIRECTORY.mkdir()) ; //directory is created;
        }
    }

    private void init() {
        recordButton = (Button) findViewById(R.id.record_button);
        playButton = (Button) findViewById(R.id.play_button);
        seekBar = (SeekBar) findViewById(R.id.seekbar_main);
        stopButton = (Button) findViewById(R.id.stop_button);
        pauseButton = (Button) findViewById(R.id.pause_button);
        playbackTime = (TextView) findViewById(R.id.playback_time);
        durationTime = (TextView) findViewById(R.id.duration_time);
        recorder = new Recorder();

        isPaused = false;
        isPlaying = false;
    }

    private void initListeners() {
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!recorder.isRecording()) {
                    recorder.startRecording();
                    recordButton.setText("STOP");
                } else if (recorder.isRecording()) {
                    recorder.stopRecording();
                    recordButton.setText("START");
                    recording = new Recording(recorder.getFilePath(), getApplicationContext());
                    durationTime.setText(formatTime(recording.getDurationInMs()));
                    seekBar.setMax(recording.getDurationInMs());

                    recording.addPlaybackListener(MainActivity.this);
                    recording.addCompletionListener(MainActivity.this);
                }
            }
        });
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recording != null) {
                    //Log.d("STATE", String.valueOf(recording.getState()));
                    recording.play(seekBar.getProgress() * 2);
            /*        if (!isPlaying && !isPaused) {
                        isPaused = false;
                        isPlaying = true;
                        recording.play(0);
                        playButton.setText("Pause");
                    } else if (isPlaying) {
                        isPlaying = false;
                        isPaused = true;
                        recording.pause();
                        playButton.setText("Play");
                    } else if (isPaused) {
                        isPlaying = true;
                        isPaused = false;
                        recording.play(lastPlayedBytes * 2);
                        playButton.setText("Pause");
                    }*/
                }
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recording.stop();
                seekBar.setProgress(0);
            }
        });
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recording.pause();
            }
        });
    }

    private void initSeekBar() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String currentTimeString = formatTime(progress);
                playbackTime.setText(currentTimeString);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (recording.getState() == 1) {
                    recording.pause();
                    //recording.play(lastPlayedBytes * 2);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void playback(final int bytesread) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                double currentTime = byteToInt(bytesread);

                seekBar.setProgress((int) (currentTime));
                lastPlayedBytes = (int) (currentTime);
                String currentTimeString = formatTime(currentTime);
                playbackTime.setText(currentTimeString);
            }
        });
    }

    private double byteToInt(int bytesread){
        return (bytesread * 1000) / (Config.sampleRate * 2);
    }

    private String formatTime(double timeInMs) {
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes((int) timeInMs),
                TimeUnit.MILLISECONDS.toSeconds((int) timeInMs),
                (TimeUnit.MILLISECONDS.toMillis((int) timeInMs) - TimeUnit.MILLISECONDS.toSeconds((int) timeInMs) * 1000) / 10
        );
    }


    @Override
    public void playbackComplete() {
        recording.stop();
        seekBar.setProgress(0);
    }
}
