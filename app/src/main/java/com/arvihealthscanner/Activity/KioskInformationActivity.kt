package com.arvihealthscanner.Activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import com.arvihealthscanner.R
import com.arvihealthscanner.btScan.java.arvi.Settings_Activity_organised
import kotlinx.android.synthetic.main.activity_kiosk_information.*

class KioskInformationActivity : AppCompatActivity(), View.OnClickListener {

    var rlNextIA: RelativeLayout? = null
    var imgVwBackIA:ImageView?=null
    var context: Context?=null
    var snackbarView:View?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kiosk_information)
        try {
            setIds()
            setListeners()
            setData()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun setData() {
        try {
            tvComapnyNmAKI.setText(intent.getStringExtra("companyName"))
            tvComapnyIdAKI.setText(intent.getStringExtra("companyId"))
            tvLocationAKI.setText(intent.getStringExtra("kioskLocation"))
            tvIMEINoAKI.setText(intent.getStringExtra("imei"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setListeners() {
        try {
            rlNextIA!!.setOnClickListener(this)
            imgVwBackIA!!.setOnClickListener(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setIds() {
        try {
            context = KioskInformationActivity@this
            snackbarView = findViewById(android.R.id.content)
            rlNextIA = findViewById(R.id.rlNextIA)
            imgVwBackIA = findViewById(R.id.imgVwBackIA)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onClick(v: View?) {
        try {
            when (v!!.id) {
                R.id.rlNextIA -> {
                    try {
                        var from = "1"
                        var intent = Intent(context, Settings_Activity_organised::class.java)
                        intent.putExtra("from", from)
                        startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                                 }
                R.id.imgVwBackIA->{
                    finish()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBackPressed() {
        finish()
    }
}

