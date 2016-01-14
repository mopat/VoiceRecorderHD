package com.mopat.patrick.voicerecorderhd;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class MyRecordingsActivity extends AppCompatActivity {
    private ListView myRecordingsListView;
    private MyRecordingsArrayAdapter myRecordingsArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_recording);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        init();

        displayMyRecordings();
        initListeners();
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
                Log.d("FILENAME", filename);
                Intent i = new Intent(MyRecordingsActivity.this, MainActivity.class);
                i.putExtra("filename", filename);
                startActivity(i);
            }
        });
    }

    private void setupListView() {

    }

    private void displayMyRecordings() {
        String path = Absolutes.DIRECTORY.toString();
        Log.d("Files", "Path: " + path);
        File f = new File(path);
        File file[] = f.listFiles();
        ArrayList<MyRecordingsListitem> myRecordings = new ArrayList<>();
        Log.d("Files", "Size: " + file.length);
        for (int i = 0; i < file.length; i++) {
            //Log.d("Files", "FileName:" + file[i].getName());
            myRecordings.add(new MyRecordingsListitem(file[i].getName(), "0", false));
        }
        myRecordingsArrayAdapter = new MyRecordingsArrayAdapter(this, myRecordings);
        myRecordingsListView.setAdapter(myRecordingsArrayAdapter);
        myRecordingsArrayAdapter.notifyDataSetChanged();

    }
}

