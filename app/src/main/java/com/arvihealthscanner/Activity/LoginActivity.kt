package com.arvihealthscanner.Activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.arvihealthscanner.Model.GetKioskById
import com.arvihealthscanner.Model.GetLoginResponse
import com.arvihealthscanner.Model.SendOtpResponse
import com.arvihealthscanner.R
import com.arvihealthscanner.RetrofitApiCall.APIService
import com.arvihealthscanner.RetrofitApiCall.ApiUtils
import com.arvihealthscanner.SessionManager.SessionManager
import com.arvihealthscanner.Utils.AppConstants.TX55
import com.arvihealthscanner.Utils.AppConstants.TX66
import com.arvihealthscanner.Utils.AppConstants.TX77
import com.arvihealthscanner.Utils.AppConstants.TX99
import com.arvihealthscanner.Utils.ConnectivityDetector
import com.arvihealthscanner.Utils.KeyboardUtility
import com.arvihealthscanner.Utils.MyProgressDialog
import com.arvihealthscanner.Utils.SnackBar
import com.arvihealthscanner.btScan.java.arvi.Settings_Activity_organised
import com.google.gson.JsonObject

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginActivity : AppCompatActivity(), View.OnClickListener {

    var etIdLA: EditText? = null
    var etEmpIdLA: EditText? = null
    var etPasswordLA: EditText? = null
    var rlNextLA: RelativeLayout? = null
    var rlCorporatesLA: RelativeLayout? = null
    var imgVwCorporateLA: ImageView? = null
    var rlCommercialLA: RelativeLayout? = null
    var imgVwComCheckLA: ImageView? = null
    var spModelLA: Spinner? = null

    var context: Context? = null
    var snackbarView: View? = null

    var selected: String = "commercial"
    internal var modelType = arrayOf(TX55, TX66, TX77, TX99)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        try {
            setIds()
            setListeners()
            setModelData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setModelData() {
        try {
            val oxiScanAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, modelType)
            oxiScanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spModelLA!!.setAdapter(oxiScanAdapter)
            spModelLA!!.setSelection(2)

            spModelLA!!.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    try {
                        Log.e("modelType: ", modelType[position])
                        SessionManager.setKioskModel(context!!, modelType[position])
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }

                override fun onNothingSelected(parent: AdapterView<*>) {

                }
            })

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun setListeners() {
        try {
            rlNextLA!!.setOnClickListener(this)
            rlCorporatesLA!!.setOnClickListener(this)
            rlCommercialLA!!.setOnClickListener(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setIds() {
        try {
            context = LoginActivity@ this
            snackbarView = findViewById(android.R.id.content)

            etIdLA = findViewById(R.id.etIdLA)
            etEmpIdLA = findViewById(R.id.etEmpIdLA)
            etPasswordLA = findViewById(R.id.etPasswordLA)
            rlNextLA = findViewById(R.id.rlNextLA)
            rlCorporatesLA = findViewById(R.id.rlCorporatesLA)
            imgVwCorporateLA = findViewById(R.id.imgVwCorporateLA)
            rlCommercialLA = findViewById(R.id.rlCommercialLA)
            imgVwComCheckLA = findViewById(R.id.imgVwComCheckLA)
            spModelLA = findViewById(R.id.spModelLA)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onClick(v: View?) {
        try {
            when (v!!.id) {
                R.id.rlCommercialLA -> {
                    setCommercial()
                }
                R.id.rlCorporatesLA -> {
                    setCorporate()
                }
                R.id.rlNextLA -> {
                    try {
                        KeyboardUtility.hideKeyboard(context!!, rlNextLA)
                        var kioskId = etIdLA!!.text.toString()
                        if (isValidInput()) {
                           /* if (!kioskId.isEmpty()) {*/
                                if (ConnectivityDetector.isConnectingToInternet(context!!)) {
                                    callNewLoginApi()
                                    //callLoginAPI(strKioskId)
                                } else {
                                    SnackBar.showInternetError(context!!, snackbarView!!)
                                }
                         /*   } else {
                                SnackBar.showError(
                                    context!!,
                                    snackbarView!!,
                                    "Please enter Kiosk ID."
                                )
                            }*/
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    var strKioskId: String = ""
    var strEmpId: String = ""
    var strPassword: String = ""
    private fun isValidInput(): Boolean {
        strKioskId = etIdLA!!.text.toString()
        strEmpId = etEmpIdLA!!.text.toString()
        strPassword = etPasswordLA!!.text.toString()
        if (strKioskId.isNullOrEmpty())
        {
            SnackBar.showError(
                context!!,
                snackbarView!!,
                "Please enter Kiosk ID."
            )
            etIdLA!!.requestFocus()
            return false
        }else if (strEmpId.isNullOrEmpty())
        {
            SnackBar.showError(
                context!!,
                snackbarView!!,
                "Please enter Employee ID."
            )
            etEmpIdLA!!.requestFocus()
            return false
        }else if (strPassword.isNullOrEmpty())
        {
            SnackBar.showError(
                context!!,
                snackbarView!!,
                "Please enter password."
            )
            etPasswordLA!!.requestFocus()
            return false
        }
        return true
    }

    private fun callNewLoginApi() {
        try {
            var jsonObject = JsonObject()

            jsonObject.addProperty("companyId", strKioskId)
            jsonObject.addProperty("employeeId", strEmpId)
            jsonObject.addProperty("password", strPassword)



            var mAPIService: APIService? = null
            mAPIService = ApiUtils.apiService
            MyProgressDialog.showProgressDialog(context!!)
            mAPIService!!.getLogin(
                "application/json", jsonObject

            )

                .enqueue(object : Callback<GetLoginResponse> {

                    override fun onResponse(
                        call: Call<GetLoginResponse>,
                        response: Response<GetLoginResponse>
                    ) {
                        MyProgressDialog.hideProgressDialog()
                        try {
                            if (response.code() == 200) {
                                SessionManager.setToken(context!!,response.body().accessToken)
                                SessionManager.setKioskID(context!!,response.body().user.employeeId)
                                var from = "1"
                                var intent = Intent(context, Settings_Activity_organised::class.java)
                                intent.putExtra("from", from)
                                startActivity(intent)
                                /*callLoginAPI(strKioskId)*/
                            } else {
                                SnackBar.showError(
                                    context!!,
                                    snackbarView!!,
                                    "Something went wrong"
                                )
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()

                        }
                    }

                    override fun onFailure(
                        call: Call<GetLoginResponse>,
                        t: Throwable
                    ) {
                        MyProgressDialog.hideProgressDialog()
                    }
                })

        } catch (e: Exception) {
            e.printStackTrace()
            MyProgressDialog.hideProgressDialog()

        }

    }

/*
    private fun callLoginAPI(kioskId: String?) {
        try {
            var mAPIService: APIService? = null
            mAPIService = ApiUtils.apiService
            MyProgressDialog.showProgressDialog(context!!)
            mAPIService!!.getKiosk(kioskId!!)

                .enqueue(object : Callback<GetKioskById> {

                    override fun onResponse(
                        call: Call<GetKioskById>,
                        response: Response<GetKioskById>
                    ) {
                        MyProgressDialog.hideProgressDialog()
                        try {
                            if (response.code() == 200) {
                                if (response.body() != null) {
                                    SessionManager.setKioskID(context!!, response.body().kioskId)
//                                    SessionManager.setToken(context!!, response.body().deviceId)

                                    var intent =
                                        Intent(context, KioskInformationActivity::class.java)
                                    intent.putExtra(
                                        "companyName",
                                        response.body().company.companyName
                                    )
                                    intent.putExtra("kioskId", response.body().kioskId)
                                    intent.putExtra("companyId", response.body().companyId)
                                    intent.putExtra("imei", response.body().imei)
                                    intent.putExtra("kioskLocation", response.body().kioskLocation)
                                    intent.putExtra("deviceId", response.body().deviceId)
                                    startActivity(intent)
                                } else {
                                    SnackBar.showError(
                                        context!!,
                                        snackbarView!!,
                                        "Something went wrong"
                                    )
                                }
                            } else {
                                SnackBar.showError(
                                    context!!,
                                    snackbarView!!,
                                    "Something went wrong"
                                )
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onFailure(
                        call: Call<GetKioskById>,
                        t: Throwable
                    ) {
                        MyProgressDialog.hideProgressDialog()
                    }
                })

        } catch (e: Exception) {
            e.printStackTrace()
            MyProgressDialog.hideProgressDialog()
        }

    }
*/

    private fun setCorporate() {
        try {
            selected = "corporate"
            rlCorporatesLA!!.setBackgroundDrawable(resources.getDrawable(R.drawable.selected_location_bg))
            imgVwCorporateLA!!.visibility = View.VISIBLE

            rlCommercialLA!!.setBackgroundDrawable(resources.getDrawable(R.drawable.light_blue_rounded_bg))
            imgVwComCheckLA!!.visibility = View.GONE
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setCommercial() {
        try {
            selected = "commercial"
            rlCommercialLA!!.setBackgroundDrawable(resources.getDrawable(R.drawable.selected_location_bg))
            imgVwComCheckLA!!.visibility = View.VISIBLE

            rlCorporatesLA!!.setBackgroundDrawable(resources.getDrawable(R.drawable.light_blue_rounded_bg))
            imgVwCorporateLA!!.visibility = View.GONE
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
