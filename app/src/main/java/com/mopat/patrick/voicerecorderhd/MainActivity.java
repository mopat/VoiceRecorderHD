package com.mopat.patrick.voicerecorderhd;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements PlaybackListener, CompletionListener, PauseListener, PlayListener, StopListener {

    private Button recordButton, playButton, stopButton, myRecordingsButton, samplerateButton;
    private Recorder recorder;
    private Recording recording;
    private SeekBar seekBar;
    private TextView playbackTime, durationTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createDirectory();
        init();
        initListeners();
        initSeekBar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
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
        myRecordingsButton = (Button) findViewById(R.id.my_recordings_button);
        samplerateButton = (Button) findViewById(R.id.samplerate_button);
        playbackTime = (TextView) findViewById(R.id.playback_time);
        durationTime = (TextView) findViewById(R.id.duration_time);
        recorder = new Recorder();
    }

    private void showSamplerateAlertDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.samplerate_alert_dialog, null);
        AlertDialog.Builder sampleRateAlertDialog = new AlertDialog.Builder(this);
        sampleRateAlertDialog.setTitle("Quality");
        sampleRateAlertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        sampleRateAlertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        sampleRateAlertDialog.setView(dialoglayout);
        sampleRateAlertDialog.show();
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
                    recording.addPauseListener(MainActivity.this);
                    recording.addPlayListener(MainActivity.this);
                    recording.addStopListener(MainActivity.this);
                }
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recording != null) {
                    if (playButton.getText().equals("Play")) {
                        recording.play(seekBar.getProgress() * 2);
                    } else {
                        recording.pause();
                    }
                }
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recording.stop();
                playButton.setText("Play");
            }
        });
        myRecordingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, MyRecordingsActivity.class);
                startActivity(i);
            }
        });
        samplerateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSamplerateAlertDialog();
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

    private double byteToInt(int bytesread) {
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
    public void playback(final int bytesread) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                double currentTime = byteToInt(bytesread);

                seekBar.setProgress((int) (currentTime));
                //lastPlayedBytes = (int) (currentTime);
                String currentTimeString = formatTime(currentTime);
                playbackTime.setText(currentTimeString);
            }
        });
    }

    @Override
    public void playbackComplete() {
        Log.d("COMPLETE", "COMPLETE");
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!playButton.getText().equals("Play"))
                    playButton.setText("Play");
            }
        });
        recording.stop();
        //seekBar.setProgress(0);
    }

    @Override
    public void playbackPaused() {
        Log.d("PAUSE", "PAUSE");
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!playButton.getText().equals("Play"))
                    playButton.setText("Play");
            }
        });
    }

    @Override
    public void playbackPlay() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!playButton.getText().equals("Pause"))
                    playButton.setText("Pause");
            }
        });
    }

    @Override
    public void playbackStop() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!playButton.getText().equals("Play"))
                    playButton.setText("Play");
            }
        });
    }
}
