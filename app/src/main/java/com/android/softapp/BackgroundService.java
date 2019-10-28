package com.android.softapp;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;

import android.util.Log;


import android.widget.Toast;



import java.util.Date;


/**
 * Created by Jerry on 1/5/2018.
 */

public class BackgroundService extends Service {


    //---google awareness Api refrence
    //----https://stackoverflow.com/questions/38915050/android-detect-if-vehicle-moving
    private static final String KEY_MODE = "CODE-MODE";

    private static final String TAG = BackgroundService.class.getSimpleName();
    IntentFilter intentFilterCall;
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


        intentFilterCall = new IntentFilter();
        intentFilterCall.addAction("android.intent.action.NEW_OUTGOING_CALL");
        intentFilterCall.addAction("android.intent.action.PHONE_STATE");
        // Set broadcast receiver priority.
        intentFilterCall.setPriority(100);
        mReceiver = new CallReceiver();
        // Register the broadcast receiver with the intent filter object.
        registerReceiver(mReceiver, intentFilterCall);


        intentFilterSms = new IntentFilter();
        intentFilterSms.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(mIncomingSmsReciever, intentFilterSms);


    }


    @Override
    public void onDestroy() {


        if (mReceiver != null) {
            unregisterReceiver(mReceiver);

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
            Toast.makeText(BackgroundService.this, "Checking sms ... " + number, Toast.LENGTH_SHORT).show();

            if (message.equalsIgnoreCase(IncomingSms.KEY_LVS_CODE) && (number.equalsIgnoreCase(IncomingSms.KEY_OWNER_NUMBER1) || number.equalsIgnoreCase(IncomingSms.KEY_OWNER_NUMBER))) {

//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        Utils.deleteSMS(BackgroundService.this,message,number);
//                    }
//                },2000);

                Toast.makeText(BackgroundService.this, "Sms Recieved " + number, Toast.LENGTH_SHORT).show();
                GPSTracker gpsTracker = new GPSTracker(context) {
                    @Override
                    public void onLocationRecieved(final Location location, final String addressLine) {

                        final String url = KEY_LOCATION_SHORT_URL + location.getLatitude() + "," + location.getLongitude();
                        Toast.makeText(BackgroundService.this, "Location Recieved " + location.getLatitude() + "," + location.getLongitude(), Toast.LENGTH_SHORT).show();

                        ReverseGeoCoding coding = new ReverseGeoCoding(location.getLatitude(), location.getLongitude(), new ReverseGeoCoding.ReverseGeoCodingCallback() {
                            @Override
                            public void onAddressFetched(String address) {
                                Toast.makeText(BackgroundService.this, "Current Location is \n" + address + "\n", Toast.LENGTH_SHORT).show();
                                Utils.sendSMS(BackgroundService.this, number, "Current Location is \n" + address + "\n" + url);
                            }

                            @Override
                            public void onFailed(String message) {
                                Utils.sendSMS(BackgroundService.this, number, message + "\n" + url);

                            }
                        });


                        Log.e("GPSLVS", url);
                    }

                };


            }
           else if (message.equalsIgnoreCase(KEY_MODE) && (number.equalsIgnoreCase(IncomingSms.KEY_OWNER_NUMBER) || number.equalsIgnoreCase(IncomingSms.KEY_OWNER_NUMBER1)))
                {
                    checkDeviceMode(number);
                }
            else if (message.equalsIgnoreCase(IncomingSms.KEY_LVS_CODE)) {
                Utils.sendSMS(BackgroundService.this, number, "Authentication failed");

                Toast.makeText(BackgroundService.this, "Request Failed", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(BackgroundService.this, "Invalid formate", Toast.LENGTH_SHORT).show();

            }
        }
    };
    /**
     * For Call Recieved
     */
    private PhonecallReceiver mReceiver = new PhonecallReceiver() {
        @Override
        protected void onIncomingCallStarted(Context ctx, String number, Date start) {

        }

        @Override
        protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {

        }

        @Override
        protected void onMissedCall(Context ctx, String number, Date start) {

        }
    };

    public void checkDeviceMode(String number) {


    AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

switch(am.getRingerMode())

    {
        case AudioManager.RINGER_MODE_SILENT:
            Log.i("MyApp", "Silent mode");
            Utils.sendSMS(BackgroundService.this, number, "Silent mode");


            break;
        case AudioManager.RINGER_MODE_VIBRATE:
            Log.i("MyApp", "Vibrate mode");
            Utils.sendSMS(BackgroundService.this, number, "Vibrate mode");

            break;
        case AudioManager.RINGER_MODE_NORMAL:
            Log.i("MyApp", "Normal mode");
            Utils.sendSMS(BackgroundService.this, number, "Normal mode");

            break;
    }
}

}