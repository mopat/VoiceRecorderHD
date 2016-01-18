package com.mopat.patrick.voicerecorderhd;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements PlaybackListener, CompletionListener, PauseListener, PlayListener, StopListener {

    private Button playButton, stopButton, myRecordingsButton, deleteButton;
    private ImageButton recordButton;
    private Recorder recorder;
    private Recording recording;
    private SeekBar seekBar;
    private TextView playbackTime, durationTime, filenameTextView;
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
        recordButton = (ImageButton) findViewById(R.id.record_button);
        playButton = (Button) findViewById(R.id.play_button);
        seekBar = (SeekBar) findViewById(R.id.seekbar_main);
        stopButton = (Button) findViewById(R.id.stop_button);
        myRecordingsButton = (Button) findViewById(R.id.my_recordings_button);
        deleteButton = (Button) findViewById(R.id.delete_button);
        samplerateSpinner = (Spinner) findViewById(R.id.samplerate_spinner);
        playbackTime = (TextView) findViewById(R.id.playback_time);
        durationTime = (TextView) findViewById(R.id.duration_time);
        filenameTextView = (TextView) findViewById(R.id.filename_text_view);
        recorder = new Recorder(getApplicationContext());

        res = getResources();
    }

    private void showSaveDialog() {
        final AlertDialog.Builder saveRecordingDialog = new AlertDialog.Builder(this);

        saveRecordingDialog.setTitle("Save File?");
        saveRecordingDialog.setCancelable(false);
        final EditText filenameEditText = new EditText(MainActivity.this);
        filenameEditText.setHint("Filename");
        saveRecordingDialog.setView(filenameEditText);
        filenameEditText.setText(recorder.getRecordingFilename());
        saveRecordingDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                String filename = filenameEditText.getText().toString();
                recorder.renameFile(filename);
                initRecording(recorder.getFilePath(), recorder.getRecordingFilename(), recorder.getSamplerate());
                Toast.makeText(getApplicationContext(), "File saved under " + Absolutes.DIRECTORY + "/" + filename + Config.filetype, Toast.LENGTH_LONG).show();
            }
        });
        saveRecordingDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                recorder.deleteFile();
                Toast.makeText(getApplicationContext(), "File not saved", Toast.LENGTH_LONG).show();
            }
        });
        saveRecordingDialog.show();
    }

    private void initListeners() {
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!recorder.isRecording()) {
                    recorder.startRecording();
                    recordButton.setBackgroundResource(R.drawable.ic_stop_white_48dp);
                    samplerateSpinner.setEnabled(false);
                } else if (recorder.isRecording()) {
                    recorder.stopRecording();
                    showSaveDialog();
                    recordButton.setBackgroundResource(R.drawable.ic_mic_white_48dp);
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
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteDialog();
            }
        });
        samplerateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                Config.sampleRate = Integer.valueOf(selected);
                storeSampleRate(position);
                if (recording != null) {
                    seekBar.setMax(recording.getDurationInMs());
                    durationTime.setText(formatTime(recording.getDurationInMs()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void showDeleteDialog(){
        AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
        adb.setTitle("Delete?");
        adb.setMessage("Are you sure you want to delete " + filenameTextView.getText());
        adb.setNegativeButton("Cancel", null);
        adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                recording.delete();
                Toast.makeText(getApplicationContext(), "File deleted", Toast.LENGTH_LONG).show();
                recording = null;
                resetViews();
            }
        });
        adb.show();
    }

    private void initRecording(String filePath, String filename, int samplerate) {
        recording = new Recording(filePath, samplerate, MainActivity.this);
        filenameTextView.setText(filename);
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
            String filename = getIntent().getStringExtra("filename");
            String samplerate = getIntent().getStringExtra("samplerate");
            Log.d("FILEPATH", String.valueOf(getSpinnerIndex(samplerate)));
            initRecording(filePath, filename, Integer.parseInt(samplerate));
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

    private void resetViews(){
        playbackTime.setText(formatTime(0.0));
        durationTime.setText(formatTime(0.0));
        filenameTextView.setText("None");
    }

    private int getSpinnerIndex(String samplerate) {
        int index = 0;

        for (int i = 0; i < samplerateSpinner.getCount(); i++) {
            if (samplerateSpinner.getItemAtPosition(i).toString().equalsIgnoreCase(samplerate)) {
                index = i;
                break;
            }
        }
        return index;
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                return true;
            case R.id.action_share_main:
                if (recording != null) {
                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                    sharingIntent.setType("audio/*");
                    Uri uri = Uri.parse(recording.getFilepPath());
                    sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    startActivity(Intent.createChooser(sharingIntent, "Share via"));
                } else
                    Toast.makeText(getApplicationContext(), "No Recording", Toast.LENGTH_LONG).show();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
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
