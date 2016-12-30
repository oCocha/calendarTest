package com.bocha.calendartest.activities;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.bocha.calendartest.MainActivity;
import com.bocha.calendartest.R;
import com.bocha.calendartest.adapter.eventAdapter;
import com.bocha.calendartest.data.Event;
import com.bocha.calendartest.utility.EventUtility;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class NewEventsActivity extends AppCompatActivity {

    private static final String TAG = "New Events";

    private ListView myEventListView;
    private eventAdapter myAdapter;
    private ArrayList<ArrayList> eventList;

    /**Test event data*/
    private ArrayList<int[]> eventStartDate = new ArrayList<>();
    private ArrayList<int[]> eventEndDate = new ArrayList<>();
    private ArrayList<String> eventName = new ArrayList<>();
    private ArrayList<String> eventDescription = new ArrayList<>();
    private ArrayList<Event> testEventList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_events);
        myEventListView = (ListView) findViewById(R.id.list_new_events);

        setupNewEventsData();
        setupNewEventsList();
        setupNewEventsClickListener();
    }

    /**Setup an click listener for the listview elements*/
    private void setupNewEventsClickListener() {
        myEventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Event event = (Event)myEventListView.getItemAtPosition(position);
                readEvents();
                EventUtility.addEvent(NewEventsActivity.this, event);
                Log.v(TAG, "Clicked: " + event.getEventName());
            }
        });
    }

    private void setupNewEventsData() {
        /**Setup the test start date*/
        int[] tempStartDate1 = {2016, 11, 24, 7, 30};
        eventStartDate.add(tempStartDate1);
        int[] tempStartDate2 = {2016, 11, 27, 7, 30};
        eventStartDate.add(tempStartDate2);
        int[] tempStartDate3 = {2016, 11, 20, 7, 30};
        eventStartDate.add(tempStartDate3);
        /**Setup the test end date*/
        int[] tempEndDate1 = {2016, 11, 24, 9, 30};
        eventEndDate.add(tempEndDate1);
        int[] tempEndDate2 = {2016, 11, 27, 11, 30};
        eventEndDate.add(tempEndDate2);
        int[] tempEndDate3 = {2016, 11, 20, 15, 30};
        eventEndDate.add(tempEndDate3);
        /**Setup the test names*/
        eventName.add("Test event 1");
        eventName.add("Test event 2");
        eventName.add("Test event 3");
        /**Setup the test descriptions*/
        eventDescription.add("This is the description for test event 1");
        eventDescription.add("This is the description for test event 2");
        eventDescription.add("This is the description for test event 3");

        /**Setup the test data events*/
        testEventList.add(new Event(tempStartDate1, tempEndDate1, eventName.get(0), eventDescription.get(0)));
        testEventList.add(new Event(tempStartDate2, tempEndDate2, eventName.get(1), eventDescription.get(1)));
        testEventList.add(new Event(tempStartDate3, tempEndDate3, eventName.get(2), eventDescription.get(2)));
    }

    private void readEvents() {
        if(eventList != null){
            eventList.clear();
        }
        if (isCalendarReadPermissionGranted()) {
            Log.v(TAG, "Calendar read Permission granted");
        }
        eventList = EventUtility.readCalendarEvent(NewEventsActivity.this);
    }

    /**Delay when adding a new event to the calendar
     * -> The list doesnt update correctly sometimes*/
    private void setupNewEventsList() {
        readEvents();

        if (myAdapter == null) {
            myAdapter = new eventAdapter(this,
                    R.layout.item_new_event,
                    R.id.new_event_title,
                    R.id.new_event_description,
                    testEventList);
            myEventListView.setAdapter(myAdapter);
            Log.v(TAG, "New adapter");
        } else {
            myAdapter.clear();
            myAdapter.addAll(eventName);
            myAdapter.notifyDataSetChanged();
            Log.v(TAG, "NotifyDataSetChanged");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_event_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.activity_events_lists:
                Intent mainIntent = new Intent(this, MainActivity.class);
                startActivity(mainIntent);
                return true;
            case R.id.activity_events_calendar:
                Intent calIntent = new Intent(this, CalendarActivity.class);
                startActivity(calIntent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**Add an calendar event, event data is entered automatically*//*
    private void addEventAutomatically(Event event) {
        long calID = 1;
        long startMillis = 0;
        long endMillis = 0;
        Calendar beginTime = null;
        beginTime = Calendar.getInstance();
        beginTime.set(event.getEventStartDate()[0], event.getEventStartDate()[1], event.getEventStartDate()[2], event.getEventStartDate()[3], event.getEventStartDate()[4]);
        startMillis = beginTime.getTimeInMillis();
        Calendar endTime = Calendar.getInstance();
        endTime.set(event.getEventEndDate()[0], event.getEventEndDate()[1], event.getEventEndDate()[2], event.getEventEndDate()[3], event.getEventEndDate()[4]);
        endMillis = endTime.getTimeInMillis();

        ArrayList<ArrayList> collidingEvents = EventUtility.checkEventCollision(startMillis, endMillis);

        /**Set the event data*//*
        final ContentResolver cr = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, startMillis);
        values.put(CalendarContract.Events.DTEND, endMillis);
        values.put(CalendarContract.Events.TITLE, event.getEventName());
        values.put(CalendarContract.Events.DESCRIPTION, event.getEventDescription());
        values.put(CalendarContract.Events.CALENDAR_ID, calID);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, "Europe/Berlin");

        /**Save the necessary event data*//*
        final ContentValues eventValues = values;
        final Context context = this;

        if (isCalendarWritePermissionGranted()) {
            Log.v(TAG, "Permission is granted");
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.v(TAG, "Permission not granted");
            return;
        }

        Log.v(TAG, "Size: " + collidingEvents.size());
        if (collidingEvents.size() != 0) {
            String collisionNames = new String();
            for (int i = 0, j = collidingEvents.size(); i < j; i++) {
                collisionNames = collisionNames + " " + collidingEvents.get(i).get(0);
            }

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("New event collides with: " + collisionNames)
                    .setMessage("Create new event?")
                    .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                                return;
                            }
                            Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, eventValues);
                        }
                    })
                    .setNegativeButton("Decline", null)
                    .create();
            dialog.show();
        }else{
            Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
            Log.v(TAG,"Event added");
            // get the event ID that is the last element in the Uri
            long eventID = Long.parseLong(uri.getLastPathSegment());
            //
            // ... do something with event ID
            //
            //
        }
    }*/

    /**Check whether the new event collides with old events
     * Ask the user if he wants to create the new event if collisions occur*//*
    private ArrayList<ArrayList> checkEventCollision(long startMillis, long endMillis) {
        ArrayList<ArrayList> collidingEvents = new ArrayList<>();
        readEvents();
        Log.v(TAG, "Eventlist size: " + eventList.size());
        for(int i = 0, l = eventList.size(); i < l; i++){
            if((Long.parseLong((String)eventList.get(i).get(1)) > startMillis && Long.parseLong((String)eventList.get(i).get(1)) < endMillis) ||
                    (Long.parseLong((String)eventList.get(i).get(2)) > startMillis && Long.parseLong((String)eventList.get(i).get(2)) < endMillis)){
                collidingEvents.add(eventList.get(i));
                Log.v(TAG, "Event collision: " + eventList.get(i).get(0));
            }
        }
        return collidingEvents;
    }*/

    /**Check whether the app can write in the calendar device app
     * Request the necessary permisison if not*//*
    public  boolean isCalendarWritePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_CALENDAR)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CALENDAR}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }*/

    /**Check whether the app can write in the calendar device app
     * Request the necessary permisison if not*/
    public  boolean isCalendarReadPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_CALENDAR)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALENDAR}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }

    /*
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
        }
    }*/
}
