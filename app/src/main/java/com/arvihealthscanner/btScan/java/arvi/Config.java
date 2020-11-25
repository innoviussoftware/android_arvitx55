package com.arvihealthscanner.btScan.java.arvi;

public class Config
{
	public static final boolean showRectangle=true;
	public static float defaultMinFaceWidth=25f; //1
	public static float detectMinFaceWidth=60f;//1

	public static float fixedBoxTopMargin=20f;
	public static float fixedBoxLeftMargin=10f;
	public static float fixedBoxWidth=80f;
	public static float fixedBoxHeight=80f;

	public static float detectAngleY=12.0f;
	public static float detectAngleZ=10.0f;


	public static int detectTimeoutSec=10;
	public static int resultTimeoutSec=5;
	public static int temperatureRetries=3;

	public static Float tempIRH=150.00f;
	public static Float tempIRL=50.00f;
	//todo:: Priyanka 27-10
//	public static Float tempAlarm=99.9f;
	public static Float tempOffset=0.0f;
	public static String MAC_ADDRESS="34:DE:1A:DE:B1:8C";

	public static int oximeterLevel = 90;
	public static String oxiScanOption = "Disable";
	public static String santitizerOption = "Disable";
	public static String faceRecognizeOption = "ON";

	public static String restartAppTime= "23 : 59 : 00";
}