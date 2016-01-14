package com.mopat.patrick.voicerecorderhd;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements PlaybackListener, CompletionListener, PauseListener, PlayListener, StopListener {

    private Button recordButton, playButton, stopButton, myRecordingsButton;
    private Recorder recorder;
    private Recording recording;
    private SeekBar seekBar;
    private TextView playbackTime, durationTime;
    private Spinner samplerateSpinner;
    private Resources res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createDirectory();
        init();
        initListeners();
        initSeekBar();
        loadSampleRate();
        checkIntent();
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
        samplerateSpinner = (Spinner) findViewById(R.id.samplerate_spinner);
        playbackTime = (TextView) findViewById(R.id.playback_time);
        durationTime = (TextView) findViewById(R.id.duration_time);
        recorder = new Recorder(getApplicationContext());

        res = getResources();
    }

    private void initListeners() {
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!recorder.isRecording()) {
                    recorder.startRecording();
                    recordButton.setText("STOP");
                    samplerateSpinner.setEnabled(false);
                } else if (recorder.isRecording()) {
                    recorder.stopRecording();
                    recordButton.setText("START");
                    initRecording(recorder.getFilePath(), recorder.getSamplerate());
                    samplerateSpinner.setEnabled(true);
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
        samplerateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                Config.sampleRate = Integer.valueOf(selected);
                storeSampleRate(position);
                if (recording != null){
                    seekBar.setMax(recording.getDurationInMs());
                    durationTime.setText(formatTime(recording.getDurationInMs()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    //private method of your class
    private int getSpinnerIndex(String samplerate)
    {
        int index = 0;

        for (int i=0;i<samplerateSpinner.getCount();i++){
            if (samplerateSpinner.getItemAtPosition(i).toString().equalsIgnoreCase(samplerate)){
                index = i;
                break;
            }
        }
        return index;
    }

    private void initRecording(String filePath, int samplerate) {
        recording = new Recording(filePath, samplerate, MainActivity.this);
        durationTime.setText(formatTime(recording.getDurationInMs()));
        seekBar.setMax(recording.getDurationInMs());

        recording.addPlaybackListener(MainActivity.this);
        recording.addCompletionListener(MainActivity.this);
        recording.addPauseListener(MainActivity.this);
        recording.addPlayListener(MainActivity.this);
        recording.addStopListener(MainActivity.this);
    }

    private void checkIntent() {
        if (getIntent().hasExtra("filepath")) {
            String filePath = getIntent().getStringExtra("filepath");
            String samplerate = getIntent().getStringExtra("samplerate");
            Log.d("FILEPATH", String.valueOf(getSpinnerIndex(samplerate)));
            initRecording(filePath, Integer.parseInt(samplerate));
            seekBar.setMax(recording.getDurationInMs());
            samplerateSpinner.setSelection(getSpinnerIndex(samplerate));
        }
    }

    private void storeSampleRate(int position) {
        SharedPreferences prefs = this.getSharedPreferences(res.getString(R.string.shared_preferences_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putInt(res.getString(R.string.samplerate_key, position), position);
        edit.commit();
        edit.apply();
    }

    private void loadSampleRate() {
        SharedPreferences prefs = this.getSharedPreferences(res.getString(R.string.shared_preferences_key), Context.MODE_PRIVATE); //1
        int position = prefs.getInt(res.getString(R.string.samplerate_key), Context.MODE_PRIVATE); //2
        samplerateSpinner.setSelection(position);
        Config.sampleRate = Integer.parseInt((String) samplerateSpinner.getSelectedItem());
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
