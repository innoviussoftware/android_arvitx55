package com.arvihealthscanner.btScan.java;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


import com.arvihealthscanner.Activity.LoginActivity;
import com.arvihealthscanner.Activity.ScanQRCodeActivity;
import com.arvihealthscanner.Model.DetectFaceNewResponse;
import com.arvihealthscanner.R;
import com.arvihealthscanner.RetrofitApiCall.APIService;
import com.arvihealthscanner.RetrofitApiCall.ApiUtils;
import com.arvihealthscanner.SessionManager.SessionManager;
import com.arvihealthscanner.Utils.AppConstants;
import com.arvihealthscanner.Utils.GoSettingScreen;
import com.arvihealthscanner.Utils.SingleShotLocationProvider;
import com.arvihealthscanner.btScan.common.CameraSource;
import com.arvihealthscanner.btScan.common.CameraSourcePreview;
import com.arvihealthscanner.btScan.common.GraphicOverlay;
import com.arvihealthscanner.btScan.java.arvi.ArviAudioPlaybacks;
import com.arvihealthscanner.btScan.java.arvi.ArviFaceDetectionProcessor;
import com.arvihealthscanner.btScan.java.arvi.Config;
import com.arvihealthscanner.btScan.java.arvi.FaceDetectionListener;
import com.arvihealthscanner.btScan.java.services.SlaveListener;
import com.arvihealthscanner.btScan.java.services.SlaveService;
import com.google.gson.JsonObject;
import com.societyguard.Utils.FileUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class FaceCaptureActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, SlaveListener {
    TextView tvInstruction;
    Dialog dialog;
    private static String TAG = "Face Capture";
    private static final int PERMISSION_REQUESTS = 1;
    private CameraSource cameraSource = null;
    private static boolean faceDetected = false, faceOutside = false, tempNormal = false;
    private Bitmap facebitmap;
    private boolean isApiCalled = false;
    private String strAddress = "";
    private String strCurrentDate,strCurrentTime;

    private static enum STATE {UNKNOWN, INIT, WAIT_FOR_FACE, READ_TEMP, WAIT_FOR_TEMP, WAIT_OPERATE_LED, SHOW_RESULT_SCREEN, DELAY, WAIT_FOR_EXIT, WAIT_SANITIZER_ON, WAIT_SANITIZER_OFF}

    ;

    private MultipartBody.Part file1;
    private static SlaveService slaveService;
    private static boolean isServiceBound;
    private ServiceConnection serviceConnection;
    private Intent serviceIntent;
    private int stateTimeoutIn100ms, faceLockTimeout, faceDetectTimeout, msgTimeout, tempRetry, resultToastTimeout, adjustAudioTimeout;
    private boolean threadRunning = false;
    private STATE state = STATE.UNKNOWN;
    private STATE previousState = STATE.UNKNOWN;
    private String temperature = "", message = "";
    private Toast resultToast;

    ToggleButton facingSwitch;
    GraphicOverlay facePreviewOverlay;
    CameraSourcePreview faceCapturePreview;
    String fullname;
    String strUserId = "", strUserName = "";
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_preview);
        try {
            context = FaceCaptureActivity.this;
            ArviAudioPlaybacks.init(this.getApplicationContext());
            facingSwitch = findViewById(R.id.facingSwitch);
            faceCapturePreview = findViewById(R.id.faceCapturePreview);
            facePreviewOverlay = findViewById(R.id.facePreviewOverlay);
            facingSwitch.setOnCheckedChangeListener(this);
            tvInstruction = (TextView) findViewById(R.id.tvInstruction);
            // Hide the toggle button if there is only 1 camera
            if (Camera.getNumberOfCameras() == 1) {
                facingSwitch.setVisibility(View.GONE);
            }
            if (allPermissionsGranted()) {
                createCameraSource();
                startCameraSource();
            } else {
                getRuntimePermissions();
            }

            isServiceBound = false;
            serviceIntent = new Intent(getApplicationContext(), SlaveService.class);

            if (getIntent().getExtras() != null) {
                fullname = getIntent().getStringExtra("fullname");
                strUserId = getIntent().getStringExtra("userId");
            }

            String msg = "Please put your face inside border";
            if (fullname != null) {
                if (!fullname.isEmpty()) {
                    msg = "Hi " + fullname + "!\n" + msg;
                    tvInstruction.setText(msg);
                } else {
                    tvInstruction.setText(msg);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted: " + permission);
            return true;
        }
        Log.i(TAG, "Permission NOT granted: " + permission);
        return false;
    }

    private String[] getRequiredPermissions() {
        try {
            PackageInfo info =
                    this.getPackageManager()
                            .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    private void getRuntimePermissions() {
        try {
            List<String> allNeededPermissions = new ArrayList<>();
            for (String permission : getRequiredPermissions()) {
                if (!isPermissionGranted(this, permission)) {
                    allNeededPermissions.add(permission);
                }
            }

            if (!allNeededPermissions.isEmpty()) {
                ActivityCompat.requestPermissions(
                        this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createCameraSource() {
        // If there's no existing cameraSource, create one.
        try {
            if (cameraSource == null) {
                cameraSource = new CameraSource(this, facePreviewOverlay);

            }
            try {
                cameraSource.setMachineLearningFrameProcessor(ArviFaceDetectionProcessor.getForDetectionScreen(getResources(),
                        new FaceDetectionListener() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void faceDetected(Bitmap face) {
                                try {
                                    facebitmap = face;
                                    faceDetectTimeout = 100;
                                    if (faceLockTimeout == 0) {
                                        faceDetected = true;
                                    }
                                    if (facebitmap != null) {
                                        if(!isApiCalled) {
                                            callDetectFaceAPI(facebitmap);
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }

                            @Override
                            public void faceErrorYangleFailed(Bitmap face, float yAngle) {
                                try {
                                    facebitmap = face;
                                    faceDetectTimeout = 100;
                                    if (yAngle < 0) {
                                        if (state.equals(STATE.WAIT_FOR_FACE)) {
                                            showMessage("Turn your face towards left", 50, false);
                                        }

                                    } else {
                                        if (state.equals(STATE.WAIT_FOR_FACE)) {
                                            showMessage("Turn your face towards right", 50, false);
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }

                            @Override
                            public void faceErrorZangleFailed(Bitmap face, float zAngle) {
                                try {
                                    facebitmap = face;
                                    faceDetectTimeout = 100;
                                    if (state.equals(STATE.WAIT_FOR_FACE)) {
                                        showMessage("Keep your face straight", 50, false);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }

                            @Override
                            public void faceErrorOutsideBox(Bitmap face) {
                                try {
                                    facebitmap = face;
                                    faceDetectTimeout = 100;
                                    faceOutside = true;
                                    if (state.equals(STATE.WAIT_FOR_FACE)) {
                                        showMessage("Adjust your face within the box", 50, false);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override

                            public void faceErrorTooSmall(Bitmap face, float width) {
                                try {
                                    facebitmap = face;
                                    faceDetectTimeout = 100;

                                    if (state.equals(STATE.WAIT_FOR_FACE)) {
                                        showMessage("Come forward", 50, false);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }));
            } catch (Exception e) {
                Log.e(TAG, "Can not create image processor: ", e);
                Toast.makeText(
                        getApplicationContext(),
                        "Can not create image processor: " + e.getMessage(),
                        Toast.LENGTH_LONG)
                        .show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 0, bytes);
        String path = MediaStore.Images.Media.insertImage(
                inContext.getContentResolver(),
                inImage,
                "title",
                null
        );
        return Uri.parse(path);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void callDetectFaceAPI(Bitmap face) {
        try {
            isApiCalled = true;
            Uri tempUri = getImageUri(FaceCaptureActivity.this, face);
            String profilePath = FileUtil.INSTANCE.getPath(FaceCaptureActivity.this, tempUri);
            Log.e("path ", profilePath);

            try {
                if (profilePath.isEmpty()) {

                    file1 = MultipartBody.Part.createFormData(
                            "file1", "",
                            RequestBody.create(MediaType.parse("multipart/form-data"), ""));
                } else {
                    File file = new File(profilePath);

                    file1 = MultipartBody.Part.createFormData(
                            "file1", file.getName(),
                            RequestBody.create(MediaType.parse("multipart/form-data"), file)
                    );
                }
                APIService mAPIService = null;
                mAPIService = ApiUtils.INSTANCE.getApiService();
                retrofit2.Call<DetectFaceNewResponse> call = mAPIService.detectFace(AppConstants.INSTANCE.getBEARER_TOKEN() +  SessionManager.INSTANCE.getToken(context),file1);
                call.enqueue(new Callback<DetectFaceNewResponse>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(retrofit2.Call<DetectFaceNewResponse> call, Response<DetectFaceNewResponse> response) {
                        try {
                            if  (response.code() == 200) {
                                Log.e("Upload", "success");
                                strUserId = response.body().getData().getEmployeeId();
                                strUserName = response.body().getData().getName();// response.body().getFullName();
                                fullname = strUserName;
                                Log.e("userId:-", strUserId + "");
                                showToast(tempNormal, temperature, message, strUserName);
                            }else if(response.code() == 401){
                                Intent intent =new Intent(context, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onFailure(Call<DetectFaceNewResponse> call, Throwable t) {
                        Log.e("Upload", "failure");
                        showToast(tempNormal, temperature, message,strUserName);
                        Toast.makeText(FaceCaptureActivity.this, "Not able to recognize face", Toast.LENGTH_SHORT).show();
                 //       showToast(tempNormal, temperature, message, strUserName);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
               // showToast(tempNormal, temperature, message, strUserName);
            }

        } catch (Exception e) {
            e.printStackTrace();
           // showToast(tempNormal, temperature, message, strUserName);
        }

    }

    private void startCameraSource() {
        try {
            if (cameraSource != null) {
                try {
                    if (faceCapturePreview == null) {
                        Log.d(TAG, "resume: Preview is null");
                    }
                    if (facePreviewOverlay == null) {
                        Log.d(TAG, "resume: graphOverlay is null");
                    }
                    faceCapturePreview.start(cameraSource, facePreviewOverlay);

                 /*   if (cameraSource != null) {
                        if  (cameraSource.getCameraFacing() == CameraSource.CAMERA_FACING_BACK) {
                            cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
                        }else{
                            cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
                        }
                    }*/

                } catch (IOException e) {
                    Log.e(TAG, "Unable to start camera source.", e);
                    cameraSource.release();
                    cameraSource = null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopCameraSource() {
        try {
            if (cameraSource != null) {
                faceCapturePreview.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        try {
            Log.d(TAG, "Set facing");
            if (cameraSource != null) {
                if (isChecked) {
                    cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
                } else {
                    cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
                }
            }
            faceCapturePreview.stop();
            startCameraSource();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void showMessage(String msg, int timeout, boolean priority) {
        try {
            if (msgTimeout == 0 || priority) {
                msgTimeout = timeout;
                if (fullname != null) {
                    if (!fullname.isEmpty()) {
                        msg = "Hi " + fullname + "!\n" + msg;
                        tvInstruction.setText(msg);
                    } else {
                        tvInstruction.setText(msg);
                    }
                }
            } else {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showToast(boolean result, String temp, String msg, String strUserName) {
        try {

            if (message != null && !message.equals("")) {
                if (message.equals("ENTRY DENIED")) {
                    //     ArviAudioPlaybacks.forcePlay(R.raw.salli_temp_high_denined);
                    if (SessionManager.INSTANCE.getKioskModel(getApplicationContext()).equals("TX77") ||
                            SessionManager.INSTANCE.getKioskModel(getApplicationContext()).equals("TX99")
                    ) {
                        if (SessionManager.INSTANCE.getSanitizerOption(getApplicationContext()).equals("Enable")) {
                            ArviAudioPlaybacks.forcePlay(R.raw.salli_temp_high);
                        } else {
                            ArviAudioPlaybacks.forcePlay(R.raw.salli_temp_high_denined);
                        }
                    } else {
                        ArviAudioPlaybacks.forcePlay(R.raw.salli_temp_high_denined);
                    }
                } else if (message.equals("NORMAL") || message.contains("NORMAL")) {
//                    ArviAudioPlaybacks.forcePlay(R.raw.salli_temp_normal_pass);
                    if (SessionManager.INSTANCE.getKioskModel(getApplicationContext()).equals("TX77") ||
                            SessionManager.INSTANCE.getKioskModel(getApplicationContext()).equals("TX99")
                    ) {
                        if (SessionManager.INSTANCE.getSanitizerOption(getApplicationContext()).equals("Enable")) {
                            ArviAudioPlaybacks.forcePlay(R.raw.salli_temp_normal);
                        } else {
                            ArviAudioPlaybacks.forcePlay(R.raw.salli_temp_normal_pass);
                        }
                    } else {
                        ArviAudioPlaybacks.forcePlay(R.raw.salli_temp_normal_pass);
                    }
                }
            }
/*
            dialog = new Dialog(this);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.custom_toast);
            TextView tempLbl = (TextView) dialog.findViewById(R.id.custom_toast_temp);
            TextView msgLbl = (TextView) dialog.findViewById(R.id.custom_toast_message);
            TextView tvUserName = (TextView) dialog.findViewById(R.id.tvUserName);
            RelativeLayout rlSanitizer = (RelativeLayout) dialog.findViewById(R.id.rlSanitizer);

*/
/*            if (SessionManager.INSTANCE.getKioskModel(getApplicationContext()).equals("TX77") ||
                    SessionManager.INSTANCE.getKioskModel(getApplicationContext()).equals("TX99")) {
                if (SessionManager.INSTANCE.getSanitizerOption(getApplicationContext()).equals("Enable")) {
                    rlSanitizer.setVisibility(View.VISIBLE);
                } else {
                    rlSanitizer.setVisibility(View.GONE);
                }
            } else {
                rlSanitizer.setVisibility(View.GONE);
            }*/
/*


            tempLbl.setText(temp);

            msgLbl.setText(msg);

            if (result) {
                tempLbl.setBackground(getResources().getDrawable(R.drawable.green_status));
            } else {
                tempLbl.setBackground(getResources().getDrawable(R.drawable.red_status));
            }

            if (fullname != null) {
                if (!fullname.equals("")) {
                    tvUserName.setVisibility(View.VISIBLE);
                    tvUserName.setText("Hi, " + fullname + " your temperature is:");
                } else {
                    tvUserName.setVisibility(View.GONE);
                }
            } else {
                tvUserName.setVisibility(View.GONE);
            }
            dialog.show();
*/
            dialog = new Dialog(this);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.dialog_result);
            TextView tvUserName = (TextView) dialog.findViewById(R.id.tvUserName);
            TextView tvEmpIdDR = (TextView) dialog.findViewById(R.id.tvEmpIdDR);
            TextView tvDateDR = (TextView) dialog.findViewById(R.id.tvDateDR);
            TextView tvTimeDR = (TextView) dialog.findViewById(R.id.tvTimeDR);
            TextView tvLocationDR = (TextView) dialog.findViewById(R.id.tvLocationDR);
            ImageView imgVwStatusDR = (ImageView) dialog.findViewById(R.id.imgVwStatusDR);
            TextView tvMessageDR = (TextView) dialog.findViewById(R.id.tvMessageDR);
            ImageView imgVwPhotoDR = (ImageView)dialog.findViewById(R.id.imgVwPhotoDR);

            if (facebitmap!=null){
                imgVwPhotoDR.setImageBitmap(facebitmap);
            }

            DateTimeFormatter df = DateTimeFormatter.ofPattern("dd MMM yyyy");
            DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm a");
            LocalDateTime now = LocalDateTime.now();
            System.out.println(df.format(now));
            tvDateDR.setText("Date: " + df.format(now));
            tvTimeDR.setText("Time: " + tf.format(now));

            strCurrentDate = df.format(now);
            strCurrentTime = tf.format(now);

            if  (strUserName!=null && !strUserName.equals("")){
                tvUserName.setText(strUserName);
            }
            if (strUserId!=null  && !strUserId.equals("")){
                imgVwStatusDR.setImageDrawable(getResources().getDrawable(R.mipmap.ic_check_true));
                tvMessageDR.setText("Attendance capture successful");
            }else {
                tvUserName.setText("Unknown face");
                tvEmpIdDR.setText("Please contact administrator");
                tvDateDR.setText("");
                tvTimeDR.setText("");
                tvLocationDR.setVisibility(View.INVISIBLE);
                imgVwStatusDR.setImageDrawable(getResources().getDrawable(R.mipmap.ic_check_false));
                tvMessageDR.setText("Attendance capture failed");
            }

            SingleShotLocationProvider.requestSingleUpdate(getApplicationContext(), new SingleShotLocationProvider.LocationCallback() {
                @Override
                public void onNewLocationAvailable(SingleShotLocationProvider.GPSCoordinates location) {
                    Log.e("location:",location.latitude+" , "+location.longitude);
                    double latitude = location.latitude;
                    double longitude = location.longitude;
                    Geocoder geocoder;
                    List<Address> addresses;
                    geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                    try {
                        addresses = geocoder.getFromLocation(latitude,longitude,1);
                        String address = addresses.get(0).getAddressLine(0);
                        String city = addresses.get(0).getLocality();
                        String state = addresses.get(0).getAdminArea();
                        String country = addresses.get(0).getCountryName();
                        String postalCode = addresses.get(0).getPostalCode();
                        String knownName = addresses.get(0).getFeatureName();
                        strAddress = address;
                                /*+", "+city+", "+state+", "+country+", "+postalCode*/
                        Log.e("address:",address);
                        tvLocationDR.setText("Location: "+address);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            if (fullname != null) {
                if (!fullname.equals("")) {
                    tvUserName.setVisibility(View.VISIBLE);
                    tvUserName.setText("Hi, " + fullname + " your temperature is:");
                } else {
                    tvUserName.setVisibility(View.GONE);
                }
            } else {
                tvUserName.setVisibility(View.GONE);
            }
            if(dialog.isShowing()){

            } else {
                dialog.show();
            }
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);


            if (SessionManager.INSTANCE.getKioskModel(getApplicationContext()).equals("TX55") ||
                    SessionManager.INSTANCE.getKioskModel(getApplicationContext()).equals("TX66")) {
                resultToastTimeout = 1000;
            } else {
                if (SessionManager.INSTANCE.getSanitizerOption(getApplicationContext()).equals("Enabled")) {
                    resultToastTimeout = 1000;
                } else {
                    resultToastTimeout = 1000;
                }
            }


            //todo::Priyanka 06-07 start
/*            if (SessionManager.INSTANCE.getFaceRecognizeOption(getApplicationContext()).equals("ON")) {
                if (strUserId != null) {
                    if (!strUserId.equals("")) {
                        callStoreTempApi();
                    } else {
                        strUserId = "";
                        callStoreTempApi();
                    }
                } else {
                    strUserId = "";
                    callStoreTempApi();
                }
            }*/
            goBackScreen();
            //todo:: 06-07 end
        } catch (
                Resources.NotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void processResponseFromSlave(byte[] data) {
        Log.d(TAG, "processResponseFromSlave");
    }

    @Override
    public void every100ms() {
        try {
            if (this.stateTimeoutIn100ms > 0) {
                this.stateTimeoutIn100ms--;
            }
            if (this.faceLockTimeout > 0) {
                this.faceLockTimeout--;
            }
            if (this.faceDetectTimeout > 0) {
                this.faceDetectTimeout--;
            }
            if (msgTimeout > 0) {
                msgTimeout--;
            }
            if (resultToastTimeout > 0) {
                resultToastTimeout--;
            }
            if (adjustAudioTimeout > 0) {
                adjustAudioTimeout--;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void stateMachine() {

/*
        try {
            if (state != previousState) {
                Log.d(TAG, previousState.toString() + "--->" + state.toString());
                previousState = state;
            }
            switch (state) {
                case INIT:
                    try {
                        if (SlaveService.connected) {
                            resultToastTimeout = 0;
                            faceDetectTimeout = Config.detectTimeoutSec * 10;
                            if (SlaveService.triggerTimeout == 0) {
                                slaveService.readTemperature(20);
                                stateTimeoutIn100ms = 20;
                            } else {
                                stateTimeoutIn100ms = 0;
                            }
                            adjustAudioTimeout = 100;
                            ArviAudioPlaybacks.play(R.raw.salli_pls_adjust_face);
                            state = STATE.WAIT_FOR_FACE;
                        } else if (stateTimeoutIn100ms == 0) {
                            stateTimeoutIn100ms = 100;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showMessage("Please Check Bluetooth Connection", 50, true);
                                }
                            });
                        } else if (faceDetectTimeout == 0) {
                            this.state = STATE.WAIT_FOR_EXIT;
                            //todo:: 22-06 start
                            goBackScreen();
                            //todo:: 22-06 end
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case WAIT_FOR_FACE:
                    try {
                        if (stateTimeoutIn100ms == 0) {
                            if (faceDetected) {
                                stateTimeoutIn100ms = 2;
                                tempRetry = Config.temperatureRetries;
                                */
/*runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.e("msg:", "Don't Move. Checking Temperature");
                                        showMessage("Don't Move. Checking Temperature", 50, true);
                                    }
                                });*/
/*


                                //this.state = STATE.READ_TEMP;
                                runOnUiThread(new Runnable() {
                                    @RequiresApi(api = Build.VERSION_CODES.O)
                                    @Override
                                    public void run() {
                                        if (facebitmap != null) {
                                            if (SessionManager.INSTANCE.getFaceRecognizeOption(getApplicationContext()).equals("ON")) {
                                                callDetectFaceAPI(facebitmap);
                                            } else {
                                                showToast(tempNormal, temperature, message);
                                            }
                                        }

                                    }
                                });
                                stopCameraSource();
                                stateTimeoutIn100ms = slaveService.sanitizerOn();
                                this.state = STATE.SHOW_RESULT_SCREEN;

                            } else if (adjustAudioTimeout == 0) {
                                adjustAudioTimeout = 100;
                                if (faceOutside) {
                                    faceOutside = false;
                                    ArviAudioPlaybacks.play(R.raw.salli_pls_adjust_face);
                                }
                            } else if (faceDetectTimeout == 0) {
                                this.state = STATE.WAIT_FOR_EXIT;
                                //todo:: 22-06 start
                                goBackScreen();
                                //todo:: end
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case READ_TEMP:
                    try {
                        if (stateTimeoutIn100ms == 0) {
                            slaveService.readTemperature(20);
                            stateTimeoutIn100ms = 20;
                            this.state = STATE.WAIT_FOR_TEMP;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case WAIT_FOR_TEMP: {
                    try {
                        if (SlaveService.temperature != null) {
                            //got temp
                            Log.d(TAG, "Temperature : " + SlaveService.temperature);
                            Float tempF = Float.parseFloat(SlaveService.temperature);
                            tempF = tempF * 1.8f + 32;
                            tempF += Config.tempOffset;
                            Float temp = (float) (Math.floor(tempF * 10) / 10);
                            Log.d(TAG, "Temperature : " + SlaveService.temperature + " C=" + tempF + " F=" + temp + " F");
                            String tempStr = String.format("%.01f", temp) + " F";
                            tempNormal = false;
                            temperature = tempStr;
                            if (temp.compareTo(Config.tempIRH) > 0) {
                                temperature += " IRH";
                                if (SessionManager.INSTANCE.getKioskModel(getApplicationContext()).equals("TX55") || SessionManager.INSTANCE.getKioskModel(getApplicationContext()).equals("TX66")) {
                                    message = "ENTRY DENIED";
                                } else {
                                    message = "ENTRY DENIED";
                                }
                            } else if (temp.compareTo(Config.tempIRL) < 0) {
                                temperature += " IRL";
                                if (SessionManager.INSTANCE.getKioskModel(getApplicationContext()).equals("TX55") || SessionManager.INSTANCE.getKioskModel(getApplicationContext()).equals("TX66")) {
                                    message = "ENTRY DENIED";
                                } else {
                                    message = "ENTRY DENIED";
                                }
                            }*/
/* else if (temp.compareTo(Config.tempAlarm) > 0) {
                                if(SessionManager.INSTANCE.getKioskModel(getApplicationContext()).equals("TX55") || SessionManager.INSTANCE.getKioskModel(getApplicationContext()).equals("TX66") ){
                                    message = "ENTRY DENIED";
                                }else {
                                    message = "ENTRY DENIED";
                                }
                            } */
/*
 else {
                                tempNormal = true;
                                if (SessionManager.INSTANCE.getKioskModel(getApplicationContext()).equals("TX55") || SessionManager.INSTANCE.getKioskModel(getApplicationContext()).equals("TX66")) {
                                    message = "PLEASE PASS\n\n  NORMAL";
                                } else {
                                    message = "NORMAL";
                                }
                            }
                            runOnUiThread(new Runnable() {
                                @RequiresApi(api = Build.VERSION_CODES.O)
                                @Override
                                public void run() {
                                    if (facebitmap != null) {
                                        if (SessionManager.INSTANCE.getFaceRecognizeOption(getApplicationContext()).equals("ON")) {
                                            callDetectFaceAPI(facebitmap);
                                        } else {
                                            showToast(tempNormal, temperature, message);
                                        }
                                    }

                                }
                            });

                            */
/*if (SessionManager.INSTANCE.getKioskModel(getApplicationContext()).equals("TX77") || SessionManager.INSTANCE.getKioskModel(getApplicationContext()).equals("TX99")) {
                                if (SessionManager.INSTANCE.getSanitizerOption(getApplicationContext()).equals("Enable")) {
                                    stateTimeoutIn100ms = slaveService.sanitizerOn();
                                    this.state = STATE.WAIT_SANITIZER_ON;
                                } else {
                                    stateTimeoutIn100ms = 20;
                                    if (tempNormal) {
                                        stateTimeoutIn100ms = slaveService.greenLEDblink(12, 8, 3);
                                    } else {
                                        stateTimeoutIn100ms = slaveService.redLEDblink(12, 8, 3);
                                    }
                                    this.state = STATE.WAIT_OPERATE_LED;
                                }
                            } else {
                                stateTimeoutIn100ms = 20;
                                if (tempNormal) {
                                    stateTimeoutIn100ms = slaveService.greenLEDblink(12, 8, 3);
                                } else {
                                    stateTimeoutIn100ms = slaveService.redLEDblink(12, 8, 3);
                                }
                                this.state = STATE.WAIT_OPERATE_LED;
                            }*/
/*

                            stateTimeoutIn100ms = slaveService.sanitizerOn();
                            this.state = STATE.SHOW_RESULT_SCREEN;

                        } else if (stateTimeoutIn100ms == 0) {
                            //no response from STM
                            Log.d(TAG, "no response from slave");
                            if (tempRetry > 0)
                                tempRetry--;

                            if (tempRetry == 0) {

                                Log.d(TAG, "Temp reading fail try again");
                                stateTimeoutIn100ms = 20;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showMessage("Connection Error! Please Try again", 50, true);
                                    }
                                });
                                startCameraSource();
                                stateTimeoutIn100ms = 20;
                                this.state = STATE.DELAY;
                            } else {
                                Log.d(TAG, "Reading temperature. Retries left:" + tempRetry);
                                this.state = STATE.READ_TEMP;
                            }
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                break;
*/
/*                case WAIT_SANITIZER_ON:
                    try {
                        if (stateTimeoutIn100ms == 0) {
                            if (tempNormal) {
                                stateTimeoutIn100ms = slaveService.greenLEDblink(12, 8, 3);
                            } else {
                                stateTimeoutIn100ms = slaveService.redLEDblink(12, 8, 3);
                            }
                            this.state = STATE.WAIT_OPERATE_LED;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case WAIT_OPERATE_LED:
                    try {
                        if (stateTimeoutIn100ms == 0) {

                            if (SessionManager.INSTANCE.getKioskModel(getApplicationContext()).equals("TX77") || SessionManager.INSTANCE.getKioskModel(getApplicationContext()).equals("TX99")) {
                                if (SessionManager.INSTANCE.getSanitizerOption(getApplicationContext()).equals("Enable")) {
                                    stateTimeoutIn100ms = slaveService.sanitizerOff();
                                    this.state = STATE.WAIT_SANITIZER_OFF;
                                } else {
                                    stateTimeoutIn100ms = 5;
                                    this.state = STATE.SHOW_RESULT_SCREEN;
                                }
                            } else {
                                stateTimeoutIn100ms = 5;
                                this.state = STATE.SHOW_RESULT_SCREEN;
                            }
                        }
                        if (resultToastTimeout == 0) {
                            resultToastTimeout = 2;
                            Log.d(TAG, "repeat toast");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //todo:: 22-06 start
//                                       showToast(tempNormal,temperature,message);
                                    //todo::end
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case WAIT_SANITIZER_OFF:
                    try {
                        if (stateTimeoutIn100ms == 0) {
                            stateTimeoutIn100ms = 5;
                            this.state = STATE.SHOW_RESULT_SCREEN;

                            Log.d(TAG, "repeat toast");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //todo:: 22-06 start
//                                    showToast(tempNormal,temperature,message);
                                    //todo::end
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;*/
/*

                case SHOW_RESULT_SCREEN:
                    try {
                        if (stateTimeoutIn100ms == 0) {
                            faceDetected = false;
                            faceLockTimeout = 5;
                            DafaultActivity.setNextFaceTimeout(3);
                            this.state = STATE.WAIT_FOR_EXIT;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (resultToast != null)
                                        resultToast.cancel();
                                }
                            });
                            goBackScreen();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case DELAY:
                    try {
                        if (stateTimeoutIn100ms == 0) {
                            faceDetected = false;
                            faceLockTimeout = 10;
                            stateTimeoutIn100ms = 100;
                            faceDetectTimeout = Config.detectTimeoutSec * 10;
                            this.state = STATE.INIT;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case WAIT_FOR_EXIT:
                    break;
                default: {
                    try {
                        stateTimeoutIn100ms = 100;
                        faceDetectTimeout = Config.detectTimeoutSec * 10;
                        state = STATE.INIT;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;


            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
*/
    }

    //address, empid, date and time

    private void callStoreTempApi() {
        try {
            String strTemp = temperature.replace(" F", "");
            JsonObject jsonObject = new JsonObject();
            String empId = SessionManager.INSTANCE.getKioskID(context);
            jsonObject.addProperty("employeeId", empId);
            jsonObject.addProperty("scanDate", strCurrentDate);
            jsonObject.addProperty("scanTime", strCurrentTime);
            jsonObject.addProperty("address",strAddress);

            Log.e("storeT:", jsonObject.toString());

            APIService mAPIService = null;
            mAPIService = ApiUtils.INSTANCE.getApiService();
            Call<ResponseBody> call = mAPIService.
                    recordUserTemperature(AppConstants.INSTANCE.getBEARER_TOKEN() +  SessionManager.INSTANCE.getToken(context),"application/json",jsonObject);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Log.e("Store Temp", "success");
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("Store temp", "failure");
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void goBackScreen() {
        try {
            cameraSource.stop();
            Log.d(TAG, "GO Back ! ");
            DafaultActivity.setNextFaceTimeout(3);

            //Todo:: priyanka go for oximeter reading screen
            if (SessionManager.INSTANCE.getKioskModel(getApplicationContext()).equals("TX99") &&
                    SessionManager.INSTANCE.getOxiScanOption(getApplicationContext()).equals("Enable")) {

                try {
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                            /*String strTemp = temperature.replace(" F", "");
                            Intent intent = new Intent(FaceCaptureActivity.this, OxiMessageActivity.class);
                            intent.putExtra("strUserId",strUserId);
                            intent.putExtra("strTemp",strTemp);
                            startActivity(intent);
                            finish();*/
                            if (!SessionManager.INSTANCE.getScreeningMode(getApplicationContext()).equals("Facial Recognize")) {
                                String from = "1";
                                Intent intent = new Intent(getApplicationContext(), ScanQRCodeActivity.class);
                                intent.putExtra("from", from);
                                startActivity(intent);
                            } else {
                                Intent i = new Intent(getApplicationContext(), DafaultActivity.class);
                                startActivity(i);
                            }
                        }
                    }, 5000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            dialog.dismiss();

                            //todo::Priyanka 06-07 start

                            if (SessionManager.INSTANCE.getFaceRecognizeOption(getApplicationContext()).equals("ON")) {
                                if (strUserId != null) {
                                    if (!strUserId.equals("")) {
                                        callStoreTempApi();
                                    } else {
                                        strUserId = "";
                                        callStoreTempApi();
                                    }
                                } else {
                                    strUserId = "";
                                    callStoreTempApi();
                                }
                            }
                            //todo:: 06-07 end

                            if (dialog != null) {
                                if (dialog.isShowing()) {
                                    dialog.dismiss();
                                }
                            }
                            if (!SessionManager.INSTANCE.getScreeningMode(getApplicationContext()).equals("Facial Recognize")) {
                                String from = "1";
                                Intent intent = new Intent(getApplicationContext(), ScanQRCodeActivity.class);
                                intent.putExtra("from", from);
                                startActivity(intent);
                                finish();
                            } else {
                                Intent i = new Intent(getApplicationContext(), DafaultActivity.class);
                                startActivity(i);
                                finish();
                            }
                        }
                    },5000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void bindService() {
        try {
            Log.d(TAG, "bindService");
            if (serviceConnection == null) {
                serviceConnection = new ServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                        try {
                            SlaveService.MyServiceBinder myServiceBinder = (SlaveService.MyServiceBinder) iBinder;
                            slaveService = myServiceBinder.getService();
                            isServiceBound = true;
                            Log.d(TAG, "onServiceConnected. setting owner listener " + slaveService);
                            slaveService.addListener(FaceCaptureActivity.this);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName componentName) {
                        isServiceBound = false;
                    }
                };
            }
            bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
        try {
            SlaveService.serviceOn = true;

            resultToastTimeout = 0;
            stateTimeoutIn100ms = 100;
            faceDetectTimeout = Config.detectTimeoutSec * 10;
            this.state = STATE.INIT;
            if (!threadRunning) {
                threadRunning = true;

                state = STATE.UNKNOWN;

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "activity thread started");
                        while (threadRunning) {
                            try {
                                if (isServiceBound) {
                                    stateMachine();
                                }
                                Thread.sleep(10);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        Log.d(TAG, "thread outside while");
                    }
                });
                TAG += "(" + t.getId() + ")";
                t.start();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        try {
            startService(serviceIntent);
            bindService();
            faceDetected = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        try {
            if (slaveService != null) {
                slaveService.removeListener();
            }
            if (isServiceBound) {
                unbindService(serviceConnection);
                isServiceBound = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
        try {
            threadRunning = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onBackPressed() {
        try {
            GoSettingScreen.INSTANCE.openSettingScreen(FaceCaptureActivity.this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
