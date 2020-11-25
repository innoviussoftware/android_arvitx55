package com.arvihealthscanner.Activity.OxiRead;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.arvihealthscanner.Activity.ScanQRCodeActivity;
import com.arvihealthscanner.R;
import com.arvihealthscanner.RetrofitApiCall.APIService;
import com.arvihealthscanner.RetrofitApiCall.ApiUtils;
import com.arvihealthscanner.SessionManager.SessionManager;
import com.arvihealthscanner.Utils.AppConstants;
import com.arvihealthscanner.Utils.GoSettingScreen;
import com.arvihealthscanner.Utils.SnackBar;
import com.arvihealthscanner.btScan.java.DafaultActivity;
import com.arvihealthscanner.btScan.java.arvi.ArviAudioPlaybacks;
import com.arvihealthscanner.btScan.java.services.SlaveListener;
import com.arvihealthscanner.btScan.java.services.SlaveService;
import com.google.gson.JsonObject;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//Todo:: Priyanka
//This screen is design for detect person finger and read oximeter parameters

public class OxiMessageActivity extends AppCompatActivity implements SlaveListener {

    private String TAG = "OxiMessageActivity ";
    private String oxiLevel;
    private Dialog dialog;
    TextView tvTitleOMA;
    ImageView gifSGA;

    private static enum STATE {UNKNOWN, INIT,WAIT_OPERATE_LED, SHOW_RESULT_SCREEN, DELAY, WAIT_FOR_EXIT, WAIT_SANITIZER_ON, WAIT_SANITIZER_OFF, READ_OXIMETER, WAIT_OXIMETER_REPLY, SHOW_OXIMETER_READING}

