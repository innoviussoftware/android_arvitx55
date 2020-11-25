package com.arvihealthscanner.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.arvihealthscanner.Activity.SplashActivity;

public class yourActivityRunOnStartup  extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent i = new Intent(context, SplashActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }
}
