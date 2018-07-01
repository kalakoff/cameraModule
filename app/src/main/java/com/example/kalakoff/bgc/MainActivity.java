package com.example.kalakoff.bgc;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

//checks firebase updates on main activity and store the data on local device txt file
//a background service check the file every ffteen minute and if the lost mode ison it will take a picture
public class MainActivity extends AppCompatActivity implements ValueEventListener{
    static String value = "OFF";
    static String userId = "user0010";  //user id of the current logged in user
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mRootReference = firebaseDatabase.getReference("mobiles/"+userId);    //root directory + user id
    private DatabaseReference mLostmodeReference = mRootReference.child("lostmode");    //2nd child, first child is user id
    private static final String Job_Tag = "my_job_tag";
    private FirebaseJobDispatcher jobDispatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scheduleAlarm();


    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {

        //check for data chages

        if(dataSnapshot.getValue(String.class)!=null)
        {
            //checking for data
            String key = dataSnapshot.getKey();
            if (key.equals("lostmode"))
            {
                String status = dataSnapshot.getValue(String.class);
                value = status;
                //Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
                if(status!= null)
                {
                    if(status.equals("ON"))//start service on main thread to take front photo
                    {
                        String[] dataToBeWritten = new String[1];
                        dataToBeWritten[0]=status;

                        FileTask fileTask = new FileTask(dataToBeWritten);
                        fileTask.execute();



                    }
                    else if(status.equals("OFF"))
                    {
                        String[] dataToBeWritten = new String[1];
                        dataToBeWritten[0]=status;
                        FileTask fileTask = new FileTask(dataToBeWritten);
                        fileTask.execute();

                    }
                }


            }
        }

    }
    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

 public void saveButton(View view){
        Intent intent = new Intent(getApplicationContext(), BackgroundCameraService.class);
        startService(intent);
 }

    @Override
    protected void onStart() {
        super.onStart();
       mLostmodeReference.addValueEventListener(this);
        //Intent intet= new Intent(this, BackgroundCameraService.class);
       // startService(intet);
//        scheduleAlarm();

    }

    // Setup a recurring alarm every half hour
    public void scheduleAlarm() {
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(getApplicationContext(), LostmodeAlarm.class);

        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, LostmodeAlarm.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Setup periodic alarm every every half hour from this point onwards
        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        //long timeInMillis = 1500;
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY


        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                1500, pIntent);

        Toast.makeText(this,"Alaram is set",Toast.LENGTH_SHORT).show();
    }

}
