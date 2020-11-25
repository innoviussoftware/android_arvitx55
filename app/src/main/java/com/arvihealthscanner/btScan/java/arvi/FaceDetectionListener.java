package com.arvihealthscanner.btScan.java.arvi;

import android.graphics.Bitmap;

public interface FaceDetectionListener
{
	public void faceDetected(Bitmap face);
	public void faceErrorYangleFailed(Bitmap face,float yAngle);
	public void faceErrorZangleFailed(Bitmap face,float yAngle);
	public void faceErrorOutsideBox(Bitmap face);
	public void faceErrorTooSmall(Bitmap face,float width);
}