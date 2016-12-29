package com.bocha.calendartest.utility;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


/**
 * Created by oCocha on 22.12.2016.
 */

public class EventUtility {
    private final static String TAG = "EventUtility";

    public static ArrayList<ArrayList> eventList = new ArrayList<ArrayList>();
    public static ArrayList<String> nameOfEvent = new ArrayList<String>();
    public static ArrayList<String> startDates = new ArrayList<String>();
    public static ArrayList<String> endDates = new ArrayList<String>();
    public static ArrayList<String> descriptions = new ArrayList<String>();

    public static ArrayList<ArrayList> readCalendarEvent(Context context) {
        Cursor cursor = context.getContentResolver()
                .query(
                        Uri.parse("content://com.android.calendar/events"),
                        new String[]{"calendar_id", "title", "description",
                                "dtstart", "dtend", "eventLocation"}, null,
                        null, null);
        cursor.moveToFirst();
        // fetching calendars name
        String CNames[] = new String[cursor.getCount()];

        // fetching calendars id
        nameOfEvent.clear();
        startDates.clear();
        endDates.clear();
        descriptions.clear();
        for (int i = 0; i < CNames.length; i++) {

            /**Save the event data in separate arraylists*/
            nameOfEvent.add(cursor.getString(1));
            startDates.add(getDate(Long.parseLong(cursor.getString(3))));
            endDates.add(getDate(Long.parseLong(cursor.getString(4))));
            descriptions.add(cursor.getString(2));

            /**Save the current event in a single Arraylist*
             * format: title, startTime, endTime, description, location, calID
             */
            ArrayList<String> tempEvenList = new ArrayList<String>();
            tempEvenList.add(cursor.getString(1));
            tempEvenList.add(cursor.getString(3));
            tempEvenList.add(cursor.getString(4));
            tempEvenList.add(cursor.getString(2));
            tempEvenList.add(cursor.getString(5));
            tempEvenList.add(cursor.getString(0));

            /**Add the current event arraylist to the arraylist containing all events*/
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

    /**Get the eventID for an event with the given eventtitle*/
    public static int getEventIdByTitle(Context context, String eventtitle) {
        Uri eventUri;
        if (android.os.Build.VERSION.SDK_INT <= 7) {
            // the old way

            eventUri = Uri.parse("content://calendar/events");
        } else {
            // the new way

            eventUri = Uri.parse("content://com.android.calendar/events");
        }

        int result = 0;
        String projection[] = {"_id", "title"};
        Cursor cursor = context.getContentResolver().query(eventUri, null, null, null,
                null);

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
        return result;
    }

    public static void deleteEventById(Context context, Integer eventId) {
        Log.v(TAG, "Delete event: "+eventId);
        ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        Uri deleteUri = null;
        deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId);
        int rows = context.getContentResolver().delete(deleteUri, null, null);
        Log.i(TAG, "Rows deleted: " + rows);

    }
}
