package com.mopat.patrick.voicerecorderhd;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Path;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MyRecordingsActivity extends AppCompatActivity {
    private ListView myRecordingsListView;
    private MyRecordingsArrayAdapter myRecordingsArrayAdapter;
    private ArrayList<MyRecordingsListitem> myRecordings = new ArrayList<>();
    private Menu menu;
    private boolean allSelected = false, selectionMode = false;
    private InterstitialAd mInterstitialAd;

    /*
    Sort: 1 -> new  ->old
    Sort: 2 -> old  ->new
    Sort: 3 -> alphabet desc
    Sort: 4 -> alphabet asc
    Sort: 1 -> new  ->old
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_recording);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setTitle(R.string.title_activity_my_recording);
        ab.setDisplayHomeAsUpEnabled(true);
        init();
        displayMyRecordings();
        initListeners();

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(Absolutes.AD_UNIT_ID);
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        if (mInterstitialAd.isLoaded() && !Absolutes.IS_PRO) {
            mInterstitialAd.show();
        } else {
            Log.d("AD", "The interstitial wasn't loaded yet.");
        }
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

                String samplerate = getFileSamplerate(filePath);
                Intent i = new Intent(MyRecordingsActivity.this, MainActivity.class);
                i.putExtra("filename", filename).putExtra("filepath", filePath).putExtra("samplerate", samplerate);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
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
                                    if (mInterstitialAd.isLoaded() && !Absolutes.IS_PRO) {
                                        mInterstitialAd.show();
                                    } else {
                                        Log.d("AD", "The interstitial wasn't loaded yet.");
                                    }
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

    private String getFileSamplerate(String filepath) {
        FileInputStream in = null;
        WaveHeader waveHeader = new WaveHeader();
        try {
            in = new FileInputStream(filepath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            waveHeader.read(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return String.valueOf(waveHeader.getSampleRate());
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
                if (mInterstitialAd.isLoaded() && !Absolutes.IS_PRO) {
                    mInterstitialAd.show();
                } else {
                    Log.d("AD", "The interstitial wasn't loaded yet.");
                }
            }
        });
        adb.show();
    }

    private void showRenameDialog(final String filename) {
        AlertDialog.Builder adb = new AlertDialog.Builder(MyRecordingsActivity.this);
        adb.setTitle("Rename");
        final EditText filenameEditText = new EditText(MyRecordingsActivity.this);
        filenameEditText.setHint("Filename");
        adb.setView(filenameEditText);

        final String cuttedFilename = filename.substring(0, filename.length() - 4);
        filenameEditText.setText(cuttedFilename);
        adb.setNegativeButton("Cancel", null);
        adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String newfilename = filenameEditText.getText().toString();
                if (newfilename.indexOf(".") > 0) {
                    newfilename = newfilename.replace(".", "_");
                }
                newfilename = newfilename.replaceAll("\\s", "");
                renameFile(cuttedFilename, newfilename);
                myRecordings.clear();
                displayMyRecordings();
                if (mInterstitialAd.isLoaded() && !Absolutes.IS_PRO) {
                    mInterstitialAd.show();
                } else {
                    Log.d("AD", "The interstitial wasn't loaded yet.");
                }
            }
        });
        adb.show();
    }

    public void renameFile(String filename, String newFilename) {
        File from = new File(Absolutes.DIRECTORY, filename + Config.filetype);
        File to = new File(Absolutes.DIRECTORY, newFilename + Config.filetype);
        if (from.renameTo(to))
            Toast.makeText(getApplicationContext(), "File renamed", Toast.LENGTH_LONG).show();
        else
            Toast.makeText(getApplicationContext(), "File renaming not possible", Toast.LENGTH_LONG).show();
    }


    private void displayMyRecordings() {
        String path = Absolutes.DIRECTORY.toString();
        File f = new File(path);
        File file[] = f.listFiles();

        if (file != null)
            for (int i = 0; i < file.length; i++) {
                String filename = file[i].getName();
                String fileEnding = filename.substring(filename.length() - 4, filename.length());
                // Log.d("fileending", fileEnding);

                if (fileEnding.equals(Config.filetype)) {
                    WaveHeader waveHeader = new WaveHeader();
                    FileInputStream in = null;
                    try {
                        in = new FileInputStream(Absolutes.DIRECTORY + "/" + filename);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    try {
                        waveHeader.read(in);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String samplerate = String.valueOf(waveHeader.getSampleRate());
                    if (waveHeader.getSampleRate() == 0) {
                        samplerate = "44100";
                    }
                    String duration = TimeFormat.formatTime(file[i].length() * 1000 / (Integer.parseInt(samplerate) * 2));
                    Log.d("MODIFIED", String.valueOf(new Date(file[i].lastModified())));
                    String filesize = String.valueOf(FileSizeFormat.getFormattedFileSizeForList((int) file[i].length()));
                    String modifiedDate = new SimpleDateFormat("dd.MM.yyyy, HH:mm:ss").format(
                            new Date(file[i].lastModified())
                    );

                    Log.d("Files", "FileName:" + file[i].getName());
                    myRecordings.add(new MyRecordingsListitem(filename, samplerate, filesize, duration, modifiedDate, false));

                }
                Collections.sort(myRecordings, new Comparator<MyRecordingsListitem>() {
                    public int compare(MyRecordingsListitem m1, MyRecordingsListitem m2) {
                        switch (Config.sorting) {
                            case 1:
                                return m2.getModifiedDate().compareTo(m1.getModifiedDate());
                            case 2:
                                return m1.getModifiedDate().compareTo(m2.getModifiedDate());
                            case 3:
                                return m1.getName().compareTo(m2.getName());
                            case 4:
                                return m2.getName().compareTo(m1.getName());
                            case 5:
                                return m1.getFilesize().compareTo(m2.getFilesize());
                            default:
                                return m2.getModifiedDate().compareTo(m1.getModifiedDate());
                        }

                    }
                });

            }
        myRecordingsArrayAdapter = new MyRecordingsArrayAdapter(this, R.layout.my_simple_list_item, myRecordings);

        myRecordingsListView.setAdapter(myRecordingsArrayAdapter);
        myRecordingsArrayAdapter.notifyDataSetChanged();
        Log.d("myrecordinglength", String.valueOf(myRecordingsArrayAdapter.getCount()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
/*            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                return true;*/

            case R.id.action_multiselect:
                if (myRecordingsArrayAdapter != null) {
                    if (myRecordingsArrayAdapter.getCount() > 0) {
                        hideDefaultActionBarIcons();
                        showCheckBoxes();
                        selectionMode = true;
                    }
                }
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
                if (getSelectedItems().size() > 0) {
                    Intent sharingIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                    sharingIntent.setType("audio/*");
                    sharingIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, getSelectedItems());
                    startActivity(Intent.createChooser(sharingIntent, "Share via"));
                } else {
                    Toast.makeText(getApplicationContext(), "You need to select at least one recording", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.sort_newest:
                Config.sorting = 1;
                myRecordings.clear();
                displayMyRecordings();
                return true;
            case R.id.sort_oldest:
                Config.sorting = 2;
                myRecordings.clear();
                displayMyRecordings();
                return true;
            case R.id.alph_down:
                Config.sorting = 3;
                myRecordings.clear();
                displayMyRecordings();
                return true;
            case R.id.alph_up:
                Config.sorting = 4;
                myRecordings.clear();
                displayMyRecordings();
                return true;
      /*      case R.id.size:
                Config.sorting = 5;
                myRecordings.clear();
                displayMyRecordings();
                return true;*/
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private ArrayList<Uri> getSelectedItems() {
        ArrayList<Uri> fileUris = new ArrayList<>();
        for (int i = 0; i < myRecordingsArrayAdapter.getCount(); i++) {
            if (myRecordingsArrayAdapter.getItem(i).isChecked()) {
                String filename = myRecordingsArrayAdapter.getItem(i).getName();
                String filePath = Absolutes.DIRECTORY + "/" + filename;
                Uri fileUri = Uri.parse(filePath);
                fileUris.add(fileUri);
            }
        }
        return fileUris;
    }

    private void deleteFiles() {
        if (getSelectedItems().size() > 0) {
            AlertDialog.Builder adb = new AlertDialog.Builder(MyRecordingsActivity.this);
            adb.setTitle("Delete files");
            adb.setMessage("Are you sure you want to delete the selected files");
            adb.setNegativeButton("Cancel", null);
            adb.setPositiveButton("Yes", new AlertDialog.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    for (int i = 0; i < myRecordingsArrayAdapter.getCount(); i++) {
                        if (myRecordingsArrayAdapter.getItem(i).isChecked()) {
                            String filename = myRecordingsArrayAdapter.getItem(i).getName();
                            String filePath = Absolutes.DIRECTORY + "/" + filename;
                            final File file = new File(filePath);
                            file.delete();
                        }
                    }
                    backFunction();
                    myRecordings.clear();
                    displayMyRecordings();
                }
            });
            adb.show();
        } else {
            Toast.makeText(getApplicationContext(), "You need to select at least one recording", Toast.LENGTH_SHORT).show();
        }
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
        if (myRecordingsArrayAdapter != null) {
            for (int i = 0; i < myRecordingsArrayAdapter.getCount(); i++) {
                if (allSelected)
                    myRecordingsArrayAdapter.getItem(i).setChecked(true);
                else
                    myRecordingsArrayAdapter.getItem(i).setChecked(false);

            }
            myRecordingsArrayAdapter.notifyDataSetChanged();
            myRecordingsListView.setAdapter(myRecordingsArrayAdapter);
        }

    }

    @Override
    public void onBackPressed() {
        backFunction();
    }

    private void backFunction() {
        if (selectionMode == true) {
            showDefaultActionBarIcons();
            allSelected = false;
            selectUnselectAll();
            hideCheckBoxes();
            selectionMode = false;
        } else {
            super.onBackPressed();
            finish();
            overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
        }
    }

    private void hideCheckBoxes() {
        if (myRecordingsArrayAdapter != null)
            if (myRecordingsArrayAdapter.getCount() > 0) {
                for (int i = 0; i < myRecordingsArrayAdapter.getCount(); i++) {
                    myRecordingsArrayAdapter.getItem(i).setCheckboxVisible(false);
                }
                myRecordingsArrayAdapter.notifyDataSetChanged();
                myRecordingsListView.setAdapter(myRecordingsArrayAdapter);
            }
    }

    private void showCheckBoxes() {
        if (myRecordingsArrayAdapter != null) {
            if (myRecordingsArrayAdapter.getCount() > 0) {
                for (int i = 0; i < myRecordingsArrayAdapter.getCount(); i++) {
                    myRecordingsArrayAdapter.getItem(i).setCheckboxVisible(true);
                }
                myRecordingsArrayAdapter.notifyDataSetChanged();
                myRecordingsListView.setAdapter(myRecordingsArrayAdapter);
            }
        }
    }

    private void toggleAllSelected() {
        if (allSelected)
            allSelected = false;
        else allSelected = true;
    }
}

