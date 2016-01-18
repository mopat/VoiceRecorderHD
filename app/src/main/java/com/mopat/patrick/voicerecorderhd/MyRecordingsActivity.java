package com.mopat.patrick.voicerecorderhd;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

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
    private Menu menu;
    private boolean allSelected = false, selectionMode = false;
    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_recording);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        init();
        displayMyRecordings();
        initListeners();
        android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setDisplayShowTitleEnabled(false);
        ab.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_my_recordings, menu);
        this.menu = menu;

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
                i.putExtra("filename", filename).putExtra("filepath", filePath).putExtra("samplerate", getSampleRate(filePath));
                startActivity(i);
            }
        });
        myRecordingsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, long id) {
                TextView listitem = (TextView) view.findViewById(R.id.filename);
                final String filename = (String) listitem.getText();
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(MyRecordingsActivity.this);
                builderSingle.setTitle("Actions");

                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                        MyRecordingsActivity.this,
                        R.layout.my_simple_list_item);
                arrayAdapter.add("Delete");
                arrayAdapter.add("Share...");
                arrayAdapter.add("Rename...");
                builderSingle.setNegativeButton(
                        "cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                builderSingle.setAdapter(
                        arrayAdapter,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String strName = arrayAdapter.getItem(which);
                                if (strName.equals("Delete")) {
                                    showDeleteDialog(filename);
                                } else if (strName.equals("Share...")) {
                                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                                    sharingIntent.setType("audio/*");
                                    Uri uri = Uri.parse(Absolutes.DIRECTORY + "/" + filename);
                                    sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
                                    startActivity(Intent.createChooser(sharingIntent, "Share via"));
                                } else if (strName.equals("Rename...")) {
                                    showRenameDialog(filename);
                                }
                            }
                        });
                builderSingle.show();

                return true;
            }
        });
    }

    private void showDeleteDialog(String filename) {
        String filePath = Absolutes.DIRECTORY + "/" + filename;
        final File file = new File(filePath);
        AlertDialog.Builder adb = new AlertDialog.Builder(MyRecordingsActivity.this);
        adb.setTitle("Delete?");
        adb.setMessage("Are you sure you want to delete " + filename);
        adb.setNegativeButton("Cancel", null);
        adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                file.delete();
                Toast.makeText(getApplicationContext(), "File deleted", Toast.LENGTH_LONG).show();
                myRecordings.clear();
                displayMyRecordings();
            }
        });
        adb.show();
    }

    private void showRenameDialog(final String filename) {
        AlertDialog.Builder adb = new AlertDialog.Builder(MyRecordingsActivity.this);
        adb.setTitle("Delete?");
        final EditText filenameEditText = new EditText(MyRecordingsActivity.this);
        filenameEditText.setHint("Filename");
        adb.setView(filenameEditText);
        filenameEditText.setText(filename);
        adb.setNegativeButton("Cancel", null);
        adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String newfilename = filenameEditText.getText().toString();
                renameFile(filename, newfilename);
                Toast.makeText(getApplicationContext(), "File renamed", Toast.LENGTH_LONG).show();
                myRecordings.clear();
                displayMyRecordings();
            }
        });
        adb.show();
    }

    public void renameFile(String filename, String newFilename) {
        File from = new File(Absolutes.DIRECTORY, filename);
        File to = new File(Absolutes.DIRECTORY, newFilename);
        Log.d("RENAME", String.valueOf(from.renameTo(to)));
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
            String duration = formatTime(file[i].length() * 1000 / (Integer.parseInt(samplerate) * 2));
            String filesize = String.valueOf(file[i].length());
            Log.d("Files", "FileName:" + file[i].getName());
            myRecordings.add(new MyRecordingsListitem(filename, samplerate, filesize, duration, false));
        }
        myRecordingsArrayAdapter = new MyRecordingsArrayAdapter(this, myRecordings);
        myRecordingsListView.setAdapter(myRecordingsArrayAdapter);
        myRecordingsArrayAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                return true;

            case R.id.action_multiselect:
                hideDefaultActionBarIcons();
                showCheckBoxes();
                selectionMode = true;
                return true;
            case R.id.action_delete:
                deleteFiles();
                return true;
            case R.id.select_unselect_all:
                toggleAllSelected();
                selectUnselectAll();
                return true;
   /*         case R.id.close_selection_mode:
                showDefaultActionBarIcons();
                allSelected = false;
                selectUnselectAll();
                hideCheckBoxes();
                return true;*/
            case android.R.id.home:
                backFunction();
                return true;
            case R.id.action_share:
                Intent sharingIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                sharingIntent.setType("audio/*");
                sharingIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, getSelectedItems());
                // sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, Uri.fromFile(new File(Absolutes.DIRECTORY +"/1.mp3")));
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private ArrayList<Uri> getSelectedItems() {
        ArrayList<Uri> fileUris = new ArrayList<>();
        for (int i = 0; i < myRecordingsListView.getChildCount(); i++) {
            View v = myRecordingsListView.getChildAt(i);
            CheckBox checkBox = (CheckBox) v.findViewById(R.id.listitem_checkbox);
            TextView filenameTextView = (TextView) v.findViewById(R.id.filename);
            if (checkBox.isChecked()) {
                String filename = (String) filenameTextView.getText();
                String filePath = Absolutes.DIRECTORY + "/" + filename;
                Uri fileUri = Uri.parse(filePath);
                fileUris.add(fileUri);
            }
        }
        return fileUris;
    }

    private void deleteFiles() {
        AlertDialog.Builder adb = new AlertDialog.Builder(MyRecordingsActivity.this);
        adb.setTitle("Delete files");
        adb.setMessage("Are you sure you want to delete the selected files");
        adb.setNegativeButton("Cancel", null);
        adb.setPositiveButton("Yes", new AlertDialog.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                for (int i = 0; i < myRecordingsListView.getChildCount(); i++) {
                    View v = myRecordingsListView.getChildAt(i);
                    CheckBox checkBox = (CheckBox) v.findViewById(R.id.listitem_checkbox);
                    TextView filenameTextView = (TextView) v.findViewById(R.id.filename);
                    if (checkBox.isChecked()) {
                        String filename = (String) filenameTextView.getText();
                        String filePath = Absolutes.DIRECTORY + "/" + filename;
                        final File file = new File(filePath);
                        file.delete();
                    }
                }
                myRecordings.clear();
                displayMyRecordings();
            }
        });
        adb.show();
    }

    private void hideDefaultActionBarIcons() {
        menu.setGroupVisible(R.id.delete_select_share_group, true);
        menu.setGroupVisible(R.id.main_menu_group, false);
    }

    private void showDefaultActionBarIcons() {
        menu.setGroupVisible(R.id.delete_select_share_group, false);
        menu.setGroupVisible(R.id.main_menu_group, true);
    }

    private void selectUnselectAll() {
        for (int i = 0; i < myRecordingsListView.getChildCount(); i++) {
            View v = myRecordingsListView.getChildAt(i);
            CheckBox checkBox = (CheckBox) v.findViewById(R.id.listitem_checkbox);
            if (allSelected)
                checkBox.setChecked(true);
            else
                checkBox.setChecked(false);
        }
    }

    @Override
    public void onBackPressed() {
        // moveTaskToBack(true);
        backFunction();

    }

    private void backFunction() {
        if (selectionMode == true) {
            showDefaultActionBarIcons();
            allSelected = false;
            selectUnselectAll();
            hideCheckBoxes();
            selectionMode = false;
        } else super.onBackPressed();
    }

    private void hideCheckBoxes() {
        for (int i = 0; i < myRecordingsListView.getChildCount(); i++) {
            View v = myRecordingsListView.getChildAt(i);
            CheckBox checkBox = (CheckBox) v.findViewById(R.id.listitem_checkbox);
            checkBox.setVisibility(View.INVISIBLE);
        }
    }

    private void showCheckBoxes() {
        for (int i = 0; i < myRecordingsListView.getChildCount(); i++) {
            View v = myRecordingsListView.getChildAt(i);
            CheckBox checkBox = (CheckBox) v.findViewById(R.id.listitem_checkbox);
            checkBox.setVisibility(View.VISIBLE);
        }
    }

    private void toggleAllSelected() {
        if (allSelected)
            allSelected = false;
        else allSelected = true;
    }
}

