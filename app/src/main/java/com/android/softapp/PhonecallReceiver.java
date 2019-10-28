package com.android.softapp;

import java.util.Date;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.softapp.R;

public abstract class PhonecallReceiver extends BroadcastReceiver {

    //The receiver will be recreated whenever android feels like it.  We need a static variable to remember data between instantiations

    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static Date callStartTime;
    private static boolean isIncoming;
    private static String savedNumber;  //because the passed incoming is only valid in ringing


    private callCallback mCallCallback;

    public void setCallCallback(callCallback callCallback) {
        mCallCallback = callCallback;

    }

    @Override
    public void onReceive(Context context, Intent intent) {


        //We listen to two intents.  The new outgoing call only tells us of an outgoing call.  We use it to get the number.
        if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
            savedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
        }
        else{
            String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
            String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
            int state = 0;
            if(stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)){
                state = TelephonyManager.CALL_STATE_IDLE;
            }
            else if(stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)){
                state = TelephonyManager.CALL_STATE_OFFHOOK;
            }
            else if(stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)){
                state = TelephonyManager.CALL_STATE_RINGING;
            }


            onCallStateChanged(context, state, number);
        }
    }

    //Derived classes should override these to respond to specific events of interest
    protected void onIncomingCallStarted(Context ctx, String number, Date start){}
    protected void onOutgoingCallStarted(Context ctx, String number, Date start){}
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end){}
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end){}
    protected void onMissedCall(Context ctx, String number, Date start){}

    //Deals with actual events

    //Incoming call-  goes from IDLE to RINGING when it rings, to OFFHOOK when it's answered, to IDLE when its hung up
    //Outgoing call-  goes from IDLE to OFFHOOK when it dials out, to IDLE when hung up
    public void onCallStateChanged(Context context, int state, String number) {
        if(lastState == state){
            //No change, debounce extras
            return;
        }
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                isIncoming = true;
                callStartTime = new Date();
                savedNumber = number;
                onIncomingCallStarted(context, number, callStartTime);
//                if(mCallCallback!=null)
//                {
//                    mCallCallback.onCallRecieved(number,"Incoming Call");
//                }
                showAlertDialog(context,number,"incoming Call");
//                showNotificationDialog(context,number,"incoming Call");
                displayCustomNotificationForOrders(context,"Incoming Call",number);
                Toast.makeText(context, "InComing Call "+number, Toast.LENGTH_SHORT).show();
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                if(lastState != TelephonyManager.CALL_STATE_RINGING){
                    isIncoming = false;
                    callStartTime = new Date();
                    onOutgoingCallStarted(context, savedNumber, callStartTime);                     
                }
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                if(lastState == TelephonyManager.CALL_STATE_RINGING){
                    //Ring but no pickup-  a miss
                    onMissedCall(context, savedNumber, callStartTime);
                }
                else if(isIncoming){
                    onIncomingCallEnded(context, savedNumber, callStartTime, new Date());
                    showAlertDialog(context,number,"CallEnded");
                    displayCustomNotificationForOrders(context,"CallEnded",number);
                    Toast.makeText(context, "CallEnded "+number, Toast.LENGTH_SHORT).show();
                }
                else{
                    onOutgoingCallEnded(context, savedNumber, callStartTime, new Date());                                               
                }
                break;
        }
        lastState = state;
    }
    private void showNotificationDialog(Context ctx, String number,String msg) {

        Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(ctx)


                .setSmallIcon(R.drawable.ic_launcher_background)
                .setLargeIcon(BitmapFactory.decodeResource(ctx.getResources(),
                        R.drawable.ic_launcher_background))
                .setContentTitle("Alert")
                .setColor(ctx.getResources().getColor(R.color.colorPrimary))
.setSound(uri)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg+"\n"+number))
                .setContentText(msg+"\n"+number)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setAutoCancel(true);


        NotificationManager notificationManager =
                (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());

//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
//        builder.setSmallIcon(R.drawable.ic_wald_poi)
//                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_poi))
//                .setColor(getResources().getColor(R.color.primary))
//                .setContentTitle(getString(R.string.hello))
//                .setContentIntent(notificationPendingIntent)
//                .setContentText(String.format(getString(R.string.notification), viewObject.getTitle()))
//                .setDefaults(Notification.DEFAULT_ALL)
//                .setStyle(bigText)
//                .setPriority(NotificationManager.IMPORTANCE_HIGH)

    }
    private void displayCustomNotificationForOrders(Context context,String title, String description) {

         NotificationChannel mChannel;
         NotificationManager notifManager;

            notifManager = (NotificationManager) context.getSystemService
                    (Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder builder;
            Intent intent = new Intent(context, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent;
            int importance = NotificationManager.IMPORTANCE_HIGH;

                mChannel = new NotificationChannel
                        ("0", title, importance);
                mChannel.setDescription(description);
                mChannel.enableVibration(true);
                notifManager.createNotificationChannel(mChannel);

            builder = new NotificationCompat.Builder(context, "0");

            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingIntent = PendingIntent.getActivity(context, 1251, intent, PendingIntent.FLAG_ONE_SHOT);
            builder.setContentTitle(title)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                            R.mipmap.ic_launcher))
                    .setContentText(description)  // required
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)

                    .setBadgeIconType(R.mipmap.ic_launcher)
                    .setContentIntent(pendingIntent)
                    .setSound(RingtoneManager.getDefaultUri
                            (RingtoneManager.TYPE_NOTIFICATION));
            Notification notification = builder.build();
            notifManager.notify(0, notification);
        } else {

//            Intent intent = new Intent(context, MainActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            PendingIntent pendingIntent = null;
//
//            pendingIntent = PendingIntent.getActivity(context, 1251, intent, PendingIntent.FLAG_ONE_SHOT);
//
//            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
//                    .setContentTitle(title)
//                    .setContentText(description)
//                    .setAutoCancel(true)
//                    .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
//                    .setSound(defaultSoundUri)
//                    .setContentIntent(pendingIntent)
//                    .setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle(title).bigText(description));
//
//            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//            notificationManager.notify(1251, notificationBuilder.build());

            showNotificationDialog(context,description,title);
        }
    }
    private void showAlertDialog(Context context, String msg, String number)
    {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setCancelable(true);
        dialog.setTitle(msg);
        dialog.setMessage(""+number);
        final AlertDialog alert = dialog.create();
        alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

        alert.show();
    }

  public interface callCallback
    {
        public void onCallRecieved(String number,String callType);
    }
}