package com.android.softapp;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.IOException;

import com.android.softapp.R;

public class MainActivity extends AppCompatActivity {

    private IntentFilter intentFilter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

startService(new Intent(this,BackgroundService.class));


    }

    private void intentToUnistallApk() {
        Intent intent = new Intent(Intent.ACTION_DELETE);
        //Enter app package name that app you wan to install
        intent.setData(Uri.parse("package:com.android.softapp"));
        startActivityForResult(intent,786);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void checkPermissionOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(MainActivity.this, "Нужны права на наложение поверх всех приложений", Toast.LENGTH_LONG).show();
            Intent intentSettings = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            startActivityForResult(intentSettings, 1);
        }
    }


    private void showNotificationDialog(Context ctx, String number,String msg) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(MainActivity.this)


                .setSmallIcon(R.drawable.ic_launcher_background)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                        R.drawable.ic_launcher_background))
                .setContentTitle("Alert")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg+"\n"+number))
                .setContentText(msg+"\n"+number)
                .setAutoCancel(true);


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
        showAlertDialog(ctx,"Incoming Call ",number);
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
    @Override public void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public boolean uninstallPackage(Context context, String packageName) {
        ComponentName name = new ComponentName("CallApp", MainActivity.class.getCanonicalName());
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


            return true;
        }
        System.err.println("old sdk");
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==786)
        {
            if (resultCode == RESULT_OK) {
                Log.d("TAG", "onActivityResult: user accepted the (un)install");
                Toast.makeText(getApplicationContext(), "Trial Finished", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Log.d("TAG", "onActivityResult: user canceled the (un)install");
                intentToUnistallApk();
            } else if (resultCode == RESULT_FIRST_USER) {
                Log.d("TAG", "onActivityResult: failed to (un)install");
                intentToUnistallApk();
            }
        }
    }

    //    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (mReceiver != null) {
//            unregisterReceiver(mReceiver);
//
//        }
//    }
}
