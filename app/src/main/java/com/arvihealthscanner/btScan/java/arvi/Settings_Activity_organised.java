package com.arvihealthscanner.btScan.java.arvi;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.arvihealthscanner.Activity.Enroll.AddPersonDetailActivity;
import com.arvihealthscanner.Activity.ScanQRCodeActivity;
import com.arvihealthscanner.R;
import com.arvihealthscanner.SessionManager.SessionManager;
import com.arvihealthscanner.btScan.java.DafaultActivity;

import java.util.Calendar;

public class Settings_Activity_organised extends AppCompatActivity {

    EditText defaultMinFaceWidthTb, detectMinFaceWidthTb, fixedBoxTopMarginTb, fixedBoxLeftMarginTb, fixedBoxWidthTb;
    EditText fixedBoxHeightTb, detectAngleYTb, detectAngleZTb, macAddressTb, alarmLevelTb, tempOffsetTb, etOxiLevel;
    Button doneBtn, btnAddPerson;
    Context context;
    Spinner spScreeningType;
    Spinner spOxiScanning;
    String[] screeningType = {"Facial Recognize", "QR Scanning", "Both"};
    String[] OxmScanOption = {"Disable", "Enable"};
    String[] SanitizerOption = {"Disable", "Enable"};
    String[] faceRecognizeOption = {"ON", "OFF"};
    private String strSelectedScanning = "Facial Recognize";
    TextView tvEnroll;
    private String strOximeterScanningOption = "Disable";
    String strSanitizerOption = "Enable";

    RelativeLayout rlOxiSettingSOA;
    RelativeLayout rlOxiLevel;
    RelativeLayout rlHandSanitizerOption;
    Spinner spHandSanitizerOption;

    RelativeLayout rlFaceRecognizeSOA;
    Spinner spFaceRecognizeOption;
    private String strFaceRecognizeOption = "ON";

