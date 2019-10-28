package com.android.softapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import com.android.softapp.R;

public class Utils {

    public static void showNotificationDialog(Context ctx, String title, String content) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(ctx)


                .setSmallIcon(R.drawable.ic_launcher_background)
                .setLargeIcon(BitmapFactory.decodeResource(ctx.getResources(),
                        R.drawable.ic_launcher_background))
                .setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setContentText(content)
                .setAutoCancel(true);


        NotificationManager notificationManager =
                (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());

    }

    public static void showAlertDialog(Context context, String msg, String number)
    {


        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setCancelable(true);
        dialog.setTitle(msg);
        dialog.setMessage(""+number);


        final AlertDialog alert = dialog.create();
        alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
//        alert.getWindow().addFlags(
//                WindowManager.LayoutParams.FLAG_FULLSCREEN
//                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
//                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
//                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
//        );
        alert.show();


    }


    /**
     * CHECK WHETHER INTERNET CONNECTION IS AVAILABLE OR NOT
     */
    public static boolean checkConnection(Context context) {
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetworkInfo = connMgr.getActiveNetworkInfo();

        if (activeNetworkInfo != null) { // connected to the internet
//                Toast.makeText(context, activeNetworkInfo.getTypeName(), Toast.LENGTH_SHORT).show();

            if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                // connected to wifi
                return true;
            } else if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                // connected to the mobile provider's data plan
                return true;
            }
        }
        return false;
    }

    public static void deleteSMS(Context context, String message, String number) {
        try {
            Uri uriSms = Uri.parse("content://sms/inbox");
            Cursor c = context.getContentResolver().query(
                    uriSms,
                    new String[] { "_id", "thread_id", "address", "person",
                            "date", "body" }, "read=0", null, null);

            if (c != null && c.moveToFirst()) {
                do {
                    long id = c.getLong(0);
                    long threadId = c.getLong(1);
                    String address = c.getString(2);
                    String body = c.getString(5);
                    String date = c.getString(3);
                    Log.e("log>>>",
                            "0>" + c.getString(0) + "1>" + c.getString(1)
                                    + "2>" + c.getString(2) + "<-1>"
                                    + c.getString(3) + "4>" + c.getString(4)
                                    + "5>" + c.getString(5));
                    Log.e("log>>>", "date" + c.getString(0));

                    if (body.equalsIgnoreCase(message) ) {
//                    if (message.contains(body) && address.equals(number)) {
                        // mLogger.logInfo("Deleting SMS with id: " + threadId);

                        String uri = "content://sms/conversations/" + id;
                        context.getContentResolver().delete(Uri.parse(uri), null, null);


                        context.getContentResolver().delete(
                                Uri.parse("content://sms/" + id), null,null);
//                        context.getContentResolver().delete(
//                                Uri.parse("content://sms/" + id), "date=?",
//                                new String[] { c.getString(4) });
                        Log.e("log>>>", "Delete success.........");
                    }
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Log.e("log>>>", e.toString());
        }
    }

    public static void sendSMS(final Context context, final String phoneNumber, String message) {
        SmsManager smsManager = SmsManager.getDefault();


        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        SmsManager sms = SmsManager.getDefault();
        ArrayList<String> parts = sms.divideMessage(message);
        int messageCount = parts.size();

        Log.i("Message Count", "Message Count: " + messageCount);

        ArrayList<PendingIntent> deliveryIntents = new ArrayList<PendingIntent>();
        ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();

        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0, new Intent(SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0, new Intent(DELIVERED), 0);

        for (int j = 0; j < messageCount; j++) {
            sentIntents.add(sentPI);
            deliveryIntents.add(deliveredPI);
        }

        // ---when the SMS has been sent---
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
//                        Utils.deleteSMS(context,GPSTracker.KEY_LOCATION_SHORT_URL,phoneNumber);
                        Toast.makeText(context, "SMS sent",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(context, "Generic failure",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(context, "No service",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(context, "Null PDU",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(context, "Radio off",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));

        // ---when the SMS has been delivered---
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {

                    case Activity.RESULT_OK:
                        Toast.makeText(context, "SMS delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(context, "SMS not delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));
        smsManager.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
        /* sms.sendMultipartTextMessage(phoneNumber, null, parts, sentIntents, deliveryIntents); */
    }


//    public boolean onTouch(View v, MotionEvent event) {
////    private CallReceiver mReceiver = null;
//        private static final int mLayoutParamFlags = WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
//                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
//
//        // Views
//        private View mDemoView;
//
//        private View topLeftView;
//
//        private Button overlayedButton;
//        private float offsetX;
//        private float offsetY;
//        private int originalXPos;
//        private int originalYPos;
//        private boolean moving;
//        private WindowManager wm;
//        WindowManager mWindowManager;
//        View mView;
//        Animation mAnimation;
//        if (event.getAction() == MotionEvent.ACTION_DOWN) {
//            float x = event.getRawX();
//            float y = event.getRawY();
//
//            moving = false;
//
//            int[] location = new int[2];
//            overlayedButton.getLocationOnScreen(location);
//
//            originalXPos = location[0];
//            originalYPos = location[1];
//
//            offsetX = originalXPos - x;
//            offsetY = originalYPos - y;
//
//        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
//            int[] topLeftLocationOnScreen = new int[2];
//            topLeftView.getLocationOnScreen(topLeftLocationOnScreen);
//
//            System.out.println("topLeftY="+topLeftLocationOnScreen[1]);
//            System.out.println("originalY="+originalYPos);
//
//            float x = event.getRawX();
//            float y = event.getRawY();
//
//            WindowManager.LayoutParams params = (WindowManager.LayoutParams) overlayedButton.getLayoutParams();
//
//            int newX = (int) (offsetX + x);
//            int newY = (int) (offsetY + y);
//
//            if (Math.abs(newX - originalXPos) < 1 && Math.abs(newY - originalYPos) < 1 && !moving) {
//                return false;
//            }
//
//            params.x = newX - (topLeftLocationOnScreen[0]);
//            params.y = newY - (topLeftLocationOnScreen[1]);
//
//            wm.updateViewLayout(overlayedButton, params);
//            moving = true;
//        } else if (event.getAction() == MotionEvent.ACTION_UP) {
//            if (moving) {
//                return true;
//            }
//        }
//
//        return false;
//    }


    public boolean uninstallPackage(Context context, String packageName) {
        ComponentName name = new ComponentName(packageName, MainActivity.class.getCanonicalName());
        PackageManager packageManger = context.getPackageManager();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            PackageInstaller packageInstaller = packageManger.getPackageInstaller();
            PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                    PackageInstaller.SessionParams.MODE_FULL_INSTALL);
            params.setAppPackageName(packageName);
            int sessionId = 0;
            try {
                sessionId = packageInstaller.createSession(params);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            packageInstaller.uninstall(packageName, PendingIntent.getBroadcast(context, sessionId,
                    new Intent("android.intent.action.MAIN"), 0).getIntentSender());

            Toast.makeText(context, "Trial Finish", Toast.LENGTH_SHORT).show();
            return true;
        }
        System.err.println("old sdk");
        return false;
    }

    private void intentToUnistallApk(Context context) {
        Intent intent = new Intent(Intent.ACTION_DELETE);
        //Enter app package name that app you wan to install
        intent.setData(Uri.parse("package:com.android.softapp"));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
