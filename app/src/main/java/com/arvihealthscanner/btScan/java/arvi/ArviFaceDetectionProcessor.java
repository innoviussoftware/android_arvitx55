// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.arvihealthscanner.btScan.java.arvi;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.arvihealthscanner.R;
import com.arvihealthscanner.btScan.common.CameraImageGraphic;
import com.arvihealthscanner.btScan.common.FrameMetadata;
import com.arvihealthscanner.btScan.common.GraphicOverlay;
import com.arvihealthscanner.btScan.java.VisionProcessorBase;

import java.io.IOException;
import java.util.List;

/**
 * Face Detector Demo.
 */
public class ArviFaceDetectionProcessor extends VisionProcessorBase<List<FirebaseVisionFace>> {
	private static final String TAG = "FaceDetectionProcessor";
	private final FirebaseVisionFaceDetector detector;
	private Bitmap humanOutline,humanOutline_green,humanOutline_red,leftArrow,rightArrow,clockwiseArrow,antiClockwiseArrow;

	private static  final int DEFAULT_SCREEN=1,DETECTION_SCREEN=2;
	private  final int mode;
	public static Rect fixedBox=null;
	public static Rect []  fixedBlocks=new Rect[4];
	private FaceDetectionListener listener;

	private ArviFaceDetectionProcessor(int mode, FaceDetectionListener listener ) {
		this.mode=mode;
		FirebaseVisionFaceDetectorOptions options =
				new FirebaseVisionFaceDetectorOptions.Builder()
						.setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
						.setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
						.build();
		this.detector = FirebaseVision.getInstance().getVisionFaceDetector(options);
		this.listener=listener;
	}
	public static ArviFaceDetectionProcessor getForDefaultScreen(FaceDetectionListener listener)
	{
		ArviFaceDetectionProcessor fdp=new ArviFaceDetectionProcessor(DEFAULT_SCREEN,listener);
		return fdp;
	}
	public static ArviFaceDetectionProcessor getForDetectionScreen(Resources resources, FaceDetectionListener listener)
	{
		ArviFaceDetectionProcessor fdp=new ArviFaceDetectionProcessor(DETECTION_SCREEN,listener);
		fdp.humanOutline = BitmapFactory.decodeResource(resources, R.drawable.orange_outline);
		fdp.humanOutline_red = BitmapFactory.decodeResource(resources, R.drawable.red_outline);
		fdp.humanOutline_green = BitmapFactory.decodeResource(resources, R.drawable.green_outline);
		fdp.leftArrow = BitmapFactory.decodeResource(resources, R.drawable.image_left);
		fdp.rightArrow = BitmapFactory.decodeResource(resources, R.drawable.image_left);
		fdp.clockwiseArrow = BitmapFactory.decodeResource(resources, R.drawable.clown_nose);
		fdp.antiClockwiseArrow = BitmapFactory.decodeResource(resources, R.drawable.clown_nose);

		return fdp;
	}
	private void makeRectangle(int w,int h)
	{
		try {
			if(fixedBox==null)
			{
				int left,top,right,bottom;


				left=Math.round(w*Config.fixedBoxLeftMargin/100f);
				top= Math.round(h*Config.fixedBoxTopMargin/100f);
				right=Math.round(left+ (w*Config.fixedBoxWidth/100f));
				bottom=Math.round(top+ (h*Config.fixedBoxHeight/100f));

				//	bottom=Math.round(top+ (right-left));
				fixedBox=new Rect(left,top,right,bottom);

				fixedBlocks[0]=new Rect(0,0,w,top);
				fixedBlocks[1]=new Rect(right,top,w,bottom);
				fixedBlocks[2]=new Rect(0,bottom,w,h);
				fixedBlocks[3]=new Rect(0,top,left,bottom);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	public void stop() {
		try {
			detector.close();
		} catch (IOException e) {
			Log.e(TAG, "Exception thrown while trying to close Face Detector: " + e);
		}
	}

	@Override
	protected Task<List<FirebaseVisionFace>> detectInImage(FirebaseVisionImage image) {
		return detector.detectInImage(image);
	}

	@Override
	protected void onSuccess(
			@Nullable Bitmap originalCameraImage,
			@NonNull List<FirebaseVisionFace> faces,
			@NonNull FrameMetadata frameMetadata,
			@NonNull GraphicOverlay graphicOverlay) {
		//Log.d(TAG,"BITMAP WIDTH"+originalCameraImage.getWidth()+"HEIGHT "+originalCameraImage.getHeight());
		//Log.d(TAG,"GRAPHIC OVERLAY Preview WIDTH"+graphicOverlay.previewWidth+"HEIGHT "+graphicOverlay.previewHeight+"widthScaleFactor: "+graphicOverlay.widthScaleFactor+"heightScaleFactor: "+graphicOverlay.heightScaleFactor);
		switch (this.mode)
		{
			case DEFAULT_SCREEN:
			{
				try {
					for (int i = 0; i < faces.size(); ++i)
					{
						FirebaseVisionFace face = faces.get(i);
						float width=(face.getBoundingBox().width()*100f)/graphicOverlay.previewWidth;
						if(width >= Config.defaultMinFaceWidth)
						{
							if(this.listener!=null)
							{
								this.listener.faceDetected(originalCameraImage);
							}
							break; //remove later
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			break;
			case DETECTION_SCREEN:
			{
				try {
					graphicOverlay.clear();
					if (originalCameraImage != null) {
						CameraImageGraphic imageGraphic = new CameraImageGraphic(graphicOverlay, originalCameraImage);
						graphicOverlay.add(imageGraphic);
					}
					makeRectangle(graphicOverlay.previewWidth,graphicOverlay.previewHeight);
					if(Config.showRectangle) {
						graphicOverlay.add(new ArviFaceGraphic(graphicOverlay, humanOutline, ArviFaceGraphic.HUMAN_OUTLINE));
					}
					for (int i = 0; i < faces.size(); ++i)
					{
						FirebaseVisionFace face = faces.get(i);
						float yAngle= face.getHeadEulerAngleY();
						float zAngle= face.getHeadEulerAngleZ();
						Rect faceBox=face.getBoundingBox();
						float width=(faceBox.width()*100f)/graphicOverlay.previewWidth;
						Log.d(TAG,"face"+i+":"+faceBox.left+","+faceBox.top+","+faceBox.right+","+faceBox.bottom+",w="+width+",y="+yAngle+",z="+zAngle);
						if(Config.showRectangle) {
							graphicOverlay.add(new ArviFaceGraphic(graphicOverlay,humanOutline_red, face,false));
						}
						if(faceBox.left>=fixedBox.left && faceBox.top>=fixedBox.top && faceBox.right<=fixedBox.right && faceBox.bottom<=fixedBox.bottom)
						{
							if(width >= Config.detectMinFaceWidth)
							{
								if(yAngle>=-Config.detectAngleY && yAngle<=Config.detectAngleY )
								{
									if(zAngle>=-Config.detectAngleZ && zAngle<=Config.detectAngleZ )
									{
										if(Config.showRectangle) {
											graphicOverlay.add(new ArviFaceGraphic(graphicOverlay,humanOutline_green, face,true));
										}
										if(this.listener!=null)
										{
											this.listener.faceDetected(originalCameraImage);
											break;
										}
									}
									else
									{
										if(zAngle<0) {
											graphicOverlay.add(new ArviFaceGraphic(graphicOverlay, clockwiseArrow, ArviFaceGraphic.TILT_RIGHT));
										}
										else
										{
											graphicOverlay.add(new ArviFaceGraphic(graphicOverlay,antiClockwiseArrow,ArviFaceGraphic.TILT_LEFT));
										}
										if(this.listener!=null)
										{
											this.listener.faceErrorZangleFailed(originalCameraImage,zAngle);
										}
									}
								}
								else
								{
									if(yAngle<0) {
										graphicOverlay.add(new ArviFaceGraphic(graphicOverlay, leftArrow, ArviFaceGraphic.TURN_LEFT));
									}
									else
									{
										graphicOverlay.add(new ArviFaceGraphic(graphicOverlay,rightArrow,ArviFaceGraphic.TURN_RIGHT));
									}
									if(this.listener!=null)
									{
										this.listener.faceErrorYangleFailed(originalCameraImage,yAngle);
									}
								}
							}
							else
							{
								if(this.listener!=null)
								{
									this.listener.faceErrorTooSmall(originalCameraImage,width);
								}
							}

						}
						else
						{
							if(this.listener!=null)
							{
								this.listener.faceErrorOutsideBox(originalCameraImage);
							}
						}

						//break; //remove later

					}
					graphicOverlay.postInvalidate();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			break;
		}


	}

	@Override
	protected void onFailure(@NonNull Exception e) {
		Log.e(TAG, "Face detection failed " + e);
	}
}
