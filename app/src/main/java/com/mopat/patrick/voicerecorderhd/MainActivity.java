package com.mopat.patrick.voicerecorderhd;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.mopat.patrick.voicerecorderhd.AppRater;
import com.mopat.patrick.voicerecorderhd.GoForPro;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PlaybackListener, CompletionListener, PauseListener, PlayListener, StopListener, RecordingListener {
    private ImageButton recordButton, playButton, myRecordingsButton, stopButton, pauseRecordingButton, cancelRecordingbutton, setSamplerateButton;
    private Recorder recorder;
    private Recording recording;
    private SeekBar seekBar;
    private TextView playbackTime, durationTime, filenameTextView, recordDurationTextView, filesizeTextView;
    private Spinner samplerateSpinner;
    private Resources res;
    private VisualizerView mVisualizerView;
    private Visualizer mVisualizer;
    private byte[] resetBytes;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    private InterstitialAd mInterstitialAd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (checkAndRequestPermissions()) {
            createDirectory();
            init();
            initListeners();
            initSeekBar();
            loadSampleRate();
            checkIntent();
            MobileAds.initialize(this, Absolutes.ADMOB_ID);

            mInterstitialAd = new InterstitialAd(this);
            mInterstitialAd.setAdUnitId(Absolutes.AD_UNIT_ID);
            mInterstitialAd.loadAd(new AdRequest.Builder().build());

        }

        GoForPro.app_launched(MainActivity.this);
    }


    private boolean checkAndRequestPermissions() {
        int storagePermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        int externalStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int modifyAudioSettingsPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.MODIFY_AUDIO_SETTINGS);
        int recordAudioPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        int internetPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);


        List<String> listPermissionsNeeded = new ArrayList<>();
        if (externalStoragePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (storagePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (modifyAudioSettingsPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.MODIFY_AUDIO_SETTINGS);
        }
        if (recordAudioPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
        }
        if (internetPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.INTERNET);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            Toast.makeText(this, "You need to grant all Permissions for using this HD Voice Recorder Pro", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == REQUEST_ID_MULTIPLE_PERMISSIONS) {
            Intent i = getBaseContext().getPackageManager()
                    .getLaunchIntentForPackage(getBaseContext().getPackageName());
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }
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
        pauseRecordingButton = (ImageButton) findViewById(R.id.pause_recording_button);
        myRecordingsButton = (ImageButton) findViewById(R.id.my_recordings_button);
        cancelRecordingbutton = (ImageButton) findViewById(R.id.cancel_recording_button);
        setSamplerateButton = (ImageButton) findViewById(R.id.set_samplerate_button);
        samplerateSpinner = (Spinner) findViewById(R.id.samplerate_spinner);
        playbackTime = (TextView) findViewById(R.id.playback_time);
        durationTime = (TextView) findViewById(R.id.duration_time);
        filenameTextView = (TextView) findViewById(R.id.filename_text_view);
        recordDurationTextView = (TextView) findViewById(R.id.record_duration_textview);
        filesizeTextView = (TextView) findViewById(R.id.filesize_text_view);
        mVisualizerView = (VisualizerView) findViewById(R.id.myvisualizerview);
        mVisualizerView.updateVisualizer(resetBytes);
        recorder = new Recorder(getApplicationContext());
        recorder.addRecordingListener(MainActivity.this);
        res = getResources();

        playbackTime.setText(TimeFormat.formatTime(0.0));
        durationTime.setText(TimeFormat.formatTime(0.0));

        resetBytes = new byte[recorder.bufferSize];
        Arrays.fill(resetBytes, (byte) 0);

        setupVisualizerFxAndUI();

 /*       String[] ar = res.getStringArray(R.array.samplerate_array);
        ArrayAdapter<String> adapter = new ArrayAdapter(getApplicationContext(), R.layout.my_simple_list_item, ar);
        samplerateSpinner.setAdapter(adapter);*/
    }

    private void setupVisualizerFxAndUI() {
        // Create the Visualizer object and attach it to our media player.
        mVisualizer = new Visualizer(0);
        if(Visualizer.getCaptureSizeRange().length > 0){
            mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[0]);
        }
        else{
            mVisualizer.setCaptureSize(1024);

        }
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
                if (filename.indexOf(".") > 0) {
                    filename = filename.replace(".", "_");
                }
                filename = filename.replaceAll("\\s", "");
                dialog.dismiss();
                recorder.renameFile(filename);
                initRecording(recorder.getFilePath(), recorder.getRecordingFilename() + Config.filetype, recorder.getSamplerate());
                Toast.makeText(getApplicationContext(), "File stored at " + Absolutes.DIRECTORY + "/" + filename + Config.filetype, Toast.LENGTH_LONG).show();
                mVisualizerView.updateVisualizer(resetBytes);
                filesizeTextView.setText(FileSizeFormat.getFormattedFileSize(recording.getFileSize()));
                recordDurationTextView.setText(TimeFormat.formatTime(recording.getDurationInMs()));
                pauseRecordingButton.setBackgroundResource(R.drawable.ic_pause_circle_filled_black_48dp_disabled);
                cancelRecordingbutton.setBackgroundResource(R.drawable.ic_close_circle_filled_black_48dp_disabled);
                AppRater.app_launched(MainActivity.this);
                if (mInterstitialAd.isLoaded() && !Absolutes.IS_PRO) {
                    mInterstitialAd.show();
                } else {
                    Log.d("AD", "The interstitial wasn't loaded yet.");
                }
            }
        });
        saveRecordingDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                recorder.deleteFile();
                resetViews();
                Toast.makeText(getApplicationContext(), "File not saved", Toast.LENGTH_LONG).show();
                pauseRecordingButton.setBackgroundResource(R.drawable.ic_pause_circle_filled_black_48dp_disabled);
                cancelRecordingbutton.setBackgroundResource(R.drawable.ic_close_circle_filled_black_48dp_disabled);
                if (mInterstitialAd.isLoaded() && !Absolutes.IS_PRO) {
                    mInterstitialAd.show();
                } else {
                    Log.d("AD", "The interstitial wasn't loaded yet.");
                }
            }
        });
        saveRecordingDialog.show();
    }

    public boolean isValidSampleRate() {
        int bufferSize = AudioRecord.getMinBufferSize(Integer.parseInt(samplerateSpinner.getSelectedItem().toString()), AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
            // buffer size is valid, Sample rate supported
            return true;
        }
        return false;
    }

    private void initListeners() {
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //reset recording
                if (recording != null) {
                    recording.stop();
                    resetViews();
                    recording = null;
                }

                if (!recorder.isRecording()) {
                    if (isValidSampleRate()) {
                        recorder.startRecording();
                        setupVisualizerFxAndUI();
                        mVisualizer.setEnabled(true);
                        recordButton.setBackgroundResource(R.drawable.rec_with_stop);
                        setRecAnimation();
                        disableViews();
                        resetViews();
                        pauseRecordingButton.setBackgroundResource(R.drawable.ic_pause_circle_filled_black_48dp);
                        cancelRecordingbutton.setBackgroundResource(R.drawable.ic_close_circle_filled_black_48dp);
                    } else {
                        Toast.makeText(getApplicationContext(), "Samplerate not supported", Toast.LENGTH_SHORT).show();
                    }

                } else if (recorder.isRecording()) {
                    recorder.stopRecording();
                    showSaveDialog();
                    mVisualizer.setEnabled(false);
                    recordButton.setBackgroundResource(R.drawable.ic_mic_black_48dp);
                    pauseRecordingButton.setBackgroundResource(R.drawable.ic_pause_circle_filled_black_48dp_disabled);
                    cancelRecordingbutton.setBackgroundResource(R.drawable.ic_close_circle_filled_black_48dp_disabled);
                    recordButton.clearAnimation();
                    enableViews();
                }
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recording != null) {
                    if (recording.getState() == 2 || recording.getState() == 0) {
                        recording.play(seekBar.getProgress() * 2);
                        playButton.setBackgroundResource(R.drawable.ic_pause_black_48dp);
                    } else if (recording.getState() == 1) {
                        recording.pause();
                        playButton.setBackgroundResource(R.drawable.ic_play_arrow_black_48dp);
                    }
                }
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recording != null) {
                    recording.stop();
                    playButton.setBackgroundResource(R.drawable.ic_play_arrow_black_48dp);
                }

            }
        });
        myRecordingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, MyRecordingsActivity.class);
                if (recording != null && recording.getState() != 0)
                    recording.stop();
                startActivity(i);
                overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
            }
        });
        pauseRecordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recorder.getState() == 1) {
                    recorder.pause();
                    recordButton.clearAnimation();
                    pauseRecordingButton.setBackgroundResource(R.drawable.continue_rec);
                } else if (recorder.getState() == 2) {
                    recorder.continueRecording();
                    setRecAnimation();
                    pauseRecordingButton.setBackgroundResource(R.drawable.ic_pause_circle_filled_black_48dp);
                    cancelRecordingbutton.setBackgroundResource(R.drawable.ic_close_circle_filled_black_48dp);
                }
            }
        });
        cancelRecordingbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recorder.isRecording()) {
                    showCancelDialog();
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
                    durationTime.setText(TimeFormat.formatTime(recording.getDurationInMs()));
                }
                if (mInterstitialAd.isLoaded() && !Absolutes.IS_PRO) {
                    mInterstitialAd.show();
                } else {
                    Log.d("AD", "The interstitial wasn't loaded yet.");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        setSamplerateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!Absolutes.IS_PRO){
                    AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
                    adb.setTitle("Pro-Version required");
                    adb.setMessage("HD Voice Recorder Pro is required the save Tracks with a new samplerate.");
                    adb.setNegativeButton("Cancel", null);
                    adb.setPositiveButton("Visit Play Store", new AlertDialog.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + Absolutes.PRO_PACKAGE_NAME)));
                            } catch (ActivityNotFoundException anfe) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + Absolutes.PRO_PACKAGE_NAME)));
                            }
                        }
                    });
                    adb.show();
                }
            }
        });
    }

    private void showCancelDialog() {
        AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
        adb.setTitle("Cancel Recording");
        adb.setMessage("Are you sure you want to cancel current recording?");
        adb.setNegativeButton("No", null);
        adb.setPositiveButton("Yes", new AlertDialog.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                recorder.cancelRecording();
                mVisualizer.setEnabled(false);
                recording = null;
                resetViews();
                recordButton.setBackgroundResource(R.drawable.ic_mic_black_48dp);
                pauseRecordingButton.setBackgroundResource(R.drawable.ic_pause_circle_filled_black_48dp_disabled);

                recordButton.clearAnimation();
                enableViews();
            }
        });
        adb.show();
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
                durationTime.setText(TimeFormat.formatTime(0.0));
            }
        });
        adb.show();
    }

    private void initRecording(String filePath, String filename, int samplerate) {
        recording = new Recording(filePath, samplerate, MainActivity.this);
        filenameTextView.setText(filename);
        recordDurationTextView.setText(TimeFormat.formatTime(recording.getDurationInMs()));
        filesizeTextView.setText(FileSizeFormat.getFormattedFileSize(recording.getFileSize()));
        durationTime.setText(TimeFormat.formatTime(recording.getDurationInMs()));
        seekBar.setMax(recording.getDurationInMs());

        recording.addPlaybackListener(MainActivity.this);
        recording.addCompletionListener(MainActivity.this);
        recording.addPauseListener(MainActivity.this);
        recording.addPlayListener(MainActivity.this);
        recording.addStopListener(MainActivity.this);

        setSamplerateButton.setBackgroundResource(R.drawable.ic_menu_save_48dp);
    }

    private void setRecAnimation() {
        AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
        anim.setDuration(1000);
        anim.setRepeatCount((int) Double.POSITIVE_INFINITY);
        anim.setRepeatMode(Animation.REVERSE);
        recordButton.startAnimation(anim);
    }

    private void checkIntent() {
        if (getIntent().hasExtra("filepath")) {
            String filePath = getIntent().getStringExtra("filepath");
            String filename = getIntent().getStringExtra("filename");
            String samplerate = getIntent().getStringExtra("samplerate");
            Config.sampleRate = Integer.parseInt(samplerate);
            initRecording(filePath, filename, Integer.parseInt(samplerate));
            seekBar.setMax(recording.getDurationInMs());
            Log.d("rate", String.valueOf(Config.sampleRate));

            samplerateSpinner.setSelection(getSpinnerIndex(samplerate));
            storeSampleRate(getSpinnerIndex(samplerate));
            playButton.setBackgroundResource(R.drawable.ic_pause_black_48dp);
            recording.play(0);
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
        playbackTime.setText(TimeFormat.formatTime(0.0));
        durationTime.setText(TimeFormat.formatTime(0.0));
        recordDurationTextView.setText(TimeFormat.formatTime(0.0));
        filenameTextView.setText("");
        mVisualizerView.updateVisualizer(resetBytes);
        filesizeTextView.setText("");
        pauseRecordingButton.setBackgroundResource(R.drawable.ic_pause_circle_filled_black_48dp_disabled);
        cancelRecordingbutton.setBackgroundResource(R.drawable.ic_close_circle_filled_black_48dp_disabled);
        setSamplerateButton.setBackgroundResource(R.drawable.ic_menu_save_disabled_48dp);
    }


    private void disableViews() {
        playButton.setEnabled(false);
        seekBar.setEnabled(false);
        samplerateSpinner.setEnabled(false);
    }

    private void enableViews() {
        playButton.setEnabled(true);
        seekBar.setEnabled(true);
        samplerateSpinner.setEnabled(true);
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
                String currentTimeString = TimeFormat.formatTime(progress);
                playbackTime.setText(currentTimeString);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (recording != null && recording.getState() == 1) {
                    recording.pause();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (recorder != null && recorder.isRecording()) {
            Toast.makeText(getApplicationContext(), "Stop recording to exit", Toast.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (recorder != null) {
            if (recorder.isRecording())
                recorder.stopRecording();
            else if (recording != null && recording.getState() == 1) {
                recording.pause();
            }
            recordButton.clearAnimation();
            recordButton.setBackgroundResource(R.drawable.ic_mic_black_48dp);
            mVisualizer.setEnabled(false);
            resetViews();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (recorder != null && recorder.isRecording()) {
            recorder.stopRecording();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
/*            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                return true;*/
            case R.id.action_delete_main:
                if (recording != null)
                    showDeleteDialog();
                else
                    Toast.makeText(getApplicationContext(), "No Recording", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_share_main:
                if (recording != null) {
                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                    sharingIntent.setType("audio/*");
                    Uri uri = Uri.parse(recording.getFilepPath());
                    sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    startActivity(Intent.createChooser(sharingIntent, "Share via"));
                } else
                    Toast.makeText(getApplicationContext(), "No Recording", Toast.LENGTH_SHORT).show();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private long byteToInt(int bytesread) {
        return ((long) bytesread * 1000) / (Config.sampleRate * 2);
    }

    @Override
    public void playback(final int bytesread, final byte[] playedBytes) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                double currentTime = byteToInt(bytesread);
                seekBar.setProgress((int) (currentTime));
                String currentTimeString = TimeFormat.formatTime(currentTime);
                playbackTime.setText(currentTimeString);
                mVisualizerView.updateVisualizer(playedBytes);
            }
        });
    }

    @Override
    public void playbackComplete() {
        Log.d("COMPLETE", "COMPLETE");
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (recording != null && recording.getState() != 0) {
                    playButton.setBackgroundResource(R.drawable.ic_play_arrow_black_48dp);
                }

            }
        });
        if (recording != null)
            recording.stop();
    }

    @Override
    public void playbackPaused() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (recording != null && recording.getState() == 2)
                    playButton.setBackgroundResource(R.drawable.ic_play_arrow_black_48dp);
            }
        });
    }

    @Override
    public void playbackPlay() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (recording != null && recording.getState() == 2)
                    playButton.setBackgroundResource(R.drawable.ic_play_arrow_black_48dp);
            }
        });
    }

    @Override
    public void playbackStop() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (recording != null && recording.getState() == 0)
                    playButton.setBackgroundResource(R.drawable.ic_play_arrow_black_48dp);
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
                if (recordedBytes == 0) {
                    recordDurationTextView.setText("00:00:00");
                    filesizeTextView.setText("");
                } else {
                    String currentTimeString = TimeFormat.formatTime(currentTime);
                    recordDurationTextView.setText(currentTimeString);
                    filesizeTextView.setText(FileSizeFormat.getFormattedFileSize(recordedBytes));
                }
            }
        });
    }

    @Override
    public void maximumRecordingSizeReached() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Maximum recoding length reached", Toast.LENGTH_LONG).show();
                recorder.stopRecording();
                showSaveDialog();
                mVisualizer.setEnabled(false);
                recordButton.setBackgroundResource(R.drawable.ic_mic_black_48dp);
                pauseRecordingButton.setBackgroundResource(R.drawable.ic_pause_circle_filled_black_48dp_disabled);
                cancelRecordingbutton.setBackgroundResource(R.drawable.ic_close_circle_filled_black_48dp_disabled);
                recordButton.clearAnimation();
                enableViews();
            }
        });
    }

}
