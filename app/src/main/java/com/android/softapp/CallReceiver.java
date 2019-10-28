package com.android.softapp;

import android.content.Context;

import java.util.Date;

public class CallReceiver extends PhonecallReceiver {
    CallListener mCallListener;


    public void setCallListener(CallListener callListener) {
        mCallListener = callListener;
    }

    @Override
    protected void onIncomingCallStarted(Context ctx, String number, Date start) {

//        Log.e("call","onIncomingCallStarted"+number);
//        Intent trIntent = new Intent("android.intent.action.MAIN");
//        trIntent.setClass(ctx, MainActivity.class);
//
//        trIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        ctx.startActivity(trIntent);
//       showAlertDialog(ctx,"Incoming Call",number);
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {

    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start) {
    }

public interface CallListener
{
    void onRecieve(String number,String title);
}
}