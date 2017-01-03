package com.bocha.calendartest.adapter;

import android.content.ClipData;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bocha.calendartest.R;
import com.bocha.calendartest.data.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by bob on 26.12.16.
 */

public class eventAdapter extends ArrayAdapter {
    ArrayList<Event> events;

    SimpleDateFormat formatter;

    public eventAdapter(Context context, int eventId, int titleId, int descId, ArrayList<Event> events){
        super(context, eventId, events);
        this.events = events;

        formatter = new SimpleDateFormat("dd MMM yyyy hh : mm");
    }

    /*
	 * we are overriding the getView method here - this is what defines how each
	 * list item will look.
	 */
    public View getView(int position, View convertView, ViewGroup parent){

        // assign the view we are converting to a local variable
        View v = convertView;

        // first check to see if the view is null. if so, we have to inflate it.
        // to inflate it basically means to render, or show, the view.
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.item_new_event, null);
        }

		/*
		 * Recall that the variable position is sent in as an argument to this method.
		 * The variable simply refers to the position of the current object in the list. (The ArrayAdapter
		 * iterates through the list we sent it)
		 *
		 * Therefore, i refers to the current Item object.
		 */
        Event i = events.get(position);

        if (i != null) {

            // This is how you obtain a reference to the TextViews.
            // These TextViews are created in the XML files we defined.

            TextView tt = (TextView) v.findViewById(R.id.new_event_title);
            TextView td = (TextView) v.findViewById(R.id.new_event_description);
            TextView ts = (TextView) v.findViewById(R.id.new_event_start);
            TextView te = (TextView) v.findViewById(R.id.new_event_end);

            // check to see if each individual textview is null.
            // if not, assign some text!
            if (tt != null){
                tt.setText(i.getEventName());
            }
            if (td != null){
                td.setText(i.getEventDescription());
            }
            if (ts != null){
                ts.setText(dateToString(i.getEventStartDate()));
            }
            if (te != null){
                te.setText(" - " + dateToString(i.getEventEndDate()));
            }
        }

        // the view must be returned to our activity
        return v;

    }

    private String dateToString(Date date) {
        String result = "";
        result += formatter.format(date);

        return result;
    }

}
