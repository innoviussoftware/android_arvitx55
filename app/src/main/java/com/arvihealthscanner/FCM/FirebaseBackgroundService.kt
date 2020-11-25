package com.arvihealthscanner.FCM

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.legacy.content.WakefulBroadcastReceiver
import androidx.localbroadcastmanager.content.LocalBroadcastManager

var is_open: Boolean = false



class FirebaseBackgroundService : WakefulBroadcastReceiver() {


    override fun onReceive(context: Context, intent: Intent) {

        try {
            if (intent.extras != null) {
                Log.e("Notification: Noti===>", "Background")
                // gcm.notification.badge
                if (intent.extras != null) {

                        val bundle = Bundle()
                        val backgroundIntent = Intent("notification")
                        bundle.putString("noti_type", "alarm")
                        backgroundIntent.putExtras(bundle)
                        LocalBroadcastManager.getInstance(context).sendBroadcast(backgroundIntent)
                       /* val OpenIntent = Intent(context, OpenCameraActivity::class.java)
                        is_open = true
                        OpenIntent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        context.startActivity(OpenIntent)*/
                    }
                    val notificationUtils = NotificationUtils(context)
                    notificationUtils.playNotificationSound()

                } else {
                    Log.e("Notification: Noti===>", "Null intent")
                }

        } catch (e: Exception) {
            Log.e("Notification: Noti===>", "Exception" + e.message)
            e.printStackTrace()
        }

    }
}