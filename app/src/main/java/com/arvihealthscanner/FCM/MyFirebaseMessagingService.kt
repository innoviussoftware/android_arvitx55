package com.arvihealthscanner.FCM

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.arvihealthscanner.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {


    var context: Context = this
    var keyNotification: String? = ""
    var keyValues: String? = ""
    var ownername: String? = ""
    var notifyTitle: String? = ""
    var notifyMessage: String? = ""
    var imageIcon: String? = ""


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.e("Notification: Noti===>", "Home")
     //   Log.e("Notification: ", "From: " + remoteMessage.from!!)

        if (remoteMessage == null)
            return



        try {
            for ((key, value) in remoteMessage.data) {
                Log.d("1----=----", "key, $key value $value")

                /*if (key == "notification_type") {
                    notification_type = value
                } else if (key == "time") {
                    reminder_time = value
                }*/
            }


        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (remoteMessage.notification != null) {
            try {
                Log.e("Notification: ", "Notification Body: " + remoteMessage.notification!!.body!!)
                notifyTitle = remoteMessage.notification!!.title
                notifyMessage = remoteMessage.notification!!.body
                Log.e("Notification: notiTtl", "=== $notifyTitle")
            } catch (e: Exception) {
                Log.e("Notification: ", " Exception: " + e.message)
            }

        }

        // Check if message contains a data payload.
        if (remoteMessage.data.size > 0) {
            Log.e("Notification: ", "Data Payload: " + remoteMessage.data.toString())
            try {
                for ((key, value) in remoteMessage.data) {
                    keyNotification = key
                    keyValues = value

                    Log.e("Notification: --",keyNotification)
                    Log.e("Notification: --",keyValues)

                    if (keyNotification == "gcm.notification.bedge") {
                        var notifiCount = keyValues
                        Log.e("Notification:notCnt-3--",notifiCount)
                    }
                    if (keyNotification == "gcm.notification.title") {
                        notifyTitle = keyValues
                    }
                    if (keyNotification == "gcm.notification.body") {
                        notifyMessage = keyValues
                    }

                }


                handleDataMessage(notifyTitle, notifyMessage, imageIcon)
            } catch (e: Exception) {
                Log.e("Notification: ", "Exception: " + e.message)
            }

        }

    }

    private fun handleDataMessage(
        msgTitle: String?,
        msgDesc: String?,
        imageIcon: String?
    ) {
        try {

            var intent: Intent? = null
/*
            if (!is_open) {
                is_open = true
                intent = Intent(this, OpenCameraActivity::class.java)
                intent!!.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                intent.putExtra("from","notification")
            } else {
                intent = Intent(this, OpenCameraActivity::class.java)
                intent.putExtra("from","notification")
            }
*/

            intent!!.flags = (Intent.FLAG_ACTIVITY_CLEAR_TOP
                    or Intent.FLAG_ACTIVITY_SINGLE_TOP)

            val pendingIntent =
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
            val channelId = "Default"
            var title = ""
            if (msgTitle != null) {
                title = msgTitle
            }else{
                title = "Arvi Health Scanner"
            }
            val builder =
                NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.drawable.app_icon)
                    .setContentTitle(title)
                    .setAutoCancel(true)
                    .setContentText("")
                    .setContentIntent(pendingIntent)

            val manager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Default channel",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                manager.createNotificationChannel(channel)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val channel = NotificationChannel(
                    channelId,
                    "Default channel",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                manager.createNotificationChannel(channel)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val channel = NotificationChannel(
                    channelId,
                    "Default channel",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                manager.createNotificationChannel(channel)
            } else {
            }

            manager.notify(0, builder.build())
            Log.d("Notification: msg", "onMessageReceived: ")
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }


}