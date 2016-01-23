package com.mopat.patrick.voicerecorderhd;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

class MyRecordingsArrayAdapter extends ArrayAdapter<MyRecordingsListitem> {
    ArrayList<MyRecordingsListitem> myRecordingsListitems;
    Context context;
    private static LayoutInflater inflater;

    public MyRecordingsArrayAdapter(Context context, int viewId, ArrayList<MyRecordingsListitem> myRecordingsListitems) {
        super(context, viewId, myRecordingsListitems);

        this.context = context;
        this.myRecordingsListitems = myRecordingsListitems;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount() {
        return myRecordingsListitems.size();
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (vi == null)
            vi = inflater.inflate(R.layout.my_recordings_listitem, null);
        TextView filename = (TextView) vi.findViewById(R.id.filename);
        TextView samplerate = (TextView) vi.findViewById(R.id.samplerate_value);
        TextView size = (TextView) vi.findViewById(R.id.size_value);
        TextView duration = (TextView) vi.findViewById(R.id.duration_value);
        TextView modified = (TextView) vi.findViewById(R.id.modified_data);
        final CheckBox checkBox = (CheckBox) vi.findViewById(R.id.listitem_checkbox);

        filename.setText(myRecordingsListitems.get(position).getName());
        samplerate.setText(myRecordingsListitems.get(position).getSamplerate() + "Hz");
        size.setText(myRecordingsListitems.get(position).getFilesize());
        duration.setText(myRecordingsListitems.get(position).getDuration());
        modified.setText(myRecordingsListitems.get(position).getModifiedDate());
        if (myRecordingsListitems.get(position).isCheckboxVisible())
            checkBox.setVisibility(View.VISIBLE);
        else checkBox.setVisibility(View.INVISIBLE);

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkBox.isChecked()) {
                    myRecordingsListitems.get(position).setChecked(true);
                } else myRecordingsListitems.get(position).setChecked(false);

            }
        });
        checkBox.setChecked(myRecordingsListitems.get(position).isChecked());

        return vi;
    }
}