    ImageView imgVwTime;
    TextView tvRestartTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings__organised);
        try {

            setIds();

            doneBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doneButtonAct();
                }
            });


            imgVwTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    OpenTimePickerDialog();
                }
            });
            setData();
            //todo:: priyanka start

            setScannigData();
            setOximeterData();
            setSanitizerData();
            setFaceRecognizeData();


            if (SessionManager.INSTANCE.getKioskModel(context).equals("TX99")) {
                rlOxiSettingSOA.setVisibility(View.VISIBLE);
                rlOxiLevel.setVisibility(View.VISIBLE);
            } else {
                rlOxiSettingSOA.setVisibility(View.GONE);
                rlOxiLevel.setVisibility(View.GONE);
            }

            if (SessionManager.INSTANCE.getKioskModel(context).equals("TX77") || SessionManager.INSTANCE.getKioskModel(context).equals("TX99")) {
                rlHandSanitizerOption.setVisibility(View.VISIBLE);
            } else {
                rlHandSanitizerOption.setVisibility(View.GONE);
            }

            btnAddPerson.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent intent = new Intent(context, AddPersonDetailActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
        //todo:: priyanka end
    }

    private void OpenTimePickerDialog() {
        try {

            Calendar mcurrentTime = Calendar.getInstance();
            int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
            int minute = mcurrentTime.get(Calendar.MINUTE);
            TimePickerDialog mTimePicker;
            mTimePicker = new TimePickerDialog(Settings_Activity_organised.this,R.style.DialogTheme, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                    String hour = "23";
                    String minute = "00";
                    if(selectedHour<10){
                        if(selectedHour == 0){
                            hour = "24";
                        }else {
                            hour = "0" + String.valueOf(selectedHour);
                        }
                    }else {
                        hour = String.valueOf(selectedHour);
                    }

                    if(selectedMinute<10){
                        minute = "0"+String.valueOf(selectedMinute);
                    }else {
                        minute = String.valueOf(selectedMinute);
                    }

                    tvRestartTime.setText(hour+" : "+minute);

                }
            }, hour, minute, true);//Yes 24 hour time
            mTimePicker.setTitle("Select Time");
            mTimePicker.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setFaceRecognizeData() {
        try {
            ArrayAdapter faceRecognizeAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, faceRecognizeOption);
            faceRecognizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spFaceRecognizeOption.setAdapter(faceRecognizeAdapter);
            spFaceRecognizeOption.getBackground().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);


            spFaceRecognizeOption.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
                        strFaceRecognizeOption = faceRecognizeOption[position];
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

    }

    private void setSanitizerData() {
        try {
            ArrayAdapter sanitizerAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, SanitizerOption);
            sanitizerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spHandSanitizerOption.setAdapter(sanitizerAdapter);
            spHandSanitizerOption.getBackground().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);


            spHandSanitizerOption.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
                        strSanitizerOption = SanitizerOption[position];
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

    }

    private void setOximeterData() {
        try {
            ArrayAdapter oxiScanAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, OxmScanOption);
            oxiScanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spOxiScanning.setAdapter(oxiScanAdapter);
            spOxiScanning.getBackground().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);


            spOxiScanning.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
                        strOximeterScanningOption = OxmScanOption[position];
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

    }

    private void setScannigData() {
        try {
            ArrayAdapter scanningAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, screeningType);
            scanningAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spScreeningType.setAdapter(scanningAdapter);
            spScreeningType.getBackground().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);


            spScreeningType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
                        strSelectedScanning = screeningType[position];
                        if (strSelectedScanning.equals("Facial Recognize") || strSelectedScanning.equals("Both")) {
                            tvEnroll.setVisibility(View.VISIBLE);
                            btnAddPerson.setVisibility(View.VISIBLE);
                        } else {
                            tvEnroll.setVisibility(View.GONE);
                            btnAddPerson.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }


    }

    private void setIds() {
        try {
            context = Settings_Activity_organised.this;
            tvEnroll = (TextView) findViewById(R.id.tvEnroll);

            defaultMinFaceWidthTb = (EditText) findViewById(R.id.editText2);
            detectMinFaceWidthTb = (EditText) findViewById(R.id.editText3);
            fixedBoxTopMarginTb = (EditText) findViewById(R.id.editText4);
            fixedBoxLeftMarginTb = (EditText) findViewById(R.id.editText5);
            fixedBoxWidthTb = (EditText) findViewById(R.id.editText6);
            fixedBoxHeightTb = (EditText) findViewById(R.id.editTextNumberDecimal);
            detectAngleYTb = (EditText) findViewById(R.id.editText);
            detectAngleZTb = (EditText) findViewById(R.id.editText8);
            macAddressTb = (EditText) findViewById(R.id.macAddressTb);
            alarmLevelTb = (EditText) findViewById(R.id.editText7);
            tempOffsetTb = (EditText) findViewById(R.id.editText9);
            doneBtn = (Button) findViewById(R.id.doneBtn2);
            spScreeningType = (Spinner) findViewById(R.id.spScreeningType);
            spOxiScanning = (Spinner) findViewById(R.id.spOxiScanningOption);
            etOxiLevel = (EditText) findViewById(R.id.etOxiLevel);
            btnAddPerson = (Button) findViewById(R.id.btnAddPerson);
            rlOxiSettingSOA = (RelativeLayout) findViewById(R.id.rlOxiSettingSOA);
            rlOxiLevel = (RelativeLayout) findViewById(R.id.rlOxiLevel);
            rlHandSanitizerOption = (RelativeLayout) findViewById(R.id.rlHandSanitizerOption);
            spHandSanitizerOption = (Spinner) findViewById(R.id.spHandSanitizerOption);

            rlFaceRecognizeSOA = (RelativeLayout) findViewById(R.id.rlFaceRecognizeSOA);
            spFaceRecognizeOption = (Spinner) findViewById(R.id.spFaceRecognizeOption);


            imgVwTime = (ImageView) findViewById(R.id.imgVwTime);
            tvRestartTime = (TextView) findViewById(R.id.tvRestartTime);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setData() {
        try {
            defaultMinFaceWidthTb.setText(String.valueOf(Config.defaultMinFaceWidth));
            detectMinFaceWidthTb.setText(String.valueOf(Config.detectMinFaceWidth));
            fixedBoxTopMarginTb.setText(String.valueOf(Config.fixedBoxTopMargin));
            fixedBoxLeftMarginTb.setText(String.valueOf(Config.fixedBoxLeftMargin));
            fixedBoxWidthTb.setText(String.valueOf(Config.fixedBoxWidth));
            fixedBoxHeightTb.setText(String.valueOf(Config.fixedBoxHeight));
         //   alarmLevelTb.setText(String.valueOf(Config.tempAlarm));
         //   tempOffsetTb.setText(String.valueOf(Config.tempOffset));
            detectAngleYTb.setText(String.valueOf(Config.detectAngleY));
            detectAngleZTb.setText(String.valueOf(Config.detectAngleZ));
            macAddressTb.setText(String.valueOf(Config.MAC_ADDRESS));
            etOxiLevel.setText(String.valueOf(Config.oximeterLevel));

            String restartTime = Config.restartAppTime;
            tvRestartTime.setText(restartTime.substring(0,7));

            spScreeningType.setSelection(getIndex(spScreeningType, SessionManager.INSTANCE.getScreeningMode(context)));
            spOxiScanning.setSelection(getIndex(spOxiScanning, SessionManager.INSTANCE.getOxiScanOption(context)));
            spHandSanitizerOption.setSelection(getIndex(spHandSanitizerOption, SessionManager.INSTANCE.getSanitizerOption(context)));
            spFaceRecognizeOption.setSelection(getIndex(spFaceRecognizeOption, SessionManager.INSTANCE.getFaceRecognizeOption(context)));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //private method of your class
    private int getIndex(Spinner spinner, String myString) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)) {
                return i;
            }
        }

        return 0;
    }

    public void doneButtonAct() {
        try {
            Config.defaultMinFaceWidth = Float.parseFloat(defaultMinFaceWidthTb.getText().toString());
            Config.detectMinFaceWidth = Float.parseFloat(detectMinFaceWidthTb.getText().toString());
            Config.fixedBoxTopMargin = Float.parseFloat(fixedBoxTopMarginTb.getText().toString());
            Config.fixedBoxLeftMargin = Float.parseFloat(fixedBoxLeftMarginTb.getText().toString());
            Config.fixedBoxWidth = Float.parseFloat(fixedBoxWidthTb.getText().toString());
            Config.fixedBoxHeight = Float.parseFloat(fixedBoxHeightTb.getText().toString());
            Config.detectAngleY = Float.parseFloat(detectAngleYTb.getText().toString());
            Config.detectAngleZ = Float.parseFloat(detectAngleZTb.getText().toString());
            Config.MAC_ADDRESS = macAddressTb.getText().toString();
          //  Config.tempAlarm = Float.parseFloat(alarmLevelTb.getText().toString());
          //  Config.tempOffset = Float.parseFloat(tempOffsetTb.getText().toString());
            Config.oximeterLevel = Integer.parseInt(etOxiLevel.getText().toString());
            Config.oxiScanOption = strOximeterScanningOption;
            Config.santitizerOption = strSanitizerOption;
            Config.faceRecognizeOption = strFaceRecognizeOption;
            Config.restartAppTime = tvRestartTime.getText().toString() + " : 00";
            // Intent i = new Intent(getApplicationContext(), FaceCaptureActivity.class);
            SessionManager.INSTANCE.setMacAddress(context, Config.MAC_ADDRESS);
            SessionManager.INSTANCE.setDefaultFaceWidth(context, Config.defaultMinFaceWidth);
            SessionManager.INSTANCE.setDetectFaceWidth(context, Config.detectMinFaceWidth);
            SessionManager.INSTANCE.setTopMargin(context, Config.fixedBoxTopMargin);
            SessionManager.INSTANCE.setLeftMargin(context, Config.fixedBoxLeftMargin);
            SessionManager.INSTANCE.setBoxHeight(context, Config.fixedBoxHeight);
            SessionManager.INSTANCE.setBoxWidth(context, Config.fixedBoxWidth);
            SessionManager.INSTANCE.setAngleY(context, Config.detectAngleY);
            SessionManager.INSTANCE.setAngleZ(context, Config.detectAngleZ);
            //todo:: Priyanka 27-10
          //  SessionManager.INSTANCE.setAlarmLevel(context, Config.tempAlarm);
          //  SessionManager.INSTANCE.setTempOffset(context, Config.tempOffset);
            SessionManager.INSTANCE.setSettingSeted(context, true);
            SessionManager.INSTANCE.setScreeningMode(context, strSelectedScanning);
            SessionManager.INSTANCE.setOxiScanOption(context, strOximeterScanningOption);
            SessionManager.INSTANCE.setOxiLevel(context, Config.oximeterLevel);
            SessionManager.INSTANCE.setSanitizerOption(context, Config.santitizerOption);
            SessionManager.INSTANCE.setFaceRecognizeOption(context, Config.faceRecognizeOption);
            SessionManager.INSTANCE.setRestartAppTime(context,Config.restartAppTime);
            if (strSelectedScanning != null && strSelectedScanning.equals("Facial Recognize")) {
                Intent i = new Intent(getApplicationContext(), DafaultActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                ArviFaceDetectionProcessor.fixedBox = null;
                startActivity(i);
            } else {
                Intent i = new Intent(getApplicationContext(), ScanQRCodeActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        try {
            setData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
