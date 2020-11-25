package com.arvihealthscanner.Activity.Enroll;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.arvihealthscanner.Model.GetAddEmployeeResponse;
import com.arvihealthscanner.Model.UploadPhotoData;
import com.arvihealthscanner.Model.UploadPhotoResponse;
import com.arvihealthscanner.R;
import com.arvihealthscanner.RetrofitApiCall.APIService;
import com.arvihealthscanner.RetrofitApiCall.ApiUtils;
import com.arvihealthscanner.SessionManager.SessionManager;
import com.arvihealthscanner.Utils.AppConstants;
import com.arvihealthscanner.Utils.MyProgressDialog;
import com.arvihealthscanner.btScan.common.CameraSource;
import com.arvihealthscanner.btScan.common.CameraSourcePreview;
import com.arvihealthscanner.btScan.common.GraphicOverlay;
import com.arvihealthscanner.btScan.java.arvi.ArviAudioPlaybacks;
import com.arvihealthscanner.btScan.java.arvi.ArviFaceDetectionProcessor;
import com.arvihealthscanner.btScan.java.arvi.FaceDetectionListener;
import com.arvihealthscanner.btScan.java.arvi.Settings_Activity_organised;
import com.arvihealthscanner.btScan.java.services.SlaveListener;
import com.arvihealthscanner.btScan.java.services.SlaveService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.societyguard.Utils.FileUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class TakePersonPhotoActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, SlaveListener, View.OnClickListener {
    TextView tvInstruction;
    private static String TAG = "Preview Class";
    private static final int PERMISSION_REQUESTS = 1;
    private CameraSource cameraSource = null;
    private static boolean faceDetected = false, faceOutside = false;
    private String isDoneCapture;
    private int photoCount = 0;
    private MultipartBody.Part file1;
    private String token;
    private Bitmap newFace;
    private String strEmpId,strPhone;


    private static enum STATE {UNKNOWN, INIT, WAIT_FOR_FACE, READ_TEMP, WAIT_FOR_TEMP, WAIT_FOR_EXIT}

    private static SlaveService slaveService;
    private static boolean isServiceBound;
    private ServiceConnection serviceConnection;
    private Intent serviceIntent;
    private static boolean threadRunning = false;
    private static STATE state = STATE.UNKNOWN;
    private static STATE previousState = STATE.UNKNOWN;


    ToggleButton facingSwitch;
    GraphicOverlay facePreviewOverlay;
    CameraSourcePreview faceCapturePreview;
    ImageView img1, img2, img3, img4, img5;
    Boolean isImg1Seted = false;
    Boolean isImg2Seted = false;
    Boolean isImg3Seted = false;
    Boolean isImg4Seted = false;
    Boolean isImg5Seted = false;
    int imgCount = 0;
    String name = "";
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_person_photo);
        try {
            context = TakePersonPhotoActivity.this;
            ArviAudioPlaybacks.init(this.getApplicationContext());
            facingSwitch = findViewById(R.id.facingSwitch);
            faceCapturePreview = findViewById(R.id.faceCapturePreview);
            facePreviewOverlay = findViewById(R.id.facePreviewOverlay);
            facingSwitch.setOnCheckedChangeListener(this);
            tvInstruction = (TextView) findViewById(R.id.tvInstruction);
            tvInstruction.setOnClickListener(this);
            img1 = findViewById(R.id.img1);
            img2 = findViewById(R.id.img2);
            img3 = findViewById(R.id.img3);
            img4 = findViewById(R.id.img4);
            img5 = findViewById(R.id.img5);

            if (Camera.getNumberOfCameras() == 1) {
                //facingSwitch.setVisibility(View.GONE);
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
                name = getIntent().getStringExtra("name");
                token = SessionManager.INSTANCE.getToken(context);
                        // getIntent().getStringExtra("token");
                strPhone = getIntent().getStringExtra("mobile");
                strEmpId = getIntent().getStringExtra("employeeId");
//                String msg = "Please put your face inside border";
                //todo:: priyanka 27-10
                /*if (name != null) {
                    if (!name.isEmpty()) {
                        msg = "Hi " + name + "!\n" + msg;
                        tvInstruction.setText(msg);
                    } else {
                        tvInstruction.setText(msg);
                    }
                }*/
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try {
            if (requestCode == PERMISSION_REQUESTS) {
                if (allPermissionsGranted()) {
                    createCameraSource();
                    startCameraSource();
                } else {
                    getRuntimePermissions();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createCameraSource() {
        try {
            if (cameraSource == null) {
                cameraSource = new CameraSource(this, facePreviewOverlay);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            //todo:: priyanka 27-10

            cameraSource.setMachineLearningFrameProcessor(ArviFaceDetectionProcessor.getForDetectionScreen(getResources(), new FaceDetectionListener() {
                @Override
                public void faceDetected(Bitmap face) {

                    try {
                        newFace = face;
                       /* if (!isImg1Seted) {
                            isImg1Seted = true;
                            img1.setImageBitmap(face);
                            isDoneCapture = "front";
                            photoCount = photoCount + 1;
                            Toast.makeText(getApplicationContext(), "Photo " + photoCount + " is captured", Toast.LENGTH_SHORT).show();
                            openNextScreen(face);
                        }*/
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void faceErrorYangleFailed(Bitmap face, float yAngle) {
                    try {
                        Log.e("Angle Y: ",String.valueOf(yAngle));
                        newFace = face;
/*                        if (!isImg2Seted) {
                            isImg2Seted = true;
                            img2.setImageBitmap(face);
                            isDoneCapture = "front,right";
                            photoCount = photoCount + 1;
                            Toast.makeText(getApplicationContext(), "Photo " + photoCount + " is captured", Toast.LENGTH_SHORT).show();
                            openNextScreen(face);
                        } else if (!isImg3Seted) {
                            isImg3Seted = true;
                            img3.setImageBitmap(face);
                            isDoneCapture = "front,left";
                            photoCount = photoCount + 1;
                            Toast.makeText(getApplicationContext(), "Photo " + photoCount + " is captured", Toast.LENGTH_SHORT).show();
                            openNextScreen(face);
                        } else if (!isImg4Seted) {
                            isImg4Seted = true;
                            img4.setImageBitmap(face);
                            photoCount = photoCount + 1;
                            Toast.makeText(getApplicationContext(), "Photo " + photoCount + " is captured", Toast.LENGTH_SHORT).show();
                            openNextScreen(face);
                        }*/
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void faceErrorZangleFailed(Bitmap face, float zAngle) {
                    try {
                        newFace = face;
                        Log.e("Angle z:",String.valueOf(zAngle));
                        /*if (state.equals(STATE.WAIT_FOR_FACE)) {
                            showMessage("Keep your face straight", 50, false);
                        }*/
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void faceErrorOutsideBox(Bitmap face) {
                    try {
                        faceOutside = true;
                        if (state.equals(STATE.WAIT_FOR_FACE)) {
                          //  showMessage("Adjust your face within the box", 50, false);
                            Toast.makeText(TakePersonPhotoActivity.this, "Adjust your face within the box", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                @Override

                public void faceErrorTooSmall(Bitmap face, float width) {
                    try {
                        faceOutside = true;
                        if (state.equals(STATE.WAIT_FOR_FACE)) {
                            Toast.makeText(TakePersonPhotoActivity.this, "Come forward", Toast.LENGTH_SHORT).show();
                           // showMessage("Come forward", 50, false);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }));
                       /*cameraSource.setMachineLearningFrameProcessor(ArviFaceDetectionProcessor.getForDetectionScreen(getResources(), new FaceDetectionListener() {
                @Override
                public void faceDetected(Bitmap face) {

                    try {
                        if (!isImg1Seted) {
                            isImg1Seted = true;
                            img1.setImageBitmap(face);
                            isDoneCapture = "front";
                            photoCount = photoCount + 1;
                            Toast.makeText(getApplicationContext(), "Photo " + photoCount + " is captured", Toast.LENGTH_SHORT).show();
                            openNextScreen(face);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void faceErrorYangleFailed(Bitmap face, float yAngle) {
                    try {
                        Log.e("Angle Y: ",String.valueOf(yAngle));
                        if (!isImg2Seted) {
                            isImg2Seted = true;
                            img2.setImageBitmap(face);
                            isDoneCapture = "front,right";
                            photoCount = photoCount + 1;
                            Toast.makeText(getApplicationContext(), "Photo " + photoCount + " is captured", Toast.LENGTH_SHORT).show();
                            openNextScreen(face);
                        } else if (!isImg3Seted) {
                            isImg3Seted = true;
                            img3.setImageBitmap(face);
                            isDoneCapture = "front,left";
                            photoCount = photoCount + 1;
                            Toast.makeText(getApplicationContext(), "Photo " + photoCount + " is captured", Toast.LENGTH_SHORT).show();
                            openNextScreen(face);
                        } else if (!isImg4Seted) {
                            isImg4Seted = true;
                            img4.setImageBitmap(face);
                            photoCount = photoCount + 1;
                            Toast.makeText(getApplicationContext(), "Photo " + photoCount + " is captured", Toast.LENGTH_SHORT).show();
                            openNextScreen(face);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void faceErrorZangleFailed(Bitmap face, float zAngle) {
                    try {
                        Log.e("Angle z:",String.valueOf(zAngle));
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
                        faceOutside = true;
                        if (state.equals(STATE.WAIT_FOR_FACE)) {
                            showMessage("Come forward", 50, false);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }));*/
        } catch (Exception e) {
            Log.e(TAG, "Can not create image processor: ", e);
            Toast.makeText(
                    getApplicationContext(),
                    "Can not create image processor: " + e.getMessage(),
                    Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tvInstruction:

                if (!isImg1Seted) {
                    isImg1Seted = true;
                    img1.setImageBitmap(newFace);
                    isDoneCapture = "front";
                    photoCount = photoCount + 1;
                    Toast.makeText(getApplicationContext(), "Photo " + photoCount + " is captured", Toast.LENGTH_SHORT).show();
                    openNextScreen(newFace);
                } else  if (!isImg2Seted) {
                    isImg2Seted = true;
                    img2.setImageBitmap(newFace);
                    isDoneCapture = "front,right";
                    photoCount = photoCount + 1;
                    Toast.makeText(getApplicationContext(), "Photo " + photoCount + " is captured", Toast.LENGTH_SHORT).show();
                    openNextScreen(newFace);
                } else if (!isImg3Seted) {
                    isImg3Seted = true;
                    img3.setImageBitmap(newFace);
                    isDoneCapture = "front,left";
                    photoCount = photoCount + 1;
                    Toast.makeText(getApplicationContext(), "Photo " + photoCount + " is captured", Toast.LENGTH_SHORT).show();
                    openNextScreen(newFace);
                } else if (!isImg4Seted) {
                    isImg4Seted = true;
                    img4.setImageBitmap(newFace);
                    photoCount = photoCount + 1;
                    Toast.makeText(getApplicationContext(), "Photo " + photoCount + " is captured", Toast.LENGTH_SHORT).show();
                    openNextScreen(newFace);
                }
                break;
        }
    }


    private void openNextScreen(Bitmap face) {
        try {

            imgCount = imgCount + 1;
            Uri tempUri = getImageUri(TakePersonPhotoActivity.this, face);
            String profilePath = FileUtil.INSTANCE.getPath(TakePersonPhotoActivity.this, tempUri);
            Log.e("path ", profilePath);
            callStorePersonPicApi(profilePath);
            if (imgCount == 4) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
                builder.setCancelable(false);
                builder.setMessage("Person Enrolled Successfully");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            dialog.dismiss();
                            Intent intent = new Intent(getApplicationContext(), Settings_Activity_organised.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callStorePersonPicApi(String profilePath) {
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
            Call<UploadPhotoResponse> call = mAPIService.uploadUserPhoto(AppConstants.INSTANCE.getBEARER_TOKEN() + token, file1);
            call.enqueue(new Callback<UploadPhotoResponse>() {
                @Override
                public void onResponse(Call<UploadPhotoResponse> call, Response<UploadPhotoResponse> response) {
                    Log.e("Upload", "success");
                    ArrayList<UploadPhotoData>  alPhotoDetail = new ArrayList<>();
                    alPhotoDetail.addAll(response.body().getData());
                    callStoreWithId(alPhotoDetail);
                }

                @Override
                public void onFailure(Call<UploadPhotoResponse> call, Throwable t) {
                    Log.e("Upload", "failure");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callStoreWithId(ArrayList<UploadPhotoData> response) {
        try {
            JsonObject jsonObject = new JsonObject();
            JsonArray jsonArray = new JsonArray();
            JsonObject jsonImgObject = new JsonObject();
            String path = response.get(0).getPath();
            String mimetype =  response.get(0).getMimetype();
            String filename =  response.get(0).getFilename();


            jsonImgObject.addProperty("path",path);
            jsonImgObject.addProperty("mimetype",mimetype);
            jsonImgObject.addProperty("filename",filename);

            jsonObject.addProperty("mobile", strPhone);
            jsonObject.addProperty("email","");
            jsonObject.addProperty("employeeId",strEmpId);
            jsonObject.addProperty("name",name);

//            jsonArray.add("images",jsonImgObject);
            jsonArray.add(jsonImgObject);
            jsonObject.add("images",jsonArray);

            APIService mAPIService = null;
            mAPIService = ApiUtils.INSTANCE.getApiService();
            Context context = TakePersonPhotoActivity.this;
            Call<GetAddEmployeeResponse> call = mAPIService.addEmployee(  "application/json","Bearer "+ SessionManager.INSTANCE.getToken(context) ,jsonObject);
            call.enqueue(new Callback<GetAddEmployeeResponse>() {
                @Override
                public void onResponse(Call<GetAddEmployeeResponse> call, Response<GetAddEmployeeResponse> response) {
                    Log.e("Upload", "success");
                }

                @Override
                public void onFailure(Call<GetAddEmployeeResponse> call, Throwable t) {
                    Log.e("Upload", "failure");
                }
            });

            /*var jsonObject = JsonObject()

            jsonObject.addProperty("mobile", strPhone)
            jsonObject.addProperty("email","")
            jsonObject.addProperty("employeeId",strEmpId)
            jsonObject.addProperty("name",strName)

            var mAPIService: APIService? = null
            mAPIService = ApiUtils.apiService
            MyProgressDialog.showProgressDialog(context!!)
            mAPIService!!.addEmployee(
                    "application/json","Bearer "+ SessionManager.getToken(context!!) ,jsonObject

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
            })*/

        } catch (Exception e) {
            e.printStackTrace();
            MyProgressDialog.INSTANCE.hideProgressDialog();

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

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        try {
            Log.d(TAG, "Set facing");
            if (cameraSource != null) {
                if (isChecked) {
                    //todo:: Priyanka 27-10
                    cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
//                    cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
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
            tvInstruction.setText(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processResponseFromSlave(byte[] data) {
        Log.d(TAG, "processResponseFromSlave");
    }

    @Override
    public void every100ms() {
    }

    public void stateMachine() {

        try {
            if (state != previousState) {
                Log.d(TAG, previousState.toString() + "--->" + state.toString());
                previousState = state;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        switch (state) {
            case INIT:
                try {
                    if (SlaveService.connected) {
                        if (SlaveService.triggerTimeout == 0) {
                        } else {
                        }
                        state = STATE.WAIT_FOR_FACE;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case WAIT_FOR_FACE:
                break;
            case READ_TEMP:
                break;
            case WAIT_FOR_TEMP:
                break;
            case WAIT_FOR_EXIT:
                break;
            default: {
                state = STATE.INIT;
            }
            break;


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
                            slaveService.addListener(TakePersonPhotoActivity.this);
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
        try {
            Log.d(TAG, "onStart");
            super.onStart();
            SlaveService.serviceOn = true;
            this.state = STATE.INIT;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!threadRunning) {
            threadRunning = true;

            state = STATE.UNKNOWN;

            new Thread(new Runnable() {
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
            }).start();
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

}

