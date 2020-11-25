package com.arvihealthscanner.Utils

import android.app.ProgressDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Color.parseColor
import android.widget.ProgressBar
import com.arvihealthscanner.R


object  MyProgressDialog {
    private var progressBar: ProgressDialog? = null

    fun showProgressDialog(context: Context) {
        try {
            progressBar = ProgressDialog(context)
            progressBar!!.setMessage(context.getString(R.string.please_wait))
            progressBar!!.setCancelable(false)
            progressBar!!.show()
            val progressbar = progressBar!!.findViewById(android.R.id.progress) as ProgressBar
            progressbar.indeterminateDrawable.setColorFilter(
                Color.parseColor("#1B8CBE"),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun hideProgressDialog() {
        try {
            if (progressBar!!.isShowing) {
                progressBar!!.dismiss()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

}