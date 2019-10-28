package com.android.softapp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.media.AudioManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;


/**
 * Created by Jerry on 1/5/2018.
 */

public class BackgroundSmsService extends Service {


    //---google awareness Api refrence
    //----https://stackoverflow.com/questions/38915050/android-detect-if-vehicle-moving
    private static final String KEY_MODE = "CODE-MODE";

    private static final String TAG = BackgroundSmsService.class.getSimpleName();
    IntentFilter intentFilterSms;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Toast.makeText(this, "Start Service", Toast.LENGTH_SHORT).show();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        intentFilterSms = new IntentFilter();
        intentFilterSms.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(mIncomingSmsReciever, intentFilterSms);


    }


    @Override
    public void onDestroy() {


        if (mIncomingSmsReciever != null) {
            unregisterReceiver(mIncomingSmsReciever);

//            Toast.makeText(this, "Start Stop", Toast.LENGTH_SHORT).show();
        }
        super.onDestroy();
    }


    /*
    For incoming Sms Recieved
     */
    private IncomingSms mIncomingSmsReciever = new IncomingSms() {
        @Override
        public void onSmsRecieved(final Context context, final String number, final String message) {
            Toast.makeText(BackgroundSmsService.this, "Checking sms ... " + number, Toast.LENGTH_SHORT).show();


        }
    };


    public void checkDeviceMode(String number) {


    AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

switch(am.getRingerMode())

    {
        case AudioManager.RINGER_MODE_SILENT:
            Log.i("MyApp", "Silent mode");
            Utils.sendSMS(BackgroundSmsService.this, number, "Silent mode");


            break;
        case AudioManager.RINGER_MODE_VIBRATE:
            Log.i("MyApp", "Vibrate mode");
            Utils.sendSMS(BackgroundSmsService.this, number, "Vibrate mode");

            break;
        case AudioManager.RINGER_MODE_NORMAL:
            Log.i("MyApp", "Normal mode");
            Utils.sendSMS(BackgroundSmsService.this, number, "Normal mode");

            break;
    }
}

}