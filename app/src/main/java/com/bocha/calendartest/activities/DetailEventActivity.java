package com.bocha.calendartest.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bocha.calendartest.R;
import com.bocha.calendartest.data.Event;
import com.bocha.calendartest.utility.EventUtility;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by bob on 04.01.17.
 */

public class DetailEventActivity extends AppCompatActivity {
    private static final String TAG = "New Events";
    public static final String PREFS_NAME = "LoginPrefs";

    private TextView titleTextView;
    private TextView startTextView;
    private TextView endTextView;
    private TextView descTextView;

    private Button acceptButton;
    private Button denyButton;

    private String eventTitle;
    private String eventDescription;
    private Long eventStart;
    private Long eventEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        titleTextView = (TextView) findViewById(R.id.detail_event_title);
        startTextView = (TextView) findViewById(R.id.detail_event_start);
        endTextView = (TextView) findViewById(R.id.detail_event_end);
        descTextView = (TextView) findViewById(R.id.detail_event_description);

        acceptButton = (Button) findViewById(R.id.detail_event_allow_button);
        denyButton = (Button) findViewById(R.id.detail_event_deny_button);

        getIntentData();
        setIntentData();
        setListener();

    }

    private void setListener() {
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addEvent();
            }
        });

        denyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void addEvent() {
        Event event = new Event(eventStart, eventStart, eventTitle, eventDescription);

        EventUtility.addEvent(DetailEventActivity.this, event);

        finish();
    }

    private void setIntentData() {
        titleTextView.setText(eventTitle);
        startTextView.setText(millisToDate(eventStart));
        endTextView.setText(millisToDate(eventEnd));
        descTextView.setText(eventDescription);
    }

    private String millisToDate(Long milliSeconds) {
        SimpleDateFormat formatter = new SimpleDateFormat(
                "dd/MM/yyyy hh:mm:ss a");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    private void getIntentData() {
        Intent detailIntent = getIntent();
        eventTitle = detailIntent.getStringExtra("eventTitle");
        eventStart = detailIntent.getLongExtra("eventStart", 1L);
        eventEnd = detailIntent.getLongExtra("eventEnd", 1L);
        eventDescription = detailIntent.getStringExtra("eventDesc");
        Log.v(TAG, "Fetch intent data: "+eventTitle+eventStart+eventEnd+eventDescription);
    }
}
