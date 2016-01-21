package com.mopat.patrick.voicerecorderhd;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.audiofx.Visualizer;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements PlaybackListener, CompletionListener, PauseListener, PlayListener, StopListener, RecordingListener {
private Button pauseRecordingButton;
    private ImageButton recordButton, playButton, myRecordingsButton, stopButton;
    private Recorder recorder;
    private Recording recording;
    private SeekBar seekBar;
    private TextView playbackTime, durationTime, filenameTextView, recordDurationTextView;
    private Spinner samplerateSpinner;
    private Resources res;
    private VisualizerView mVisualizerView;
    private Visualizer mVisualizer;
    private byte[] resetBytes = new byte[1024];

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
        playButton = (ImageButton) findViewById(R.id.play_button);
        seekBar = (SeekBar) findViewById(R.id.seekbar_main);
        stopButton = (ImageButton) findViewById(R.id.stop_button);
        pauseRecordingButton = (Button)findViewById(R.id.pause_recording_button);
        myRecordingsButton = (ImageButton) findViewById(R.id.my_recordings_button);
        samplerateSpinner = (Spinner) findViewById(R.id.samplerate_spinner);
        playbackTime = (TextView) findViewById(R.id.playback_time);
        durationTime = (TextView) findViewById(R.id.duration_time);
        filenameTextView = (TextView) findViewById(R.id.filename_text_view);
        recordDurationTextView = (TextView) findViewById(R.id.record_duration_textview);
        mVisualizerView = (VisualizerView) findViewById(R.id.myvisualizerview);
        recorder = new Recorder(getApplicationContext());
        recorder.addRecordingListener(MainActivity.this);
        res = getResources();

        playbackTime.setText(formatTime(0.0));
        durationTime.setText(formatTime(0.0));

        Arrays.fill(resetBytes, (byte) 0);
        setupVisualizerFxAndUI();

 /*       String[] ar = res.getStringArray(R.array.samplerate_array);
        ArrayAdapter<String> adapter = new ArrayAdapter(getApplicationContext(), R.layout.my_simple_list_item, ar);
        samplerateSpinner.setAdapter(adapter);*/
    }

    private void setupVisualizerFxAndUI() {

        // Create the Visualizer object and attach it to our media player.
        mVisualizer = new Visualizer(0);
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[0]);
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
                resetViews();
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
                    setupVisualizerFxAndUI();
                    mVisualizer.setEnabled(true);
                    recordButton.setBackgroundResource(R.drawable.ic_stop_black_48dp);
                    samplerateSpinner.setEnabled(false);
                    resetViews();
                } else if (recorder.isRecording()) {
                    pauseRecordingButton.setText("pauserecording");
                    recorder.stopRecording();
                    showSaveDialog();
                    mVisualizer.setEnabled(false);
                    recordButton.setBackgroundResource(R.drawable.ic_mic_black_48dp);
                    samplerateSpinner.setEnabled(true);
                }
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recording != null) {
                    if (recording.getState() == 2 || recording.getState() == 0) {
                        recording.play(seekBar.getProgress() * 2);
                        playButton.setBackgroundResource(R.drawable.ic_pause_circle_filled_black_48dp);
                    } else if (recording.getState() == 1) {
                        recording.pause();
                        playButton.setBackgroundResource(R.drawable.ic_play_circle_filled_black_48dp);
                    }
                }
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recording.stop();
                playButton.setBackgroundResource(R.drawable.ic_play_circle_filled_black_48dp);
            }
        });
        myRecordingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, MyRecordingsActivity.class);
                startActivity(i);
            }
        });
        pauseRecordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recorder.getState() == 1) {
                    recorder.pause();
                    pauseRecordingButton.setText("continue");
                } else if (recorder.getState() == 2) {
                    recorder.continueRecording();
                    pauseRecordingButton.setText("PAUSERECORDING");
                }
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

    private void showDeleteDialog() {
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

    private void resetViews() {
        playbackTime.setText(formatTime(0.0));
        durationTime.setText(formatTime(0.0));
        recordDurationTextView.setText(formatTime(0.0));
        filenameTextView.setText("");
        mVisualizerView.updateVisualizer(resetBytes);
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
            case R.id.action_delete_main:
                showDeleteDialog();
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
                if (recording.getState() != 0)
                    playButton.setBackgroundResource(R.drawable.ic_play_circle_filled_black_48dp);
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
                if (recording.getState() == 2)
                    playButton.setBackgroundResource(R.drawable.ic_play_circle_filled_black_48dp);
            }
        });
    }

    @Override
    public void playbackPlay() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (recording.getState() == 2)
                    playButton.setBackgroundResource(R.drawable.ic_play_circle_filled_black_48dp);
            }
        });
    }

    @Override
    public void playbackStop() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (recording.getState() == 0)
                    playButton.setBackgroundResource(R.drawable.ic_play_circle_filled_black_48dp);
            }
        });
    }

    @Override
    public void recordedBytes(final int recordedBytes, final byte[] bytes) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mVisualizerView.updateVisualizer(bytes);
                double currentTime = byteToInt(recordedBytes);
                String currentTimeString = formatTime(currentTime);
                recordDurationTextView.setText(currentTimeString);
            }
        });
    }
}
