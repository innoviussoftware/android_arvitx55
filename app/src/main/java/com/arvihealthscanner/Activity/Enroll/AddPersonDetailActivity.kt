package com.arvihealthscanner.Activity.Enroll

import android.app.Dialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.arvihealthscanner.Model.GetAddEmployeeResponse
import com.arvihealthscanner.Model.GetVerifyOtpResponse
import com.arvihealthscanner.Model.SendOtpResponse
import com.arvihealthscanner.Model.UpdateUserDetailResponse
import com.arvihealthscanner.R
import com.arvihealthscanner.RetrofitApiCall.APIService
import com.arvihealthscanner.RetrofitApiCall.ApiUtils
import com.arvihealthscanner.SessionManager.SessionManager
import com.arvihealthscanner.Utils.*
import com.arvihealthscanner.Utils.GoSettingScreen.openSettingScreen

import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddPersonDetailActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var strEmpId: String
    var context: Context? = null
    var snackbarView: View? = null
    var imgVwBackPDA: ImageView? = null
    var etNamePDA: EditText? = null
    var etAddressPDA: EditText? = null
    var etEmpIdPDA: EditText? = null
    var etMobileNoPDA: EditText? = null
    var rlVerifyPDA: RelativeLayout? = null
    var strName: String = ""
    var strPhone: String = ""
    var strAddress: String = ""

    var dialogOtp: Dialog? = null
    var strOtp: String = ""
    var etOtpDO: EditText? = null
    var kioskId: String? = ""
    var deviceId: String? = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_person_detail)
        try {
            setIds()
            setListeners()
        } catch (e: Exception) {
            e.printStackTrace()
          
        }
    }

    private fun setListeners() {
        try {
            imgVwBackPDA!!.setOnClickListener(this)
            rlVerifyPDA!!.setOnClickListener(this)
        } catch (e: Exception) {
            e.printStackTrace()
          
        }
    }

    private fun setIds() {
        try {
            context = AddPersonDetailActivity@ this
            snackbarView = findViewById(android.R.id.content)
            imgVwBackPDA = findViewById(R.id.imgVwBackPDA)
            etNamePDA = findViewById(R.id.etNamePDA)
            etEmpIdPDA = findViewById(R.id.etEmpIdPDA)
            etAddressPDA = findViewById(R.id.etAddressPDA)
            etMobileNoPDA = findViewById(R.id.etMobileNoPDA)
            rlVerifyPDA = findViewById(R.id.rlVerifyPDA)
        } catch (e: Exception) {
            e.printStackTrace()
       
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.imgVwBackPDA -> {
                finish()
            }
            R.id.rlVerifyPDA -> {
                try {
                    KeyboardUtility.hideKeyboard(context!!, rlVerifyPDA!!)
                    if (isValidInput()) {
                        if (ConnectivityDetector.isConnectingToInternet(context!!)) {
                            /*callAddEmployeeApi()*/
                            var intent =
                                Intent(context!!, TakePersonPhotoActivity::class.java)
                            intent.putExtra("name", strName)
                            intent.putExtra("mobile",strPhone)
                            intent.putExtra("employeeId",strEmpId)
                            startActivity(intent)
                            //callSendOtpApi()
                        } else {
                            SnackBar.showInternetError(context!!, snackbarView!!)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                  
                }
            }
        }
    }

    private fun callAddEmployeeApi() {
        try {
            var jsonObject = JsonObject()

            jsonObject.addProperty("mobile", strPhone)
            jsonObject.addProperty("email","")
            jsonObject.addProperty("employeeId",strEmpId)
            jsonObject.addProperty("name",strName)

            var mAPIService: APIService? = null
            mAPIService = ApiUtils.apiService
            MyProgressDialog.showProgressDialog(context!!)
            mAPIService!!.addEmployee(
                "application/json","Bearer "+SessionManager.getToken(context!!) ,jsonObject

            )

                .enqueue(object : Callback<GetAddEmployeeResponse> {

                    override fun onResponse(
                        call: Call<GetAddEmployeeResponse>,
                        response: Response<GetAddEmployeeResponse>
                    ) {
                        MyProgressDialog.hideProgressDialog()
                        try {
                            if (response.code() == 200) {
                                var token = SessionManager.getToken(context!!)

                                var intent =
                                    Intent(context!!, TakePersonPhotoActivity::class.java)
                                intent.putExtra("name", strName)
                                intent.putExtra("mobile",strPhone)
                                intent.putExtra("employeeId",strEmpId)
                                intent.putExtra("token",token)
                                startActivity(intent)
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
                        call: Call<GetAddEmployeeResponse>,
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

    private fun callSendOtpApi() {
        /*Default otp is 934663*/
        try {
            var jsonObject = JsonObject()
            var strPhone = "91" + strPhone

            jsonObject.addProperty("mobile", strPhone)


            var mAPIService: APIService? = null
            mAPIService = ApiUtils.apiService
            MyProgressDialog.showProgressDialog(context!!)
            mAPIService!!.sendOtp(
                "application/json", jsonObject

            )

                .enqueue(object : Callback<SendOtpResponse> {

                    override fun onResponse(
                        call: Call<SendOtpResponse>,
                        response: Response<SendOtpResponse>
                    ) {
                        MyProgressDialog.hideProgressDialog()
                        try {
                            if (response.code() == 200) {
                                openVerifyOtpDialog()
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
                        call: Call<SendOtpResponse>,
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

    private fun openVerifyOtpDialog() {

        try {
            dialogOtp = Dialog(context!!)
            dialogOtp!!.setCancelable(false)
            dialogOtp!!.setContentView(R.layout.dialog_verify_otp)
            dialogOtp!!.window!!.setBackgroundDrawableResource(android.R.color.transparent)

            etOtpDO = dialogOtp!!.findViewById<EditText>(R.id.etOtpDO) as EditText
            var tvNextDO = dialogOtp!!.findViewById<TextView>(R.id.tvNextDO) as TextView
            tvNextDO.setOnClickListener {
                try {
                    KeyboardUtility.hideKeyboard(context!!, tvNextDO!!)
                    if (isValidOtpInput()) {
                        if (ConnectivityDetector.isConnectingToInternet(context!!)) {
                            callVerifyOtpApi()
                        } else {
                            SnackBar.showInternetError(context!!, snackbarView!!)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                   
                }
            }

            dialogOtp!!.show()
        } catch (e: Exception) {
            e.printStackTrace()
           
        }
    }

    private fun callVerifyOtpApi() {
        try {
            var strPhone = "91" + strPhone
            kioskId = SessionManager.getKioskID(context!!)
            deviceId = FirebaseInstanceId.getInstance().token

            var jsonObject = JsonObject()
            jsonObject.addProperty("mobile", strPhone)
            jsonObject.addProperty("otp", strOtp)
            jsonObject.addProperty("kioskId", kioskId)
            jsonObject.addProperty("deviceId", deviceId)
            var mAPIService: APIService? = null
            mAPIService = ApiUtils.apiService
            MyProgressDialog.showProgressDialog(context!!)
            mAPIService!!.verifyOtp(
                "application/json", jsonObject

            )

                .enqueue(object : Callback<GetVerifyOtpResponse> {

                    override fun onResponse(
                        call: Call<GetVerifyOtpResponse>,
                        response: Response<GetVerifyOtpResponse>
                    ) {
                        MyProgressDialog.hideProgressDialog()
                        try {
                            if (response.code() == 200) {
                                dialogOtp!!.dismiss()
                                if (response.body().userExisted == true) {
                                    SnackBar.showError(
                                        context!!,
                                        snackbarView!!,
                                        "User already exists"
                                    )
                                  /*  var token = response.body().accessToken
                                    callSetUserDetailApi(token)
                                    var intent =
                                        Intent(context!!, TakePersonPhotoActivity::class.java)
                                    intent.putExtra("name", strName)
                                    intent.putExtra("token",token)
                                    startActivity(intent)*/
                                } else {
                                    var token = response.body().accessToken
                                    callSetUserDetailApi(token)
                                    var intent =
                                        Intent(context!!, TakePersonPhotoActivity::class.java)
                                    intent.putExtra("name", strName)
                                    intent.putExtra("token",token)
                                    startActivity(intent)
                                }
                            } else if (response.code() == 400) {
                                SnackBar.showError(
                                    context!!,
                                    snackbarView!!,
                                    "OTP Mismatched"
                                )
                            } else {
                                dialogOtp!!.dismiss()
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
                        call: Call<GetVerifyOtpResponse>,
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

    private fun callSetUserDetailApi(token: String) {
        try {
            var jsonObject = JsonObject()

            jsonObject.addProperty("fullName", strName)
            jsonObject.addProperty("address", strAddress)

            var mAPIService: APIService? = null
            mAPIService = ApiUtils.apiService
            mAPIService!!.updateUserProfileDetail(
                AppConstants.BEARER_TOKEN + token,
                "application/json", jsonObject)

                .enqueue(object : Callback<UpdateUserDetailResponse> {

                    override fun onResponse(
                        call: Call<UpdateUserDetailResponse>,
                        response: Response<UpdateUserDetailResponse>
                    ) {
                        try {
                            if (response.code() == 200) {
                            } else {
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                           
                        }
                    }

                    override fun onFailure(
                        call: Call<UpdateUserDetailResponse>,
                        t: Throwable
                    ) {

                    }
                })

        } catch (e: Exception) {
            e.printStackTrace()
           
        }

    }

    private fun isValidOtpInput(): Boolean {
        strOtp = etOtpDO!!.text.toString()
        if (strOtp.isEmpty()) {
            SnackBar.showValidationError(context!!, snackbarView!!, "Please enter OTP")
            etOtpDO!!.requestFocus()
            return false
        } else if (strOtp.length < 6) {
            SnackBar.showValidationError(
                context!!,
                snackbarView!!,
                "OTP length should be 6 characters long"
            )
            etOtpDO!!.requestFocus()
            return false
        }
        return true
    }

    private fun isValidInput(): Boolean {
        strName = etNamePDA!!.text.toString()
        strAddress = etAddressPDA!!.text.toString()
        strEmpId = etEmpIdPDA!!.text.toString()
        strPhone = etMobileNoPDA!!.text.toString()

        if (strName.isEmpty()) {
            SnackBar.showValidationError(context!!, snackbarView!!, "Please enter person's name")
            etNamePDA!!.requestFocus()
            return false
        } else if (strAddress.isEmpty()) {
            SnackBar.showValidationError(context!!, snackbarView!!, "Please enter person's address")
            etAddressPDA!!.requestFocus()
            return false
        }else if (strEmpId.isEmpty()) {
            SnackBar.showValidationError(context!!, snackbarView!!, "Please enter employee id")
            etEmpIdPDA!!.requestFocus()
            return false
        } else if (strPhone.isEmpty()) {
            SnackBar.showValidationError(
                context!!,
                snackbarView!!,
                "Please enter person's mobile number"
            )
            etMobileNoPDA!!.requestFocus()
            return false
        } else if (strPhone.length < 10) {
            SnackBar.showValidationError(
                context!!,
                snackbarView!!,
                "Please enter valid mobile number"
            )
            etMobileNoPDA!!.requestFocus()
            return false
        }
        return true
    }

    override fun onBackPressed() {
        try {
            openSettingScreen(context!!)
        } catch (e: Exception) {
           
        }
    }
}
