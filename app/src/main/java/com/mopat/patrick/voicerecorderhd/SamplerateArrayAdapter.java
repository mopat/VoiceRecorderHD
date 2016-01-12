package com.mopat.patrick.voicerecorderhd;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

class SamplerateArrayAdapater extends BaseAdapter {
    SamplerateListitem[] samplerates;
    Context context;
    private static LayoutInflater inflater;

    public SamplerateArrayAdapater(Context context, SamplerateListitem[] samplerates) {
        this.context = context;
        this.samplerates = samplerates;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount() {
        return samplerates.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (vi == null)
            vi = inflater.inflate(R.layout.samplerate_list_item, null);
        TextView text = (TextView) vi.findViewById(R.id.samplerate_value);
        text.setText(samplerates[position].getName());
        return vi;
    }
}
