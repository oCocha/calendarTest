package com.bocha.calendartest;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;

import java.util.Calendar;

import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bocha.calendartest.activities.CalendarActivity;
import com.bocha.calendartest.activities.NewEventsActivity;
import com.bocha.calendartest.data.Event;
import com.bocha.calendartest.utility.EventUtility;

import java.util.ArrayList;
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main Menu";

    private ListView myEventListView;
    private ArrayAdapter<String> myAdapter;
    private ArrayList<ArrayList> eventList;
    private ArrayList<String> eventNameList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myEventListView = (ListView) findViewById(R.id.list_event);
        Log.v(TAG, "LIVESTVIEW: "+myEventListView);

        readEvents();
        updateUI();
    }

    private void readEvents() {
        if(eventList != null){
            eventList.clear();
        }
        if (isCalendarReadPermissionGranted()) {
            Log.v(TAG, "Calendar read Permission granted if");
            eventList = EventUtility.readCalendarEvent(this);
        }else{
            requestReadCalendarPermission();
            eventList = EventUtility.readCalendarEvent(this);
        }
        Log.v(TAG, "eventlist size: "+eventList.size());
    }

    private void requestReadCalendarPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALENDAR}, 1);
    }

    /**Delay when adding a new event to the calendar
     * -> The list doesnt update correctly sometimes*/
    private void updateUI() {
        readEvents();

        ArrayList<String> taskList = new ArrayList<>();

        for (int i = 0, l = eventList.size(); i < l; i++) {
            taskList.add((String) eventList.get(i).get(0));
        }
        if (myAdapter == null) {
            myAdapter = new ArrayAdapter<>(this,
                    R.layout.item_event,
                    R.id.event_title,
                    taskList);
            myEventListView.setAdapter(myAdapter);
            Log.v(TAG, "New adapter");
        } else {
            myAdapter.clear();
            myAdapter.addAll(taskList);
            myAdapter.notifyDataSetChanged();
            Log.v(TAG, "NotifyDataSetChanged");
        }
        setupOnClickListener();
    }

    /**Setup an click listener for the listview elements*/
    private void setupOnClickListener() {
        final Context context = this;
        myEventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                final String eventTitle = myEventListView.getItemAtPosition(position).toString();
                Log.v(TAG, "Update: " + eventTitle);
                final EditText eventEditText = new EditText(context);
                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setTitle("Update event title")
                        .setMessage("What should the new event title be?")
                        .setView(eventEditText)
                        .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String newTitle = String.valueOf(eventEditText.getText());
                                Integer eventId = EventUtility.getEventIdByTitle(context, eventTitle);
                                Log.v(TAG, "Change title to: "+newTitle);
                                EventUtility.updateEventTitle(context, newTitle, eventId);

                                updateUI();
                            }
                        })
                        .setNegativeButton("Decline", null)
                        .create();
                dialog.show();
            }
        });
    }

    /**Delete the according calendar event when the delete button is clicked
     * To delete the event the EventUtility class is used*/
    public void deleteEvent(View view) {
        View parent = (View) view.getParent();
        TextView taskTextView = (TextView) parent.findViewById(R.id.event_title);
        String eventTitle = String.valueOf(taskTextView.getText());

        final Integer eventId = EventUtility.getEventIdByTitle(this, eventTitle);
        Log.v(TAG, ""+eventId);

        final Context context = this;

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Delete event")
                .setMessage("Delete the event '" + eventTitle + "' with event id: " + eventId + "?")
                .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        EventUtility.deleteEventById(context, eventId);

                        updateUI();
                    }
                })
                .setNegativeButton("Decline", null)
                .create();
        dialog.show();

        updateUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_event:
                final EditText eventEditText = new EditText(this);
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle("New event")
                        .setMessage("Accept the event?")
                        .setView(eventEditText)
                        .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                addEvent(String.valueOf(eventEditText.getText()));

                                updateUI();
                            }
                        })
                        .setNegativeButton("Decline", null)
                        .create();
                dialog.show();
                return true;
            case R.id.activity_new_events:
                Intent listIntent = new Intent(this, NewEventsActivity.class);
                startActivity(listIntent);
                return true;
            case R.id.activity_events_calendar:
                Intent calIntent = new Intent(this, CalendarActivity.class);
                startActivity(calIntent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**Add the event using the EventUtility class*/
    private void addEvent(String eventTitle) {
        int[] startDate = {2016, 11, 24, 7, 30};
        int[] endDate = {2016, 11, 24, 14, 30};

        Event event = new Event(startDate, endDate, eventTitle, "Descrption for " + eventTitle);

        EventUtility.addEvent(MainActivity.this, event);

        updateUI();
    }

    /**Check whether the new event collides with old events
     * Ask the user if he wants to create the new event if collisions occur*/
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
    }

    /**Check whether the app can write in the calendar device app
     * Request the necessary permisison if not*/
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
    }

    /**Check whether the app can write in the calendar device app
     * Request the necessary permisison if not*/
    public  boolean isCalendarReadPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_CALENDAR)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALENDAR}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
        }
    }
}
