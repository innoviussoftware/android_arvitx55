package com.arvihealthscanner.Activity

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.arvihealthscanner.R
import com.arvihealthscanner.SessionManager.SessionManager
import com.arvihealthscanner.Utils.MyRestartDialog
import com.arvihealthscanner.btScan.java.DafaultActivity
import com.arvihealthscanner.btScan.java.arvi.ArviFaceDetectionProcessor
import com.arvihealthscanner.btScan.java.arvi.Config
import com.arvihealthscanner.btScan.java.arvi.Settings_Activity_organised
import com.crashlytics.android.BuildConfig
import com.crashlytics.android.Crashlytics
import com.google.firebase.FirebaseApp
import io.fabric.sdk.android.Fabric
import java.util.*


class SplashActivity : AppCompatActivity() {

    var context: Context? = null
    var MY_PERMISSIONS_REQUEST_ACCOUNTS: Int = 11001


    //todo:: restart app code
    val delayMillis: Long = 1000
    var h: Handler? = null
    var r: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        FirebaseApp.initializeApp(this@SplashActivity)
        val fabric = Fabric.Builder(this)
            .kits(Crashlytics())
            .debuggable(true) // Enables Crashlytics debugger
            .build()
        Fabric.with(fabric)


        try {

        context = SplashActivity@ this

            //todo:: priyanka 31/11/2020
            //todo:: restart app code start
//            setHandlerData()
            //todo:: restart app code end


            if (allPermissionsGranted()) {
                gotoNextPage()
            } else {
                getRuntimePermissions()
            }


        } catch (e: Exception) {
            e.printStackTrace()
            Crashlytics.logException(e.cause)
        }
    }

    private fun setHandlerData() {

        try {
            h = Handler(Looper.getMainLooper())
            r = object : Runnable {
                override fun run() {

                    //current time

                    val c: Calendar = Calendar.getInstance()
                    val hour: Int = c.get(Calendar.HOUR_OF_DAY)
                    val min: Int = c.get(Calendar.MINUTE)
                    val sec: Int = c.get(Calendar.SECOND)

                    var showHour = "11"
                    var showMinute = "59"
                    var showSec = "00"



                    if (hour < 10) {
                        if (hour == 0) {
                            showHour = "24"
                        } else {
                            showHour = "0" + hour.toString()
                        }
                    } else {
                        showHour = hour.toString()
                    }

                    if (min < 10) {
                        showMinute = "0" + min.toString()
                    } else {
                        showMinute = min.toString()
                    }

                    if (sec < 10) {
                        showSec = "0" + sec.toString()
                    } else {
                        showSec = sec.toString()
                    }


                    val currenttime =
                        "$showHour : $showMinute : $showSec"
                    Log.e("time:", currenttime)
                    Log.e("Restart_at:", SessionManager.getRestartAppTime(context!!))
                    if (currenttime.equals(SessionManager.getRestartAppTime(context!!))) {
                        Log.e("restart", "true")
                        //restarting the activity
                        val intent = Intent(context!!, SplashActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        finish()
                        startActivity(intent)
                    }
                    h!!.postDelayed(this, delayMillis)
                }
            }

            h!!.post(r)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun gotoNextPage() {
        try {
            if (SessionManager.isSettingSeted(context)) {
                Config.MAC_ADDRESS = SessionManager.getMacAddress(context)
                Config.defaultMinFaceWidth = SessionManager.getDefaultFaceWidth(context)
                Config.detectMinFaceWidth = SessionManager.getDetectFaceWidth(context)
                Config.fixedBoxTopMargin = SessionManager.getTopMargin(context)
                Config.fixedBoxLeftMargin = SessionManager.getLeftMargin(context)
                Config.fixedBoxHeight = SessionManager.getBoxHeight(context)
                Config.fixedBoxWidth = SessionManager.getBoxWidth(context)
                Config.detectAngleY = SessionManager.getAngleY(context)
                Config.detectAngleZ = SessionManager.getAngleZ(context)
                //todo:: Priyanka 27-10
              //  Config.tempAlarm = SessionManager.getAlarmLevel(context)
              //  Config.tempOffset = SessionManager.getTempOffset(context)
                Config.oximeterLevel = SessionManager.getOxiLevel(context)
                Config.oxiScanOption = SessionManager.getOxiScanOption(context)
                Config.santitizerOption = SessionManager.getSanitizerOption(context)
                Config.faceRecognizeOption = SessionManager.getFaceRecognizeOption(context)
                Config.restartAppTime = SessionManager.getRestartAppTime(context)
                if (!SessionManager.getScreeningMode(context!!).equals("Facial Recognize")) {
                    var from = "1" //intent.getStringExtra("from")
                    var intent = Intent(context, ScanQRCodeActivity::class.java)
                    intent.putExtra("from", from)
                    startActivity(intent)
                } else {
                    val i = Intent(applicationContext, DafaultActivity::class.java)
                    ArviFaceDetectionProcessor.fixedBox = null
                    startActivity(i)
                }
            } else {
                var intent = Intent(context, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun allPermissionsGranted(): Boolean {
        for (permission in getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                return false
            }
        }
        return true
    }

    private fun isPermissionGranted(context: Context, permission: String): Boolean {
        if (ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.i("a", "Permission granted: $permission")
            return true
        }
        Log.i("a", "Permission NOT granted: $permission")
        return false
    }

    private fun getRequiredPermissions(): Array<String> {
        try {
            val info = this.packageManager
                .getPackageInfo(this.packageName, PackageManager.GET_PERMISSIONS)
            val ps = info.requestedPermissions
            return if (ps != null && ps.size > 0) {
                return ps
            } else {
                return emptyArray()
            }
        } catch (e: Exception) {
            return emptyArray()
        }

    }

    private fun getRuntimePermissions() {
        try {
            val allNeededPermissions = java.util.ArrayList<String>()
            for (permission in getRequiredPermissions()) {
                if (!isPermissionGranted(this, permission)) {
                    allNeededPermissions.add(permission)
                }
            }

            if (!allNeededPermissions.isEmpty()) {
                ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toTypedArray(), MY_PERMISSIONS_REQUEST_ACCOUNTS
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_ACCOUNTS -> {
                try {
                    gotoNextPage()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (allPermissionsGranted()) {
            gotoNextPage()
        } else {
            getRuntimePermissions()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
