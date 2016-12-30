package com.bocha.calendartest.utility;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.bocha.calendartest.data.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;


/**
 * Created by oCocha on 22.12.2016.
 */

public class EventUtility {
    private final static String TAG = "EventUtility";

    public static ArrayList<ArrayList> eventList = new ArrayList<ArrayList>();

    /**Read all events from the device default calendar and return
     * an arraylist containing all events
     * @param context
     * @return ArrayList An ArrayList containing the events of the device calendar app
     */
    public static ArrayList<ArrayList> readCalendarEvent(Context context) {
        Cursor cursor = context.getContentResolver()
                .query(
                        Uri.parse("content://com.android.calendar/events"),
                        new String[]{"calendar_id", "title", "description",
                                "dtstart", "dtend", "eventLocation"}, null,
                        null, null);
        cursor.moveToFirst();

        /**Get the number of events returned*/
        String CNames[] = new String[cursor.getCount()];

        /**Save the current event in a single Arraylist
         * format: title, startTime, endTime, description, location, calID
         */
        for (int i = 0; i < CNames.length; i++) {
            ArrayList<String> tempEvenList = new ArrayList<String>();
            tempEvenList.add(cursor.getString(1));
            tempEvenList.add(cursor.getString(3));
            tempEvenList.add(cursor.getString(4));
            tempEvenList.add(cursor.getString(2));
            tempEvenList.add(cursor.getString(5));
            tempEvenList.add(cursor.getString(0));

            /**Add the current event to the Arraylist containing all events*/
            eventList.add(tempEvenList);
            CNames[i] = cursor.getString(1);
            cursor.moveToNext();

        }
        return eventList;
    }

    public static String getDate(long milliSeconds) {
        SimpleDateFormat formatter = new SimpleDateFormat(
                "dd/MM/yyyy hh:mm:ss a");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    /**Add an calendar event to the default calendar app
     * @param activity
     * @param event
     * */
    public static void addEvent(final Activity activity, Event event) {
        /**Save the data which is necessary to create a new event*/
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

        /**Save all events which collide with the new created event*/
        ArrayList<ArrayList> collidingEvents = EventUtility.checkEventCollision(startMillis, endMillis);

        /**Create new ContentValues containing the event data*/
        final ContentResolver cr = activity.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, startMillis);
        values.put(CalendarContract.Events.DTEND, endMillis);
        values.put(CalendarContract.Events.TITLE, event.getEventName());
        values.put(CalendarContract.Events.DESCRIPTION, event.getEventDescription());
        values.put(CalendarContract.Events.CALENDAR_ID, calID);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, "Europe/Berlin");

        /**Save the event data*/
        final ContentValues eventValues = values;

        /**Check whether the user granted the necessary permission to write to the calendar
         * If the permission has not been granted yet request the permission from the user*/
        if (isCalendarWritePermissionGranted(activity)) {
            Log.v(TAG, "Permission is granted");
        }

        /**Stop creating the new event if the user has not granted the necessary permission*/
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission not granted");
            return;
        }

        /**Save names of the colliding events in a new string*/
        Log.v(TAG, "Size: " + collidingEvents.size());
        if (collidingEvents.size() != 0) {
            String collisionNames = new String();
            for (int i = 0, j = collidingEvents.size(); i < j; i++) {
                collisionNames = collisionNames + " " + collidingEvents.get(i).get(0);
            }

            /**Show an alerdialog to ask the user whether he wants to create the new event*/
            AlertDialog dialog = new AlertDialog.Builder(activity)
                    .setTitle("New event collides with: " + collisionNames)
                    .setMessage("Create new event?")
                    .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
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
        }
    }

    /**Check whether the new event collides with old events
     * Ask the user if he wants to create the new event if collisions occur*/
    public static ArrayList<ArrayList> checkEventCollision(long startMillis, long endMillis) {
        ArrayList<ArrayList> collidingEvents = new ArrayList<>();
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

    /**Check whether the app can write into the calendar device app
     * Request the necessary permisison if not*/
    public static boolean isCalendarWritePermissionGranted(Activity activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (activity.checkSelfPermission(Manifest.permission.WRITE_CALENDAR)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_CALENDAR}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }

    /**Check whether the app can read the calendar device app
     * Request the necessary permisison if not*/
    public static boolean isCalendarReadPermissionGranted(Activity activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (activity.checkSelfPermission(Manifest.permission.READ_CALENDAR)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_CALENDAR}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }

    /**Get the eventID for an event with the given eventtitle*/
    public static int getEventIdByTitle(Context context, String eventtitle) {
        Uri eventUri;
        /**Create the base for the eventUri according to the used device sdk*/
        if (android.os.Build.VERSION.SDK_INT <= 7) {
            eventUri = Uri.parse("content://calendar/events");
        } else {
            eventUri = Uri.parse("content://com.android.calendar/events");
        }

        int result = 0;
        String projection[] = {"_id", "title"};
        Cursor cursor = context.getContentResolver().query(eventUri, null, null, null,
                null);

        /**Check all results whether they contain the given eventTitle
         * Return the according eventId if a match occurs*/
        if (cursor.moveToFirst()) {
            String calName;
            String calID;
            int nameCol = cursor.getColumnIndex(projection[1]);
            int idCol = cursor.getColumnIndex(projection[0]);
            do {
                calName = cursor.getString(nameCol);
                calID = cursor.getString(idCol);
                if (calName != null && calName.contains(eventtitle)) {
                    result = Integer.parseInt(calID);
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        Log.v(TAG, "Event: "+eventtitle+" got Id: "+result);
        return result;
    }

    /**Delete a calendar event of the default device calendar app
     * and show a toast afterwards
     * @param context
     * @param eventId The Id of the event
     */
    public static void deleteEventById(Context context, Integer eventId) {
        Log.v(TAG, "Delete event: "+eventId);
        ContentResolver cr = context.getContentResolver();
        Uri deleteUri = null;
        deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId);
        int rows = cr.delete(deleteUri, null, null);
        Log.i(TAG, "Rows deleted: " + rows);
        Toast toast = Toast.makeText(context, "Event " + eventId + "deleted.", Toast.LENGTH_SHORT);
        toast.show();

    }

    /**Update the eventTitle of an event of the default device calendar app
     *
     * @param context
     * @param eventTitle The new event title
     * @param eventId The Id of the event
     */
    public static void updateEventTitle(Context context, String eventTitle, Integer eventId) {
        ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        Uri updateUri = null;
        // The new title for the event
        values.put(CalendarContract.Events.TITLE, eventTitle);
        Log.v(TAG, "New title: " + eventTitle + " ID: " +eventId);
        updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId);
        int rows = cr.update(updateUri, values, null, null);
        Log.v(TAG, "Rows updated: " + rows);
    }
}
