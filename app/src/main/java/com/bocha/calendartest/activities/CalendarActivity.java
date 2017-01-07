package com.bocha.calendartest.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bocha.calendartest.LoginActivity;
import com.bocha.calendartest.MainActivity;
import com.bocha.calendartest.R;
import com.bocha.calendartest.adapter.eventAdapter;
import com.bocha.calendartest.data.Event;
import com.bocha.calendartest.utility.EventUtility;
import com.bocha.calendartest.utility.MiscUtility;
import com.bocha.calendartest.views.ExpandedListView;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

public class CalendarActivity extends AppCompatActivity {

    private static final String TAG = "New Events";
    public static final String PREFS_NAME = "LoginPrefs";

    private SharedPreferences userData;

    private AlertDialog permRequestDialog;

    private ListView myEventListView;
    private TextView eventTextView;

    private ArrayList<ArrayList> eventList;

    private ArrayAdapter<String> myAdapter;

    private boolean undo = false;
    private CaldroidFragment caldroidFragment;

    SimpleDateFormat formatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        myEventListView = (ExpandedListView) findViewById(R.id.list_event_calendar);
        eventTextView = (TextView) findViewById(R.id.event_textView);

        formatter = new SimpleDateFormat("dd MMM yyyy HH : mm");

        // Setup caldroid fragment
        // **** If you want normal CaldroidFragment, use below line ****
        caldroidFragment = new CaldroidFragment();

        // If Activity is created after rotation
        if (savedInstanceState != null) {
            caldroidFragment.restoreStatesFromKey(savedInstanceState,
                    "CALDROID_SAVED_STATE");
        }
        // If activity is created from fresh
        else {
            Bundle args = new Bundle();
            Calendar cal = Calendar.getInstance();
            args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
            args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
            args.putBoolean(CaldroidFragment.ENABLE_SWIPE, true);
            args.putBoolean(CaldroidFragment.SIX_WEEKS_IN_CALENDAR, true);

            caldroidFragment.setArguments(args);
        }

