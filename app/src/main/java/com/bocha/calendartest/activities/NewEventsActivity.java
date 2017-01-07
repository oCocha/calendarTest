package com.bocha.calendartest.activities;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.bocha.calendartest.LoginActivity;
import com.bocha.calendartest.MainActivity;
import com.bocha.calendartest.R;
import com.bocha.calendartest.adapter.eventAdapter;
import com.bocha.calendartest.data.Event;
import com.bocha.calendartest.listener.SwipeListener;
import com.bocha.calendartest.utility.EventUtility;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class NewEventsActivity extends AppCompatActivity {

    private static final String TAG = "New Events";
    public static final String PREFS_NAME = "LoginPrefs";

    private ListView myEventListView;
    private eventAdapter myAdapter;
    private ArrayList<ArrayList> eventList;     //Placeholder not needed yet

    private AlertDialog permRequestDialog;

    private SharedPreferences userData;

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

        readEvents();
        setupNewEventsData();
        setupNewEventsList();
        //setupNewEventsClickListener();
        setupNewEventsTouchListener();
    }

    /**Setup a swipeListener on the newEventsList */
    private void setupNewEventsTouchListener() {

        final SwipeListener swipeListener = new SwipeListener();
        myEventListView.setOnTouchListener(swipeListener);

        myEventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                if (swipeListener.swipeDetected()) {
                    if (swipeListener.getAction() == SwipeListener.Action.LR) {
                        //Log.v(TAG, "SWIPE");
                        onEventDeclined(position);
                    } else {
                        Log.v(TAG, "NO SWIPE");
                    }
                }
            }
        });


    }

    /**Setup an click listener for the listview elements*/
    private void setupNewEventsClickListener() {
        final Context context = this;

        myEventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Event event = (Event)myEventListView.getItemAtPosition(position);
                /*readEvents();
                EventUtility.addEvent(NewEventsActivity.this, event);*/
                Intent detailIntent = new Intent(context, DetailEventActivity.class);
                detailIntent.putExtra("eventTitle", event.getEventName());
                detailIntent.putExtra("eventStart", event.getEventStartDate().getTime());
                detailIntent.putExtra("eventEnd", event.getEventEndDate().getTime());
                detailIntent.putExtra("eventDesc", event.getEventDescription());
                startActivity(detailIntent);
                Log.v(TAG, "Clicked: " + event.getEventName());
            }
        });
    }

    public void onEventAccepted(int position){
        final Context context = this;
        Event event = (Event)myEventListView.getItemAtPosition(position);

        Intent detailIntent = new Intent(context, DetailEventActivity.class);
        detailIntent.putExtra("eventTitle", event.getEventName());
        detailIntent.putExtra("eventStart", event.getEventStartDate().getTime());
        detailIntent.putExtra("eventEnd", event.getEventEndDate().getTime());
        detailIntent.putExtra("eventDesc", event.getEventDescription());
        startActivity(detailIntent);
        Log.v(TAG, "Event added "+position);
    }

    @Override
    public void onResume(){
        super.onResume();
        readEvents();
    }

    public void onEventDeclined(int position){
        Log.v(TAG, "Event deleted "+position);
    }

    private void setupNewEventsData() {
        /**Setup the test start date*/
        long tempStartDate1 = 1482561038000L;
        //ALTeventStartDate.add(tempStartDate1);
        long tempStartDate2 = 1482820238000L;
        //ALTeventStartDate.add(tempStartDate2);
        long tempStartDate3 = 1482215439000L;
        //ALTeventStartDate.add(tempStartDate3);
        /**Setup the test end date*/
        long tempEndDate1 = 1482568238000L;
        //ALTeventEndDate.add(tempEndDate1);
        long tempEndDate2 = 1482842239000L;
        //ALTeventEndDate.add(tempEndDate2);
        long tempEndDate3 = 1483317449000L;
        //ALTeventEndDate.add(tempEndDate3);
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

    /**Placeholder
     * not needed yet*/
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

    /**Delay when adding a new event to the calendar
     * -> The list doesnt update correctly sometimes*/
    private void setupNewEventsList() {
        //readEvents();

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

    /**Placeholder
     * not needed yet*/
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
        if(grantResults.length > 0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v(TAG,"Permission: "+permissions[0]+ " was "+grantResults[0] + "PERM"+Manifest.permission.READ_CALENDAR);
            //resume tasks needing this permission
            if(permissions[0].equals(Manifest.permission.READ_CALENDAR)){
                Log.v(TAG, "Permission result read calendar success.");
                setupNewEventsList();
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
