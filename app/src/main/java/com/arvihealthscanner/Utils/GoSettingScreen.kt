package com.arvihealthscanner.Utils

import android.content.Context
import android.content.Intent
import com.arvihealthscanner.btScan.java.arvi.Settings_Activity_organised

object GoSettingScreen {
    fun openSettingScreen(context: Context) {
       // SessionManager.clearAppSession(context)

        try {
            val intent = Intent(context, Settings_Activity_organised::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()

        }
    }
}