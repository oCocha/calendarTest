package com.bocha.calendartest.activities;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.bocha.calendartest.MainActivity;
import com.bocha.calendartest.R;
import com.bocha.calendartest.adapter.eventAdapter;
import com.bocha.calendartest.data.Event;
import com.bocha.calendartest.utility.EventUtility;

import java.util.ArrayList;

public class CalendarActivity extends AppCompatActivity {

    private static final String TAG = "New Events";

    private AlertDialog permRequestDialog;

    private ListView myEventCalendarView;
    private ArrayList<ArrayList> eventList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        readEvents();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.calendar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.activity_events_lists:
                Intent mainIntent = new Intent(this, MainActivity.class);
                startActivity(mainIntent);
                return true;
            case R.id.activity_new_events:
                Intent newIntent = new Intent(this, NewEventsActivity.class);
                startActivity(newIntent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
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

}
