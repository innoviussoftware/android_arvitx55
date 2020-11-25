package com.arvihealthscanner.Activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.arvihealthscanner.R
import com.arvihealthscanner.Utils.AppConstants
import com.arvihealthscanner.Utils.GoSettingScreen
import com.arvihealthscanner.Utils.SnackBar
import com.arvihealthscanner.btScan.java.FaceCaptureActivity
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.crashlytics.android.Crashlytics
import io.jsonwebtoken.*
import io.jsonwebtoken.io.DecodingException
import io.jsonwebtoken.security.SignatureException
import org.json.JSONObject
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.spec.X509EncodedKeySpec


class ScanQRCodeActivity : AppCompatActivity() {

    var context: Context? = null
    var scanner_view: CodeScannerView? = null
    private lateinit var codeScanner: CodeScanner
    var snackbarView: View? = null
    var imgVwSwitch : ImageView?=null

    var isFrontFacing : Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_qrcode)
        snackbarView = findViewById(android.R.id.content)
        setIds()

    }

    private fun GetQrData(jsonString: String) {
        try {
            val jsonObject = JSONObject(jsonString)

            if (jsonObject != null) {
                val fullname = jsonObject.getString("fullName")
                val userId = jsonObject.getString("userId")
                Log.e("Qr:name = ", fullname)
                Log.e("Qr:userId = ", userId)

                if (fullname.isNullOrEmpty() || userId.isNullOrEmpty()) {
                    checkArogyaSetuQR(jsonString)
                } else {
                    val i = Intent(applicationContext, FaceCaptureActivity::class.java)
                    i.putExtra("fullname", fullname)
                    i.putExtra("userId", userId)
                    startActivity(i)
                    finish()
                }
            } else {
                SnackBar.showError(context!!, snackbarView!!, "Invalid QR code")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            checkArogyaSetuQR(jsonString)
        }
    }


    fun decryptFile(jwtToken: String): Jws<Claims> {
        val publicKeyString = AppConstants.SCANNER_PUBLIC_KEY
        if (!TextUtils.isEmpty(publicKeyString)) {
            val publicKeyBytes = Base64.decode(publicKeyString, Base64.DEFAULT)
            // create a key object from the bytes
            val keySpec = X509EncodedKeySpec(publicKeyBytes)
            val keyFactory = KeyFactory.getInstance("RSA")
            val publicKey = keyFactory.generatePublic(keySpec)
            return Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(jwtToken)
        } else {
            throw SignatureException("Public key is empty")
        }
    }

    private fun checkArogyaSetuQR(code: String) {
        Log.e("result", code);
        var claimsJws: Jws<Claims>? = null
        try {
            claimsJws = decryptFile(code)
        } catch (e: ExpiredJwtException) {
            SnackBar.showError(context!!, snackbarView!!, "Expired QR Code : ExpiredJwt Exception")
        } catch (exception: NoSuchAlgorithmException) {
            SnackBar.showError(
                context!!,
                snackbarView!!,
                "Invalid QR Code : NoSuchAlgorithm Exception"
            )
        } catch (exception: DecodingException) {
            SnackBar.showError(context!!, snackbarView!!, "Invalid QR Code : Decode Exception")
        } catch (exception: MalformedJwtException) {
            SnackBar.showError(
                context!!,
                snackbarView!!,
                "Invalid QR Code : MalformedJwt Exception"
            )
        } catch (exception: Exception) {
            SnackBar.showError(context!!, snackbarView!!, "Invalid QR Code : Exception")
        }

        if (claimsJws != null) {
            try {
                val body = claimsJws.body
                if (body != null) {

                    val s_expiry = body.get(AppConstants.EXPIRY)
                    val s_name = body.get(AppConstants.NAME)
                    val s_mobileNo = body.get(AppConstants.MOBILE)
                    val s_colorCode = body.get(AppConstants.COLOR_CODE)
                    val s_statusCode = body.get(AppConstants.STATUS_CODE)
                    val s_message = body.get(AppConstants.MESSAGE)

                    Log.e("s:exp = ", s_expiry.toString())
                    Log.e("s:name = ", s_name.toString())
                    Log.e("s:mobileNo = ", s_mobileNo.toString())
                    Log.e("s:colorCode = ", s_colorCode.toString())
                    Log.e("s:statusCode = ", s_statusCode.toString())
                    Log.e("s:message = ", s_message.toString())


                    val expiry: Int = s_expiry as Int
                    val name:String = s_name as String
                    val mobileNo:String = s_mobileNo as String
                    val colorCode : String = s_colorCode as String
                    val statusCode :Int = s_statusCode as Int
                    val message : String  = s_message  as String

                    val millisecondsMultiplier = 1000L
                    val countDownMilliSeconds = expiry * millisecondsMultiplier
                    if (expiry <= 0 || TextUtils.isEmpty(mobileNo)) {
                        SnackBar.showError(context!!, snackbarView!!, "Invalid QR Code")
                    } else if (expiry > 0 && System.currentTimeMillis() - countDownMilliSeconds > 0) {
                        SnackBar.showError(context!!, snackbarView!!, "Expired QR Code")
                    } else if (!TextUtils.isEmpty(mobileNo) && !TextUtils.isEmpty(colorCode)) {
                        showPersonStatus(name, mobileNo, statusCode, colorCode, message)

                    } else {
                        SnackBar.showError(context!!, snackbarView!!, "Invalid QR Code")
                    }
                } else {
                    SnackBar.showError(context!!, snackbarView!!, "Invalid QR Code")
                }
            } catch (ex: Exception) {
                SnackBar.showError(context!!, snackbarView!!, "Invalid QR Code")
            }

        }
    }


    private fun showPersonStatus(
        scannerName: String,
        mobileNo: String,
        statusCode: Int,
        colorCode: String,
        message: String
    ) {
        try {
            var name = ""
            if (!TextUtils.isEmpty(scannerName)) {
                name = scannerName
            }
            configureStatusText(mobileNo, statusCode, message, name)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun configureStatusText(
        mobileNo: String,
        statusCode: Int,
        message: String,
        name: String
    ) {
        try {
            when (statusCode) {
                301, 302, 800 -> setDescriptionText(
                    mobileNo,
                    name,
                    R.string.low_risk
                )
                500, 501, 502, 600 -> setDescriptionText(
                    mobileNo,
                    name,
                    R.string.high_risk
                )
                400, 401, 402, 403 -> setDescriptionText(
                    mobileNo,
                    name,
                    R.string.moderate_risk
                )
                700, 1000 -> setDescriptionText(
                    mobileNo,
                    name,
                    R.string.tested_positive_status
                )
                else -> {
                 Log.e("success: ","else")
                    SnackBar.showSuccess(context!!, snackbarView!!, message)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setDescriptionText(mobileNo: String, name: String, message: Int) {
        try {
            Log.e("success: ","setDescriptionText")

            val descVal = name + " (" + mobileNo + ") " + getString(message)
            SnackBar.showSuccess(context!!, snackbarView!!, descVal)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onResume() {
        super.onResume()
        try {
            codeScanner.startPreview()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPause() {
        try {
            codeScanner.releaseResources()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onPause()
    }


    private fun setIds() {
        try {
            context = ScanQRCodeActivity@ this
            scanner_view = findViewById(R.id.scanner_view)
            imgVwSwitch = findViewById(R.id.imgVwSwitch)

            codeScanner = CodeScanner(context!!, scanner_view!!)
            codeScanner.isAutoFocusEnabled = true
            codeScanner.camera = CodeScanner.CAMERA_FRONT
            codeScanner.decodeCallback = DecodeCallback {
                runOnUiThread {
                    try {
                        var jsonString = it.text.toString()
                        GetQrData(jsonString)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            scanner_view!!.setOnClickListener {
                try {
                    codeScanner.startPreview()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            imgVwSwitch!!.setOnClickListener {

                try {
                    if(isFrontFacing) {
                        isFrontFacing = false
                        codeScanner.camera = CodeScanner.CAMERA_BACK
                        codeScanner.isAutoFocusEnabled = true
                    }else{
                        isFrontFacing = true
                        codeScanner.camera = CodeScanner.CAMERA_FRONT
                        codeScanner.isAutoFocusEnabled = true
                    }

                    codeScanner.startPreview()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        try {
            GoSettingScreen.openSettingScreen(context!!)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
