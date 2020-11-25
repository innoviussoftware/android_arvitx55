package com.arvihealthscanner.Utils

import android.content.Context
import android.net.NetworkInfo
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager



object  ConnectivityDetector {

    public lateinit var msContext: Context

    fun ConnectivityDetector(context: Context) {
        this.msContext = context
    }


    fun isConnectingToInternet(context: Context): Boolean {

        val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivity != null) {
            val info = connectivity.allNetworkInfo
            if (info != null)
                for (i in info.indices)
                    if (info[i].state == NetworkInfo.State.CONNECTED) {
                        return true
                    }
        }
        return false

    }
}