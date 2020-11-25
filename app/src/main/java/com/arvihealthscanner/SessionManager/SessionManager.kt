package com.arvihealthscanner.SessionManager

import android.content.Context

object SessionManager {

    private val PREF_NAME = "Arvi Session"


    fun setIsUserLoggedin(context: Context, isLogin: Boolean) {
        try {
            val preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val editor = preferences.edit()
            editor.putBoolean(AppPrefFields.SHARED_ISLOGGEDIN, isLogin)
            editor.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getIsUserLoggedin(context: Context): Boolean {
        val preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return preferences.getBoolean(AppPrefFields.SHARED_ISLOGGEDIN, false)
    }

    fun clearAppSession(context: Context) {
        try {
            val preferences =
                context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val editor = preferences.edit()
            editor.clear()
            editor.apply()
        } catch (e: Exception) {
            // TODO: handle exception
            e.printStackTrace()
        }

    }

    //Token
    fun setToken(context: Context, token: String) {
        try {
            val preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

            val editor = preferences.edit()
            editor.putString(AppPrefFields.PARAM_TOKEN, token)
            editor.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getToken(context: Context): String {
        val preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return preferences.getString(AppPrefFields.PARAM_TOKEN, "")!!
    }

    fun setKioskID(context: Context, selected: String) {
        try {
            val preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

            val editor = preferences.edit()
            editor.putString(AppPrefFields.PARAM_KIOSK_ID, selected)
            editor.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getKioskID(context: Context): String {
        val preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return preferences.getString(AppPrefFields.PARAM_KIOSK_ID, "")!!
    }

    fun setRole(context: Context, selected: String) {
        try {
            val preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

            val editor = preferences.edit()
            editor.putString(AppPrefFields.PARAM_role, selected)
            editor.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getRole(context: Context?): String {
        val preferences = context!!.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return preferences.getString(AppPrefFields.PARAM_role, "")!!
    }

    //mac address
    fun setMacAddress(context: Context, mac_address: String) {
        try {
            val preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

            val editor = preferences.edit()
            editor.putString(AppPrefFields.PARAM_MAC_ADDRESS, mac_address)
            editor.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getMacAddress(context: Context?): String {
        val preferences = context!!.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return preferences.getString(AppPrefFields.PARAM_MAC_ADDRESS, "34:DE:1A:DE:B1:8C")!!
    }

    //min face width(default screen)
    fun setDefaultFaceWidth(context: Context, width: Float) {
        try {
            val preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

            val editor = preferences.edit()
            editor.putFloat(AppPrefFields.PARAM_DEFAULT_FACE_WIDTH, width)
            editor.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getDefaultFaceWidth(context: Context?): Float {
        val preferences = context!!.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return preferences.getFloat(AppPrefFields.PARAM_DEFAULT_FACE_WIDTH, 25.0f)!!
    }

    //min face width(detect screen)
    fun setDetectFaceWidth(context: Context, width: Float) {
        try {
            val preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

            val editor = preferences.edit()
            editor.putFloat(AppPrefFields.PARAM_DETECT_FACE_WIDTH, width)
            editor.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getDetectFaceWidth(context: Context?): Float {
        val preferences = context!!.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return preferences.getFloat(AppPrefFields.PARAM_DETECT_FACE_WIDTH, 60f)!!
    }

    //top margin
    fun setTopMargin(context: Context, width: Float) {
        try {
            val preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

            val editor = preferences.edit()
            editor.putFloat(AppPrefFields.PARAM_TOP_MARGIN, width)
            editor.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getTopMargin(context: Context?): Float {
        val preferences = context!!.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return preferences.getFloat(AppPrefFields.PARAM_TOP_MARGIN, 20f)!!
    }

    //left margin
    fun setLeftMargin(context: Context, width: Float) {
        try {
            val preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

            val editor = preferences.edit()
            editor.putFloat(AppPrefFields.PARAM_LEFT_MARGIN, width)
            editor.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getLeftMargin(context: Context?): Float {
        val preferences = context!!.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return preferences.getFloat(AppPrefFields.PARAM_LEFT_MARGIN, 10f)!!
    }


    //box height
    fun setBoxHeight(context: Context, width: Float) {
        try {
            val preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

            val editor = preferences.edit()
            editor.putFloat(AppPrefFields.PARAM_BOX_HEIGHT, width)
            editor.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getBoxHeight(context: Context?): Float {
        val preferences = context!!.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return preferences.getFloat(AppPrefFields.PARAM_BOX_HEIGHT, 80f)!!
    }

    //box width
    fun setBoxWidth(context: Context, width: Float) {
        try {
            val preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

            val editor = preferences.edit()
            editor.putFloat(AppPrefFields.PARAM_BOX_WIDTH, width)
            editor.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getBoxWidth(context: Context?): Float {
        val preferences = context!!.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return preferences.getFloat(AppPrefFields.PARAM_BOX_WIDTH, 80f)!!
    }

    //detect angley
    fun setAngleY(context: Context, width: Float) {
        try {
            val preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

            val editor = preferences.edit()
            editor.putFloat(AppPrefFields.PARAM_DETECT_ANGLE_Y, width)
            editor.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAngleY(context: Context?): Float {
        val preferences = context!!.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return preferences.getFloat(AppPrefFields.PARAM_DETECT_ANGLE_Y, 12.0f)!!
    }

    //detect anglez

    fun setAngleZ(context: Context, width: Float) {
        try {
            val preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

            val editor = preferences.edit()
            editor.putFloat(AppPrefFields.PARAM_DETECT_ANGLE_Z, width)
            editor.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAngleZ(context: Context?): Float {
        val preferences = context!!.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return preferences.getFloat(AppPrefFields.PARAM_DETECT_ANGLE_Z, 10.0f)!!
    }

    //alarmlevel
    fun setAlarmLevel(context: Context, width: Float) {
        try {
            val preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

            val editor = preferences.edit()
            editor.putFloat(AppPrefFields.PARAM_ALARM_LEVEL, width)
            editor.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAlarmLevel(context: Context?): Float {
        val preferences = context!!.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return preferences.getFloat(AppPrefFields.PARAM_ALARM_LEVEL, 99.9f)!!
    }

    //temp offset
    fun setTempOffset(context: Context, width: Float) {
        try {
            val preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

            val editor = preferences.edit()
            editor.putFloat(AppPrefFields.PARAM_TEMP_OFFSET, width)
            editor.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getTempOffset(context: Context?): Float {
        val preferences = context!!.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return preferences.getFloat(AppPrefFields.PARAM_TEMP_OFFSET, 0.0f)!!
    }

    //screening mode
    fun setScreeningMode(context: Context, mode: String) {
        try {
            val preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

            val editor = preferences.edit()
            editor.putString(AppPrefFields.PARAM_SCREENING_MODE, mode)
            editor.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getScreeningMode(context: Context?): String {
        val preferences = context!!.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return preferences.getString(AppPrefFields.PARAM_SCREENING_MODE, "Facial Recognize")!!
    }

//isSetteingSeted

    fun setSettingSeted(context: Context, type: Boolean) {
        try {
            val preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

            val editor = preferences.edit()
            editor.putBoolean(AppPrefFields.PARAM_IS_SETTING_SETED, type)
            editor.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isSettingSeted(context: Context?): Boolean {
        val preferences = context!!.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return preferences.getBoolean(AppPrefFields.PARAM_IS_SETTING_SETED, false)!!
    }

    fun setOxiScanOption(context: Context, strOximeterScanningOption: String) {
        try {
            val preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

            val editor = preferences.edit()
            editor.putString(AppPrefFields.PARAM_OXI_SCAN_OPTION, strOximeterScanningOption)
            editor.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getOxiScanOption(context: Context?): String {
        val preferences = context!!.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return preferences.getString(AppPrefFields.PARAM_OXI_SCAN_OPTION, "Disable")!!
    }

    fun setOxiLevel(context: Context, oximeterLevel: Int) {
        try {
            val preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val editor = preferences.edit()
            editor.putInt(AppPrefFields.PARAM_OXI_LEVEL, oximeterLevel)
            editor.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getOxiLevel(context: Context?): Int {
        val preferences = context!!.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return preferences.getInt(AppPrefFields.PARAM_OXI_LEVEL, 90)!!
    }


    fun setKioskModel(context: Context, modelName: String) {
        try {
            val preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val editor = preferences.edit()
            editor.putString(AppPrefFields.PARAM_KIOSK_MODEL, modelName)
            editor.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getKioskModel(context: Context?): String {
        val preferences = context!!.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return preferences.getString(AppPrefFields.PARAM_KIOSK_MODEL, "")!!
    }

    fun setSanitizerOption(context: Context, sanitizer_option: String) {
        try {
            val preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

            val editor = preferences.edit()
            editor.putString(AppPrefFields.PARAM_Sanitizer_OPTION, sanitizer_option)
            editor.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getSanitizerOption(context: Context?): String {
        val preferences = context!!.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return preferences.getString(AppPrefFields.PARAM_Sanitizer_OPTION, "Enable")!!
    }

    fun setFaceRecognizeOption(context: Context, faceRecognizeOption: String) {
        try {
            val preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

            val editor = preferences.edit()
            editor.putString(AppPrefFields.PARAM_FaceRecognize_OPTION, faceRecognizeOption)
            editor.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getFaceRecognizeOption(context: Context?): String {
        val preferences = context!!.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return preferences.getString(AppPrefFields.PARAM_FaceRecognize_OPTION, "ON")!!
    }

    fun setRestartAppTime(context: Context, restartAppTime: String) {
        try {
            val preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

            val editor = preferences.edit()
            editor.putString(AppPrefFields.PARAM_RestartAppTime, restartAppTime)
            editor.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun getRestartAppTime(context: Context?): String {
        val preferences = context!!.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return preferences.getString(AppPrefFields.PARAM_RestartAppTime, "23:59:59")!!
    }

}