    ;
    private static SlaveService slaveService;
    private static boolean isServiceBound;
    private ServiceConnection serviceConnection;
    private Intent serviceIntent;
    private int stateTimeoutIn100ms,
            resultToastTimeout,
            oximeterTimeout, prevOxiLevel;
    private boolean threadRunning = false;
    private STATE state = STATE.UNKNOWN;
    private STATE previousState = STATE.UNKNOWN;
    private Toast resultToast;
    String strUserId,strTemp;
    View snackBarView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oxi_message);
        try {
            ArviAudioPlaybacks.init(this.getApplicationContext());
            snackBarView = findViewById(android.R.id.content);
            tvTitleOMA = findViewById(R.id.tvTitleOMA);
            gifSGA = findViewById(R.id.gifSGA);
            ArviAudioPlaybacks.forcePlay(R.raw.salli_insert_finger);
            isServiceBound = false;
            serviceIntent = new Intent(getApplicationContext(), SlaveService.class);
            if(getIntent().getExtras()!=null){
                strUserId = getIntent().getStringExtra("strUserId");
                strTemp = getIntent().getStringExtra("strTemp");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showOximeterToast(boolean sanitize, int oxiValue, String msg) {

        try {

            if (SessionManager.INSTANCE.getSanitizerOption(getApplicationContext()).equals("Enable")) {
                if (oxiValue > SessionManager.INSTANCE.getOxiLevel(getApplicationContext())) {
                    ArviAudioPlaybacks.forcePlay(R.raw.salli_bo_normal_san);
                    oxiLevel = "Normal";
                }else{
                    ArviAudioPlaybacks.forcePlay(R.raw.salli_bo_low_san);
                    oxiLevel = "Low";
                }
            } else {
                if (oxiValue > SessionManager.INSTANCE.getOxiLevel(getApplicationContext())) {
                    ArviAudioPlaybacks.forcePlay(R.raw.salli_bo_normal);
                    oxiLevel = "Normal";
                }else{
                    ArviAudioPlaybacks.forcePlay(R.raw.salli_bo_low);
                    oxiLevel = "Low";
                }
           }

            dialog = new Dialog(this);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.activity_oxi_result);
            TextView tvBloodRateORA = (TextView) dialog.findViewById(R.id.tvBloodRateORA);
            TextView tvStatusORA = (TextView) dialog.findViewById(R.id.tvStatusORA);
            RelativeLayout rlSanitizerOMA = (RelativeLayout) dialog.findViewById(R.id.rlSanitizerOMA);

            if (SessionManager.INSTANCE.getSanitizerOption(getApplicationContext()).equals("Enable")) {
                rlSanitizerOMA.setVisibility(View.VISIBLE);
            } else {
                rlSanitizerOMA.setVisibility(View.GONE);
            }

            tvBloodRateORA.setText(String.valueOf(oxiValue));


            if (oxiValue > SessionManager.INSTANCE.getOxiLevel(getApplicationContext())) {

                tvStatusORA.setText("Normal");
                tvBloodRateORA.setBackground(getApplicationContext().getDrawable(R.drawable.green_status));
                tvStatusORA.setTextColor(getApplicationContext().getResources().getColor(R.color.green_status));
            } else {
                tvStatusORA.setText("Low");
                tvBloodRateORA.setBackground(getApplicationContext().getDrawable(R.drawable.red_status));
                tvStatusORA.setTextColor(getApplicationContext().getResources().getColor(R.color.red));
            }

            dialog.show();
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            resultToastTimeout = 10;
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    public void stateMachine() {
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
                            slaveService.readOximeter(1);
                            stateTimeoutIn100ms = 2;
                            oximeterTimeout = 100;
                            prevOxiLevel = 127;
                            this.state = STATE.READ_OXIMETER;


                            state = STATE.READ_OXIMETER;
                        } else if (stateTimeoutIn100ms == 0) {
                            stateTimeoutIn100ms = 100;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    SnackBar.INSTANCE.showValidationError(OxiMessageActivity.this, snackBarView, "Please Check Bluetooth Connection");

                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case READ_OXIMETER:
                    try {
                        if (stateTimeoutIn100ms == 0) {
                            slaveService.readOximeter(2);
                            stateTimeoutIn100ms = 2;
                            this.state = STATE.WAIT_OXIMETER_REPLY;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case WAIT_OXIMETER_REPLY:
                    try {
                        if (SlaveService.oximeter != null) {
                            Log.d(TAG, "Checking oxi reading");
                            switch (SlaveService.oximeter[4]) {
                                case 0:
                                case 127: //finger lifted
                                    if (prevOxiLevel != 127) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                SnackBar.INSTANCE.showValidationError(OxiMessageActivity.this, snackBarView, "Place finger on oximeter");
                                            }
                                        });
                                    }
                                    stateTimeoutIn100ms = 2;
                                    this.state = STATE.READ_OXIMETER;
                                    break;
                                case 255: //auto shutdown
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SnackBar.INSTANCE.showValidationError(OxiMessageActivity.this, snackBarView, "Oximeter turned off");
                                        }
                                    });

                                    slaveService.sanitizerSpray(0, 50);
                                    stateTimeoutIn100ms = 20;
                                    this.state = STATE.SHOW_OXIMETER_READING;
                                    break;
                                default: //value
                                    if (prevOxiLevel == 127) //finger placed
                                    {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                ArviAudioPlaybacks.forcePlay(R.raw.salli_wait_comput);
                                                SnackBar.INSTANCE.showValidationError(OxiMessageActivity.this, snackBarView, "Don't move, Please wait.. Computing");
                                            }
                                        });
                                        stateTimeoutIn100ms = 20;
                                        this.state = STATE.READ_OXIMETER;
                                    } else {
                                        final int val = SlaveService.oximeter[4];
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                showOximeterToast(true, val, "Blood Oxygen");
                                                tvTitleOMA.setText("Scan Completed");
                                                gifSGA.setVisibility(View.GONE);

                                                //todo::Priyanka 06-07 start
                                                if(SessionManager.INSTANCE.getFaceRecognizeOption(getApplicationContext()).equals("ON")) {
                                                    if (strUserId != null) {
                                                        if (!strUserId.equals("")) {
                                                            callStoreRecordApi(val);
                                                        } else {
                                                            strUserId = "";
                                                            callStoreRecordApi(val);
                                                        }
                                                    } else {
                                                        strUserId = "";
                                                        callStoreRecordApi(val);
                                                    }
                                                }
                                                //todo:: 06-07 end
    //                                            SnackBar.INSTANCE.showValidationError(OxiMessageActivity.this, snackBarView, "Scan Completed");
                                            }
                                        });
                                        slaveService.sanitizerSpray(0, 50);
                                        stateTimeoutIn100ms = 20;
                                        this.state = STATE.SHOW_OXIMETER_READING;
                                    }
                                    break;
                            }
                            prevOxiLevel = SlaveService.oximeter[4];
                        } else if (stateTimeoutIn100ms == 0) {
                            try {
                                Log.d(TAG, "oxi reading failed");
                                stateTimeoutIn100ms = 2;
                                this.state = STATE.READ_OXIMETER;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (oximeterTimeout == 0) {
                            try {
                                Log.d(TAG, "oxi reading timeout");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        SnackBar.INSTANCE.showValidationError(OxiMessageActivity.this, snackBarView, "Oximeter reading timeout");
                                    }
                                });
                                prevOxiLevel = 255;
                                //                        slaveService.sanitizerSpray(0, 50);
                                stateTimeoutIn100ms = 20;
                                this.state = STATE.SHOW_OXIMETER_READING;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case SHOW_OXIMETER_READING:
                    try {
                        if (prevOxiLevel != 255 && resultToastTimeout == 0) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    resultToastTimeout = 10;
                                    //                            showOximeterToast(true, prevOxiLevel, "Blood Oxigen");
                                }
                            });
                        }
                        if (stateTimeoutIn100ms == 0) {
                            DafaultActivity.setNextFaceTimeout(3);
                            if (SessionManager.INSTANCE.getSanitizerOption(getApplicationContext()).equals("Enable")) {
                                stateTimeoutIn100ms = slaveService.sanitizerOn();
                                this.state = STATE.WAIT_SANITIZER_ON;
                            } else {
                                stateTimeoutIn100ms = 10;
                                if (oxiLevel != null && oxiLevel.equals("Normal")) {
                                    stateTimeoutIn100ms = slaveService.greenLEDblink(12, 8, 3);
                                } else {
                                    stateTimeoutIn100ms = slaveService.redLEDblink(12, 8, 3);
                                }
                                this.state = STATE.WAIT_OPERATE_LED;
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (resultToast != null)
                                        resultToast.cancel();
                                }
                            });

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case WAIT_SANITIZER_ON:
                    try {
                        if (stateTimeoutIn100ms == 0) {
                            if (oxiLevel != null && oxiLevel.equals("Normal")) {
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

                            if (SessionManager.INSTANCE.getSanitizerOption(getApplicationContext()).equals("Enable")) {
                                stateTimeoutIn100ms = slaveService.sanitizerOff();
                                this.state = STATE.WAIT_SANITIZER_OFF;
                            } else {
                                stateTimeoutIn100ms = 10;
                                this.state = STATE.WAIT_FOR_EXIT;
                            }
                        }

                    if (resultToastTimeout == 0) {
                        resultToastTimeout = 5;
                        Log.d(TAG, "repeat toast");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //showToast(tempNormal,temperature,message);
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
                            stateTimeoutIn100ms = 10;
                            this.state = STATE.WAIT_FOR_EXIT;

                            Log.d(TAG, "repeat toast");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // showToast(tempNormal,temperature,message);
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case SHOW_RESULT_SCREEN:
                    try {
                        if (stateTimeoutIn100ms == 0) {
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case DELAY:
                    try {
                        if (stateTimeoutIn100ms == 0) {
                            stateTimeoutIn100ms = 100;
                            this.state = STATE.INIT;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case WAIT_FOR_EXIT:
                    try {
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
                        } else {
                            Intent i = new Intent(getApplicationContext(), DafaultActivity.class);
                            startActivity(i);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                default: {
                    try {
                        stateTimeoutIn100ms = 100;

                        state = STATE.INIT;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callStoreRecordApi(int val) {
        try {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("userId", strUserId);
            jsonObject.addProperty("oxygenSaturation", String.valueOf(val));

            //todo::Priyanka 06-07 start
            jsonObject.addProperty("temperature", strTemp);
            //todo:: 06-07 end

            Log.e("storeBO:-",jsonObject.toString());

            APIService mAPIService = null;
            mAPIService = ApiUtils.INSTANCE.getApiService();
            Context context = OxiMessageActivity.this;
            Call<ResponseBody> call = mAPIService.recordUserTemperature(AppConstants.INSTANCE.getBEARER_TOKEN() +  SessionManager.INSTANCE.getToken(context),"application/json", jsonObject);
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
                            slaveService.addListener(OxiMessageActivity.this);
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
    public void processResponseFromSlave(byte[] data) {
        Log.d(TAG, "processResponseFromSlave");
    }

    @Override
    public void every100ms() {
        try {
            if (this.stateTimeoutIn100ms > 0) {
                this.stateTimeoutIn100ms--;
            }
            if (resultToastTimeout > 0) {
                resultToastTimeout--;
            }
            if (oximeterTimeout > 0) {
                oximeterTimeout--;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onBackPressed() {
        try {
            GoSettingScreen.INSTANCE.openSettingScreen(OxiMessageActivity.this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
