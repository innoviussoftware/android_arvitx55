package com.arvihealthscanner.FCM

import android.app.ActivityManager
import android.content.ContentResolver
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import com.arvihealthscanner.R


class NotificationUtils(mContext: Context) {


    val NOTIFICATION_ID = 100
    private val TAG = NotificationUtils::class.java!!.simpleName

    private var mContext: Context? = null

    init {
        this.mContext = mContext
    }

    // Playing notification sound
    fun playNotificationSound() {

            try {
                val alarmSound = Uri.parse(
                    (ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + mContext!!.getPackageName() + "/"
                            + R.raw.alert_tones)
                )
                val r = RingtoneManager.getRingtone(mContext, alarmSound)
                r.play()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


}