package com.eurotek.beaconapp;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
/**
 * Created by El Gormo on 02/10/2016.
 */

public class MyAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final ArrayList<String> itemsArrayList;

    public MyAdapter(Context context, ArrayList<String> itemsArrayList) {
        super(context, R.layout.my_list_item, itemsArrayList);

        this.context = context;
        this.itemsArrayList = itemsArrayList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // 1. Create inflater
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // 2. Get rowView from inflater
        View rowView = inflater.inflate(R.layout.my_list_item, parent, false);

        // 3. Get the two text view from the rowView
        TextView labelView = (TextView) rowView.findViewById(R.id.label);

        // 4. Set the text for textView
        labelView.setText(itemsArrayList.get(position));

        // 5. return rowView
        return rowView;
    }
}
