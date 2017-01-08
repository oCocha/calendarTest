package com.bocha.calendartest;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import java.util.Calendar;

import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
    public static final String PREFS_NAME = "LoginPrefs";

    private ListView myEventListView;
    private ArrayAdapter<String> myAdapter;
    private ArrayList<ArrayList> eventList;
    private ArrayList<String> eventNameList;

    private AlertDialog permRequestDialog;
    private Event standbyEvent;

    private SharedPreferences userData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myEventListView = (ListView) findViewById(R.id.list_event);

        //readEvents();
        updateUI();
    }

    private void readEvents(){
        if(permissionGrantedReadCal()){
            //Clear the eventList if necessary
            if(eventList != null){
                eventList.clear();
            }
            //Update the eventList
            eventList = EventUtility.readCalendarEvent(this);
        }else{
            Log.v(TAG, "Read events permission not granted");
        }
    }

    /** Delay when adding a new event to the calendar
     * -> The list doesnt update correctly sometimes*/
    private void updateUI() {
        readEvents();

        ArrayList<String> taskList = new ArrayList<>();

        if(eventList != null) {
            for (int i = 0, l = eventList.size(); i < l; i++) {
                if(Integer.parseInt((String)eventList.get(i).get(6)) != 0) {
                    Log.v(TAG, "Event: " + eventList.get(i).get(0) + "  deleted: " + eventList.get(i).get(6));
                }
                taskList.add((String) eventList.get(i).get(0));
            }
            Log.v(TAG, "TaskList: "+taskList);
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

                                //updateUI();
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
            case R.id.action_log_out:
                logout();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**Replace the saved login data with the default values
     * and start the login activity*/
    private void logout() {
        userData = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = userData.edit();
        editor.putString("userMail", "Default");
        editor.putString("userPass", "Default");

        // Commit the edits
        editor.commit();

        Intent logoutIntent = new Intent(this, LoginActivity.class);
        startActivity(logoutIntent);
    }

    /**Add the event using the EventUtility class*/
    private void addEvent(String eventTitle) {
        long startDate = 1482561038000L;
        long endDate = 1482561038000L;

        Event event = new Event(startDate, endDate, eventTitle, "Descrption for " + eventTitle);

        standbyEvent = event;

        EventUtility.addEvent(MainActivity.this, event);

        //updateUI();
    }

    /**Check whether the app can read in the calendar device app
     * Request the necessary permisison if not*/
    private boolean permissionGrantedReadCal(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CALENDAR)) {

                final Activity activity = this;

                permRequestDialog = new AlertDialog.Builder(this)
                        .setTitle("Calendar read permission needed")
                        .setMessage("The app needs the calendar read permission to get the events from the default calendar app.")
                        .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                //Request the permission if the user accepts
                                ActivityCompat.requestPermissions(activity,
                                        new String[]{Manifest.permission.READ_CALENDAR},
                                        1);
                            }
                        })
                        .setNegativeButton("Decline", null)
                        .create();
                permRequestDialog.show();

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CALENDAR},
                        1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
            return false;
        }else{
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v(TAG,"Permission: "+permissions[0]+ " was "+grantResults[0] + "PERM"+Manifest.permission.READ_CALENDAR);
            //resume tasks needing this permission
            if(permissions[0].equals(Manifest.permission.READ_CALENDAR)){
                Log.v(TAG, "Permission result read calendar success.");
                updateUI();
            }else if(permissions[0].equals(Manifest.permission.WRITE_CALENDAR)){
                Log.v(TAG, "Permission result write calendar success.");
                addEvent(standbyEvent.getEventName());
                updateUI();
            }
        }
    }

    /**Destroy permission request dialogs if the used activity is destroyed
     * e.g flipping device*/
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (permRequestDialog != null) {
            permRequestDialog.dismiss();
            permRequestDialog = null;
        }
    }

}