        // Attach to the activity
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.calendar1, caldroidFragment);
        t.commit();

        final Context listenerContext = this;

        // Setup listener
        final CaldroidListener listener = new CaldroidListener() {

            @Override
            public void onSelectDate(Date date, View view) {
                //TODO:CLicked Events anzeigen

                String text = "Selected: " + formatter.format(date);

                eventTextView.setText(text);

                ArrayList<ArrayList> matchingEvents = getMatchingEvents(date);
                if(matchingEvents.size() != 0){
                    showEventsData(matchingEvents);
                }

                updateUI();
            }

            @Override
            public void onLongClickDate(final Date date, View view) {

                final View dialogView = View.inflate(listenerContext, R.layout.item_new_event_dialog, null);
                final AlertDialog alertDialog = new AlertDialog.Builder(listenerContext).create();

                /**Setup the numberpicker for the event duration*/
                final NumberPicker hourPicker = (NumberPicker) dialogView.findViewById(R.id.detail_event_hour_picker);
                final NumberPicker minutePicker = (NumberPicker) dialogView.findViewById(R.id.detail_event_minute_picker);
                setupNumberPickers(hourPicker, minutePicker);

                dialogView.findViewById(R.id.dialog_accept_event).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        TimePicker startPicker = (TimePicker) dialogView.findViewById(R.id.start_time_picker);
                        EditText titleBox = (EditText) dialogView.findViewById(R.id.dialog_title_edit);
                        EditText descBox = (EditText) dialogView.findViewById(R.id.dialog_desc_edit);

                        int startHour = startPicker.getCurrentHour();
                        int startMinute = startPicker.getCurrentMinute();

                        int durationHour = hourPicker.getValue();
                        int durationMinute = minutePicker.getValue();

                        addEvent(MiscUtility.getStartingDate(date, startHour, startMinute), MiscUtility.getDurationMillis(durationHour, durationMinute), String.valueOf(titleBox.getText()), String.valueOf(descBox.getText()));

                        alertDialog.dismiss();
                    }});

                dialogView.findViewById(R.id.dialog_cancel_event).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        alertDialog.dismiss();
                    }});

                alertDialog.setView(dialogView);
                alertDialog.show();

                updateUI();
            }
        };

        // Setup Caldroid listener
        caldroidFragment.setCaldroidListener(listener);

        readEvents();
        updateUI();
        setupListClickListener();
        insertEvents();
    }

    /**Setup the hour and the minute picker to display the according numbers
     *
     * @param hourPicker
     * @param minutePicker
     */
    private void setupNumberPickers(NumberPicker hourPicker, NumberPicker minutePicker) {
        String[] hours = new String[24];
        for(int i=0; i<hours.length; i++)
            hours[i] = Integer.toString(i);

        hourPicker.setMinValue(1);
        hourPicker.setMaxValue(24);
        hourPicker.setWrapSelectorWheel(true);
        hourPicker.setDisplayedValues(hours);
        hourPicker.setValue(1);

        String[] minutes = new String[60];
        for(int i=0; i<minutes.length; i++)
            minutes[i] = Integer.toString(i);

        minutePicker.setMinValue(1);
        minutePicker.setMaxValue(60);
        minutePicker.setWrapSelectorWheel(true);
        minutePicker.setDisplayedValues(minutes);
        minutePicker.setValue(1);
    }

    /**Add the event using the EventUtility class*/
    private void addEvent(Date date, Long length, String eventTitle, String eventDescription) {
        //int[] startDate = {2017, 0, 24, 7, 30};
        //int[] endDate = {2017, 0, 24, 14, 30};
        Log.v(TAG, "adding Event: "+date);

        Event event = new Event(date.getTime(), date.getTime() + length, eventTitle, eventDescription);

        EventUtility.addEvent(CalendarActivity.this, event);

        updateUI();
    }

    /**Check whether the clicked date contains any events and return the matches*/
    private ArrayList<ArrayList> getMatchingEvents(Date date) {
        Long dateStart = date.getTime();
        Long dateEnd = dateStart + TimeUnit.DAYS.toMillis(1);
        ArrayList<ArrayList> matchingEvents = new ArrayList<>();

        for(int i = 0, j = eventList.size(); i < j; i++){
            Long eventStart = Long.parseLong((String)eventList.get(i).get(1));
            if(eventStart >= dateStart && eventStart <= dateEnd){
                matchingEvents.add(eventList.get(i));
            }
        }
        return matchingEvents;
    }

    /**Setup two click listeners for the listview element*/
    private void setupListClickListener() {
        //Setup the clickListener for the eventlist
        //Show the event info on click
        myEventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                String startDate = (String)eventList.get(position).get(1);
                caldroidFragment.moveToDate(new Date(Long.parseLong(startDate)));

                showEventData(position);
            }
        });

        //Setup the longClickListener for the eventlist
        //edit the title of the event on longclick
        final Context context = this;
        myEventListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                final String eventTitle = myEventListView.getItemAtPosition(position).toString();
                Log.v(TAG, "Update: " + eventTitle);
                final EditText eventEditText = new EditText(context);
                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setTitle("Update event title")
                        .setMessage("New title")
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
                return true;
            }
        });
    }

    /**Show event data in a textview below the calendar widget
     *
     * @param position Positon of the event to display in the eventlist
     */
    private void showEventData(int position) {
        ArrayList<String> clickedEvent = eventList.get(position);
        String eventInfo = "" + clickedEvent.get(0) + "\n";

        String[] dateData = EventUtility.calculateDate(Long.parseLong(clickedEvent.get(1)), Long.parseLong(clickedEvent.get(2)));
        eventInfo += dateData[0] + "\n";
        eventInfo += dateData[1] + "\n";
        eventInfo += "" + clickedEvent.get(3) + "\n";

        eventTextView.setText(eventInfo);
    }

    /**Show event data in a textview below the calendar widget
     *
     * @param eventList Arraylist containing the events to display
     */
    private void showEventsData(ArrayList<ArrayList> eventList) {
        ArrayList<String> clickedEvent;
        String eventsInfo = new String();
        for(int i = 0, j = eventList.size(); i < j; i++){
            clickedEvent = eventList.get(i);
            eventsInfo += "" + clickedEvent.get(0) + "\n";

            String[] dateData = EventUtility.calculateDate(Long.parseLong(clickedEvent.get(1)), Long.parseLong(clickedEvent.get(2)));
            eventsInfo += dateData[0] + "\n";
            eventsInfo += dateData[1] + "\n";

            eventsInfo += "" + clickedEvent.get(3) + "\n" + "\n";
        }

        eventTextView.setText(eventsInfo);
    }

    /**Insert the extracted events to the calendar fragment*/
    private void insertEvents() {
        Calendar cal;
        Date eventDate;

        for(int i  = 0, j = eventList.size(); i < j; i++){

            // Max date is next 7 days
            cal = Calendar.getInstance();
            cal.add(Calendar.DATE, 7);
            cal.getTime();
            eventDate = new Date(Long.parseLong((String)eventList.get(i).get(1)));

            if (caldroidFragment != null) {
                ColorDrawable eventDrawable = new ColorDrawable(Color.RED);
                caldroidFragment.setBackgroundDrawableForDate(eventDrawable, eventDate);
            }
        }
    }

    /**Delay when adding a new event to the calendar
     * -> The list doesnt update correctly sometimes*/
    private void updateUI() {
        readEvents();

        ArrayList<String> taskList = new ArrayList<>();

        if (eventList != null) {
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
                Log.v(TAG, "TaskList: "+taskList);
            }
        }
        setupListClickListener();
    }

    private void readEvents(){
        if(permissionGrantedReadCal()){
            //Clear the eventList if necessary
            if(eventList != null){
                eventList.clear();
            }
            //Update the eventList
            eventList = EventUtility.readCalendarEvent(this);
            Log.v(TAG, "EventList: " + eventList);
        }else{
            Log.v(TAG, "Read events permission not granted");
        }
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

    /**
     * Save current states of the Calendar here
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);

        if (caldroidFragment != null) {
            caldroidFragment.saveStatesToKey(outState, "CALDROID_SAVED_STATE");
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
