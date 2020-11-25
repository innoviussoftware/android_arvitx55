package com.arvihealthscanner.Utils

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import android.widget.EditText



object KeyboardUtility {
    fun showKeyboard(context: Context, etEditText: EditText) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(etEditText, InputMethodManager.SHOW_IMPLICIT)
    }

    fun hideKeyboard(context: Context, view: View?) {
        if (view == null) {
            return
        }
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (!imm.isActive()) {
            return
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
    }
}