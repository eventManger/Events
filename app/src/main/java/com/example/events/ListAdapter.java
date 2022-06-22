package com.example.events;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ListAdapter extends ArrayAdapter<Event> {

    private int resourceLayout;
    private Context mContext;


    public ListAdapter(Context context, int resource, List<Event> items) {
        super(context, resource, items);


        this.resourceLayout = resource;
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(mContext);
            v = vi.inflate(resourceLayout, null);
        }

        Event p = getItem(position);

        if (p != null) {
            TextView tt1 = (TextView) v.findViewById(R.id.EventId1);
            TextView tt2 = (TextView) v.findViewById(R.id.EventAddDate1);
            TextView tt3 = (TextView) v.findViewById(R.id.EventName1);
            TextView tt4 = (TextView) v.findViewById(R.id.AddressLine);
            
            ImageView imageView = (ImageView) v.findViewById(R.id.imageDetails1);
            if(p.getImage() == null) {

            }
            else{
                imageView.
                        setImageBitmap(
                                p.
                                        getImage());
            }

            boolean cz = p.getImage() == null;


            if (tt1 != null) {
                tt1.setText(p.getId());
            }

            if (tt2 != null) {
                tt2.setText(p.getAddDateTime().toLocalDate().toString());
            }

            if (tt3 != null) {
                tt3.setText(p.getName());
            }

            if (tt4 != null) {
                tt4.setText(p.getAddress());
            }
        }

        return v;
    }

}