package com.mopat.patrick.voicerecorderhd;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

class MyRecordingsArrayAdapter extends ArrayAdapter<MyRecordingsListitem> {
    ArrayList<MyRecordingsListitem> recordings;
    Context context;
    private static LayoutInflater inflater;

    public MyRecordingsArrayAdapter(Context context, int viewId, ArrayList<MyRecordingsListitem> recordings) {
        super(context, viewId, recordings);

        this.context = context;
        this.recordings = recordings;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount() {
        return recordings.size();
    }



    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (vi == null)
            vi = inflater.inflate(R.layout.my_recordings_listitem, null);
        TextView filename = (TextView) vi.findViewById(R.id.filename);
        TextView samplerate = (TextView)vi.findViewById(R.id.samplerate_value);
        TextView size = (TextView)vi.findViewById(R.id.size_value);
        TextView duration = (TextView)vi.findViewById(R.id.duration_value);

        filename.setText(recordings.get(position).getName());
        samplerate.setText(recordings.get(position).getSamplerate());
        size.setText(recordings.get(position).getFilesize());
        duration.setText(recordings.get(position).getDuration());
        return vi;
    }
}
