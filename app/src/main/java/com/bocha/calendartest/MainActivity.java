package com.bocha.calendartest;

import android.content.DialogInterface;
import android.content.Intent;
import android.provider.CalendarContract;
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
import java.util.ArrayList;
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main Menu";

    private ListView myEventListView;
    private ArrayAdapter<String> myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myEventListView = (ListView) findViewById(R.id.list_event);
        updateUI();
    }

    private void updateUI() {
        ArrayList<String> taskList = new ArrayList<>();

        if (myAdapter == null) {
            myAdapter = new ArrayAdapter<>(this,
                    R.layout.item_event,
                    R.id.event_title,
                    taskList);
            myEventListView.setAdapter(myAdapter);
        } else {
            myAdapter.clear();
            myAdapter.addAll(taskList);
            myAdapter.notifyDataSetChanged();
        }
    }

    public void deleteEvent(View view){
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

                                /**User got to accept
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
                                 */



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
}
