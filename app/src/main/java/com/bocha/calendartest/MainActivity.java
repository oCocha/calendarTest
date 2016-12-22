package com.bocha.calendartest;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.bocha.calendartest.Utility.EventUtility;

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

        readEvents();
        updateUI();
    }

    private void readEvents() {
        eventNameList = EventUtility.readCalendarEvent(this);
        /*
        Log.v(TAG, "Events fetched");
        for(int i = 0, j = eventList.size(); i < j; i++){
            Log.v(TAG, "Event" + i + " Name: " + eventList.get(i).get(0));
            Log.v(TAG, "Event" + i + " Startdate: " + eventList.get(i).get(1));
            Log.v(TAG, "Event" + i + " Enddate: " + eventList.get(i).get(2));
            Log.v(TAG, "Event" + i + " Description: " + eventList.get(i).get(3));
        }*/
    }

    private void updateUI() {
        readEvents();

        ArrayList<String> taskList = new ArrayList<>();

        taskList = eventNameList;

        if (myAdapter == null) {
            myAdapter = new ArrayAdapter<>(this,
                    R.layout.item_event,
                    R.id.event_title,
                    taskList);
            myEventListView.setAdapter(myAdapter);
            Log.v(TAG, "New adapter");
        } else {
            myAdapter.notifyDataSetChanged();
            Log.v(TAG, "NotifyDataSetChanged");
        }
    }

    public void deleteEvent(View view) {
        View parent = (View) view.getParent();
        TextView taskTextView = (TextView) parent.findViewById(R.id.event_title);
        String task = String.valueOf(taskTextView.getText());

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
                                String event = String.valueOf(eventEditText.getText());

                                addEventAutomatically(event);
                                //addEventManually(event);

                                updateUI();
                            }
                        })
                        .setNegativeButton("Decline", null)
                        .create();
                dialog.show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addEventManually(String event) {
        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setType("vnd.android.cursor.item/event");
        GregorianCalendar calDate = new GregorianCalendar(2016, 11, 27);
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                calDate.getTimeInMillis());
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
                calDate.getTimeInMillis());
        intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);
        intent.putExtra(CalendarContract.Events.TITLE, event);
        intent.putExtra(CalendarContract.Events.DESCRIPTION, "This is a sample description");
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, "My Guest House");
        intent.putExtra(CalendarContract.Events.RRULE, false);
        startActivity(intent);
    }

    private void addEventAutomatically(String event) {
        long calID = 1;
        long startMillis = 0;
        long endMillis = 0;
        Calendar beginTime = null;
        beginTime = Calendar.getInstance();
        beginTime.set(2016, 11, 24, 7, 30);
        startMillis = beginTime.getTimeInMillis();
        Calendar endTime = Calendar.getInstance();
        endTime.set(2016, 11, 24, 8, 45);
        endMillis = endTime.getTimeInMillis();

        ContentResolver cr = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, startMillis);
        values.put(CalendarContract.Events.DTEND, endMillis);
        values.put(CalendarContract.Events.TITLE, event);
        values.put(CalendarContract.Events.DESCRIPTION, "Event for testing purposes");
        values.put(CalendarContract.Events.CALENDAR_ID, calID);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, "Europe/Berlin");

        if (isCalendarPermissionGranted()) {
            Log.v(TAG,"Permission is granted");
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.v(TAG,"Permission not granted");
            return;
        }
        Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
        Log.v(TAG,"Event added");

        // get the event ID that is the last element in the Uri
            long eventID = Long.parseLong(uri.getLastPathSegment());
            //
            // ... do something with event ID
            //
            //

    }

    public  boolean isCalendarPermissionGranted() {
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
        }
    }
}
