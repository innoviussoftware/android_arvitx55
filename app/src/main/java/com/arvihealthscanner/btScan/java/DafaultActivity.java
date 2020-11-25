package com.arvihealthscanner.btScan.java;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.location.Address;
import android.location.Geocoder;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.arvihealthscanner.R;
import com.arvihealthscanner.Utils.GifImageView;
import com.arvihealthscanner.Utils.GoSettingScreen;
import com.arvihealthscanner.Utils.SingleShotLocationProvider;
import com.arvihealthscanner.btScan.common.CameraSource;
import com.arvihealthscanner.btScan.common.CameraSourcePreview;
import com.arvihealthscanner.btScan.common.GraphicOverlay;
import com.arvihealthscanner.btScan.java.arvi.ArviFaceDetectionProcessor;
import com.arvihealthscanner.btScan.java.arvi.FaceDetectionListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class DafaultActivity extends AppCompatActivity {
    //final MediaPlayer mp = MediaPlayer.create(this, R.raw.ding);

    private static String TAG="Default Class : ";
    private static final int PERMISSION_REQUESTS = 1;
    private CameraSource cameraSource = null;
    private static boolean faceDetected=false;
    // ArviAudioPlaybacks avp;
    private static int faceLockTimeout;
    private static Timer timer;
    GraphicOverlay defaultPreviewOverlay;
    CameraSourcePreview defaultPreview;
    GifImageView gifSGA;

    public static void setNextFaceTimeout(int timeSec)
    {
        faceLockTimeout=timeSec*10;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dafault);
        try {
            defaultPreview = findViewById(R.id.defaultPreview);
            defaultPreviewOverlay = findViewById(R.id.defaultPreviewOverlay);
            gifSGA = findViewById(R.id.gifSGA);
            gifSGA.setGifImageResource(R.drawable.loading_gif);

            if (allPermissionsGranted()) {
                createCameraSource();
                startCameraSource();
            } else {
                getRuntimePermissions();
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
            if(requestCode == PERMISSION_REQUESTS){
                if (allPermissionsGranted()) {
                    //createCameraSource(selectedModel);
                    createCameraSource();
                    //cameraSource.setMachineLearningFrameProcessor(new FaceDetectionProcessor(getResources()));
                    startCameraSource();
                    //       avp.playAudio(avp.PLAY_WELCOME);
                } else {
                    getRuntimePermissions();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createCameraSource() {
        // If there's no existing cameraSource, create one.
        try {
            if (cameraSource == null) {
                cameraSource = new CameraSource(this, defaultPreviewOverlay);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            cameraSource.setMachineLearningFrameProcessor(ArviFaceDetectionProcessor.getForDefaultScreen(new FaceDetectionListener() {
                @Override
                public void faceDetected(Bitmap face) {
                    try {
                        if(faceLockTimeout==0) {
                            if (!faceDetected) {
                                faceDetected = true;
                                startFaceCaptureActivity();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void faceErrorYangleFailed(Bitmap face, float yAngle) {

                }

                @Override
                public void faceErrorZangleFailed(Bitmap face, float zAngle) {

                }

                @Override
                public void faceErrorOutsideBox(Bitmap face) {

                }

                @Override
                public void faceErrorTooSmall(Bitmap face, float width) {

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
    }

    private void startFaceCaptureActivity() {
        try {
            cameraSource.stop();
            Log.d(TAG," FACE DETECTED ! ");
            Intent i = new Intent(getApplicationContext(), FaceCaptureActivity.class);
            startActivity(i);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startCameraSource() {
        try {
            if (cameraSource != null) {
                try {
                    if (defaultPreview == null) {
                        Log.d(TAG, "resume: Preview is null");
                    }
                    if (defaultPreviewOverlay == null) {
                        Log.d(TAG, "resume: graphOverlay is null");
                    }
                    defaultPreview.start(cameraSource, defaultPreviewOverlay);
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
    protected void onResume(){
        try {
            Log.d(TAG, "onResume");
            super.onResume();
            faceDetected=false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onStart(){
        try {
            Log.d(TAG, "onStart");
            super.onStart();
            timer=new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        if(faceLockTimeout>0)
                        {
                            faceLockTimeout--;
                            if(faceLockTimeout==0)
                            {
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            },0,100);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onStop(){
        try {
            Log.d(TAG, "onStop");
            super.onStop();
            if(timer!=null)
            {
                timer.cancel();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        GoSettingScreen.INSTANCE.openSettingScreen(DafaultActivity.this);
    }
}
