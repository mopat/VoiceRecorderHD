package com.mopat.patrick.voicerecorderhd;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.cmc.music.metadata.IMusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MyRecordingsActivity extends AppCompatActivity {
    private ListView myRecordingsListView;
    private MyRecordingsArrayAdapter myRecordingsArrayAdapter;
    private ArrayList<MyRecordingsListitem> myRecordings = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_recording);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        init();
        displayMyRecordings();
        initListeners();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_my_recordings, menu);
        return true;
    }

    private void init() {
        myRecordingsListView = (ListView) findViewById(R.id.my_recordings_listview);
    }

    private void initListeners() {
        myRecordingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView filenameTextView = (TextView) view.findViewById(R.id.filename);
                String filename = (String) filenameTextView.getText();
                String filePath = Absolutes.DIRECTORY + "/" + filename;
                Log.d("FILENAME", Absolutes.DIRECTORY + "/" + filename);
                Intent i = new Intent(MyRecordingsActivity.this, MainActivity.class);
                i.putExtra("filepath", filePath);
                i.putExtra("samplerate", getSampleRate(filePath));
                startActivity(i);
            }
        });
        myRecordingsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, long id) {
                TextView filenameTextView = (TextView) view.findViewById(R.id.filename);
                String filename = (String) filenameTextView.getText();
                String filePath = Absolutes.DIRECTORY + "/" + filename;
                final File file = new File(filePath);
                AlertDialog.Builder adb = new AlertDialog.Builder(MyRecordingsActivity.this);
                adb.setTitle("Delete?");
                adb.setMessage("Are you sure you want to delete " + filenameTextView.getText());
                adb.setNegativeButton("Cancel", null);
                adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        boolean delete = file.delete();
                        displayMyRecordings();
                    }
                });
                adb.show();

                return true;
            }
        });
    }

    private String getSampleRate(String filepath) {
        File src = new File(filepath);
        MusicMetadataSet srcSet = null;
        try {
            srcSet = new MyID3().read(src);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } // read metadata

        IMusicMetadata metadata = srcSet.getSimplified();

        return metadata.getComment();
    }

    private void setupListView() {

    }

    private String formatTime(double timeInMs) {
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes((int) timeInMs),
                TimeUnit.MILLISECONDS.toSeconds((int) timeInMs),
                (TimeUnit.MILLISECONDS.toMillis((int) timeInMs) - TimeUnit.MILLISECONDS.toSeconds((int) timeInMs) * 1000) / 10
        );
    }

    private void displayMyRecordings() {
        String path = Absolutes.DIRECTORY.toString();
        Log.d("Files", "Path: " + path);
        File f = new File(path);
        File file[] = f.listFiles();

        Log.d("Files", "Size: " + file.length);
        for (int i = 0; i < file.length; i++) {
            MusicMetadataSet srcSet = null;
            try {
                srcSet = new MyID3().read(file[i]);

            } catch (IOException e) {
                e.printStackTrace();
            }
            IMusicMetadata metadata = srcSet.getSimplified();
            String samplerate = metadata.getComment();
            String filename = file[i].getName();
            Log.d("file",(String)samplerate);
//            String duration = formatTime(file[i].length() * 1000 / (Integer.parseInt(samplerate) * 2));
            //String filesize = String.valueOf(file[i].length());
            //Log.d("Files", "FileName:" + file[i].getName());
            //myRecordings.add(new MyRecordingsListitem(filename, samplerate, filesize, duration, false));
        }
     /*   myRecordingsArrayAdapter = new MyRecordingsArrayAdapter(this, myRecordings);
        myRecordingsListView.setAdapter(myRecordingsArrayAdapter);
        myRecordingsArrayAdapter.notifyDataSetChanged();
*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                return true;

            case R.id.action_multiselect:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...

                Log.d("CLICK", "CLICK");

                for (int i = 0; i < myRecordingsListView.getChildCount(); i++) {
                    Log.d("COUNT", String.valueOf(i));
                    View v = myRecordingsListView.getChildAt(i);
                    CheckBox checkBox = (CheckBox) v.findViewById(R.id.listitem_checkbox);
                    checkBox.setVisibility(View.VISIBLE);
                    Log.d("ISCHECKED", String.valueOf(checkBox.isChecked()));
                }
                return true;
            case R.id.action_delete:
                for (int i = 0; i < myRecordingsListView.getChildCount(); i++) {
                    Log.d("COUNT", String.valueOf(i));
                    View v = myRecordingsListView.getChildAt(i);
                    CheckBox checkBox = (CheckBox) v.findViewById(R.id.listitem_checkbox);
                    TextView filenameTextView = (TextView) v.findViewById(R.id.filename);
                    Log.d("filenameTextView", (String) filenameTextView.getText());
                    if (checkBox.isChecked()) {
                        String filename = (String) filenameTextView.getText();
                        String filePath = Absolutes.DIRECTORY + "/" + filename;
                        final File file = new File(filePath);
                        boolean delete = file.delete();
                        displayMyRecordings();
                    }
                    Log.d("ISCHECKED", String.valueOf(checkBox.isChecked()));
                }
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
}

