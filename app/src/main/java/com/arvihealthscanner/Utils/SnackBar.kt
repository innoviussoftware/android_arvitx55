package com.arvihealthscanner.Utils

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import com.google.android.material.snackbar.Snackbar
import com.arvihealthscanner.R


object  SnackBar {
    fun showInternetError(context: Context, view: View) {
        val snackbar = Snackbar
            .make(view, R.string.check_internet, Snackbar.LENGTH_LONG)
        snackbar.setActionTextColor(Color.WHITE)
        snackbar.setDuration(10000)
        val snackbarView = snackbar.getView()
        //        snackbarView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
        snackbarView.setBackgroundColor(Color.BLACK)
        snackbar.show()
    }

    fun showValidationError(context: Context, view: View, msg: String) {
        val snackbar = Snackbar
            .make(view, msg, Snackbar.LENGTH_LONG)
        snackbar.setActionTextColor(Color.WHITE)
        snackbar.setDuration(10000)
        val snackbarView = snackbar.getView()
        snackbarView.setBackgroundColor(context.resources.getColor(R.color.grey))
        //        snackbarView.setBackgroundColor(Color.RED);
        snackbar.show()
    }


    fun showError(context: Context, view: View, msg: String) {
        val snackbar = Snackbar
            .make(view, msg, Snackbar.LENGTH_SHORT)
        snackbar.setActionTextColor(Color.WHITE)
        snackbar.setDuration(10000)
        val snackbarView = snackbar.getView()
        //        snackbarView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
        snackbarView.setBackgroundColor(Color.RED)
        snackbar.show()
    }

    fun showSuccess(context: Context, view: View, msg: String) {
        val snackbar = Snackbar
            .make(view, msg, Snackbar.LENGTH_SHORT)
        snackbar.setActionTextColor(Color.WHITE)
        snackbar.setDuration(10000)
        val snackbarView = snackbar.getView()
        //        snackbarView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
        snackbarView.setBackgroundColor(context.resources.getColor(R.color.light_green))
        snackbar.show()
    }

    fun showInProgressError(context: Context, view: View) {
        val msg = context.getString(R.string.work_in_progress)

        val snackbar = Snackbar
            .make(view, msg, Snackbar.LENGTH_LONG)
        snackbar.setActionTextColor(Color.WHITE)
        snackbar.setDuration(10000)
        val snackbarView = snackbar.getView()
        snackbarView.setBackgroundColor(Color.GRAY)
        //        snackbarView.setBackgroundColor(Color.RED);
        snackbar.show()
    }
}