package com.example.kalakoff.bgc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
//this class is to satrt background service to capture the image
public class LostmodeAlarm extends BroadcastReceiver {
    public static final int REQUEST_CODE = 12345;
    public static final String ACTION = "com.example.kalakoff.bgc";


    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, BackgroundCameraService.class);
//        Intent i = new Intent(context, FrontCamService.class);
        i.putExtra("foo", "bar");


        context.startService(i);
    }
}
