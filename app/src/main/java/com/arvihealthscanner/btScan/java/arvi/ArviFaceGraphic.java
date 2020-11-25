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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;
import com.arvihealthscanner.btScan.common.GraphicOverlay;
import com.arvihealthscanner.btScan.common.GraphicOverlay.Graphic;

import static androidx.constraintlayout.widget.Constraints.TAG;

/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
public class ArviFaceGraphic extends Graphic {
    private static final float FACE_POSITION_RADIUS = 4.0f;
    private static final float ID_TEXT_SIZE = 30.0f;
    private static final float ID_Y_OFFSET = 50.0f;
    private static final float ID_X_OFFSET = -50.0f;
    private static final float BOX_STROKE_WIDTH = 10.0f;


    private final Paint facePositionPaint;
    private final Paint idPaint;
    private final Paint boxPaint;
    private final Paint blockPaint;

    private volatile FirebaseVisionFace firebaseVisionFace;
    private Bitmap overlayBitmap;
    public static final int FACE_REJECTED=-1, HUMAN_OUTLINE=0,FACE_ACCEPTED=1,TURN_LEFT=2,TURN_RIGHT=4,TILT_LEFT=5,TILT_RIGHT=6;
    private final int type;


    public ArviFaceGraphic(GraphicOverlay overlay,  Bitmap overlayBitmap,int type) {
        super(overlay);
        this.overlayBitmap = overlayBitmap;
        this.type=type;
        final int selectedColor = Color.WHITE;

        facePositionPaint = new Paint();
        facePositionPaint.setColor(selectedColor);

        idPaint = new Paint();
        idPaint.setColor(selectedColor);
        idPaint.setTextSize(ID_TEXT_SIZE);

        boxPaint = new Paint();
        boxPaint.setColor(selectedColor);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(BOX_STROKE_WIDTH);

        blockPaint = new Paint();
        blockPaint.setStyle(Paint.Style.FILL);
        blockPaint.setColor(Color.argb(200,0,0,0));
    }


    public ArviFaceGraphic(GraphicOverlay overlay,Bitmap overlayBitmap, FirebaseVisionFace face,boolean accepted) {
        super(overlay);

        firebaseVisionFace = face;
        this.overlayBitmap = overlayBitmap;
        this.type=accepted?FACE_ACCEPTED:FACE_REJECTED;
        final int selectedColor = Color.WHITE;

        facePositionPaint = new Paint();
        facePositionPaint.setColor(selectedColor);

        idPaint = new Paint();
        idPaint.setColor(selectedColor);
        idPaint.setTextSize(ID_TEXT_SIZE);

        boxPaint = new Paint();
        boxPaint.setColor(selectedColor);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(BOX_STROKE_WIDTH);

        blockPaint = new Paint();
        blockPaint.setStyle(Paint.Style.FILL);
        blockPaint.setColor(Color.argb(200,0,0,0));
    }



    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        //  Log.d(TAG,"CANVAS VALUES "+canvas.getHeight()+"");
        FirebaseVisionFace face = firebaseVisionFace;

        float left,top,right,bottom;
        Rect rect;
        switch (this.type)
        {
            case FACE_ACCEPTED:
                try {
                    if (face == null) {
                        return;
                    }
                    rect=ArviFaceDetectionProcessor.fixedBox;
                    left = translateX(rect.left);
                    top = translateY(rect.top);
                    ;
                    right = translateX(rect.right);
                    bottom = translateY(rect.bottom);
                    boxPaint.setColor(Color.GREEN);
                    canvas.drawRect(left,top,right,bottom, boxPaint);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case FACE_REJECTED:
                try {
                    if (face == null) {
                        return;
                    }
                    rect=ArviFaceDetectionProcessor.fixedBox;
                    left = translateX(rect.left);
                    top = translateY(rect.top);
                    ;
                    right = translateX(rect.right);
                    bottom = translateY(rect.bottom);
                    boxPaint.setColor(Color.RED);
                    canvas.drawRect(left,top,right,bottom, boxPaint);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case HUMAN_OUTLINE:
                try {
                    if (overlayBitmap == null) {
                        return;
                    }
                    rect=ArviFaceDetectionProcessor.fixedBox;
                    left = translateX(rect.left);
                    top = translateY(rect.top);
                    ;
                    right = translateX(rect.right);
                    bottom = translateY(rect.bottom);
                    boxPaint.setColor(Color.WHITE);
                    canvas.drawRect(left,top,right,bottom, boxPaint);
                    for(int i=0;i<4;i++) {
                        rect = ArviFaceDetectionProcessor.fixedBlocks[i];
                        left = translateX(rect.left);
                        top = translateY(rect.top);
                        right = translateX(rect.right);
                        bottom = translateY(rect.bottom);
                        canvas.drawRect(left, top, right, bottom, blockPaint);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case TURN_RIGHT:
                try {
                    if (overlayBitmap == null) {
                        return;
                    }
                    rect=ArviFaceDetectionProcessor.fixedBox;
                    left = translateX(rect.left);
                    top = translateY(rect.top);
                    //   canvas.drawBitmap(overlayBitmap,left,top,null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case TURN_LEFT:
                try {
                    if (overlayBitmap == null) {
                        return;
                    }
                    rect=ArviFaceDetectionProcessor.fixedBox;
                    left = translateX(rect.right);
                    top = translateY(rect.top);
                    //    canvas.drawBitmap(overlayBitmap,left,top,null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case TILT_LEFT:
                try {
                    if (overlayBitmap == null) {
                        return;
                    }
                    rect=new Rect(0,0,200,800);
                    left = translateX(rect.left);
                    top = translateY(rect.top);
                    //      canvas.drawBitmap(overlayBitmap,left,top,null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case TILT_RIGHT:
                try {
                    if (overlayBitmap == null) {
                        return;
                    }
                    rect=ArviFaceDetectionProcessor.fixedBox;
                    left = translateX(rect.left);
                    top = translateY(rect.top);
                    //      canvas.drawBitmap(overlayBitmap,0,top,null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }
    public void drawold(Canvas canvas) {
        //  Log.d(TAG,"CANVAS VALUES "+canvas.getHeight()+"");
        FirebaseVisionFace face = firebaseVisionFace;

        float left,top,right,bottom;
        Rect rect;
        switch (this.type)
        {
            case FACE_ACCEPTED:
                try {
                    if (face == null) {
                        return;
                    }
                    rect = face.getBoundingBox();
                    left = translateX(rect.left);
                    top = translateY(rect.top);
                    right = translateX(rect.right);
                    bottom = translateY(rect.bottom);
                    boxPaint.setColor(Color.GREEN);
                    canvas.drawRect(left, top, right, bottom, boxPaint);

                    rect=ArviFaceDetectionProcessor.fixedBox;
                    left = translateX(rect.left);
                    top = translateY(rect.top);
                    ;
                    right = translateX(rect.right);
                    bottom = translateY(rect.bottom);
                    //canvas.drawBitmap(overlayBitmap,null,new RectF(left,top,right,bottom),null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case FACE_REJECTED:
                try {
                    if (face == null) {
                        return;
                    }
                    rect = face.getBoundingBox();
                    left = translateX(rect.left);
                    top = translateY(rect.top);
                    right = translateX(rect.right);
                    bottom = translateY(rect.bottom);
                    boxPaint.setColor(Color.RED);
                    canvas.drawRect(left, top, right, bottom, boxPaint);

                    rect=ArviFaceDetectionProcessor.fixedBox;
                    left = translateX(rect.left);
                    top = translateY(rect.top);
                    ;
                    right = translateX(rect.right);
                    bottom = translateY(rect.bottom);
                    //canvas.drawBitmap(overlayBitmap,null,new RectF(left,top,right,bottom),null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case HUMAN_OUTLINE:
                try {
                    if (overlayBitmap == null) {
                        return;
                    }
                    rect=ArviFaceDetectionProcessor.fixedBox;
                    left = translateX(rect.left);
                    top = translateY(rect.top);
                    ;
                    right = translateX(rect.right);
                    bottom = translateY(rect.bottom);
                    //    Log.d(TAG,"fixed box "+left+","+top+","+right+","+bottom); //try!!!!!11

                    boxPaint.setColor(Color.WHITE);
                    canvas.drawRect(left,top,right,bottom, boxPaint);
                    //canvas.drawBitmap(overlayBitmap,null,new RectF(left,top,right,bottom),null);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            case TURN_RIGHT:
                try {
                    if (overlayBitmap == null) {
                        return;
                    }
                    rect=ArviFaceDetectionProcessor.fixedBox;
                    left = translateX(rect.left);
                    top = translateY(rect.top);
                    //   canvas.drawBitmap(overlayBitmap,left,top,null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case TURN_LEFT:
                try {
                    if (overlayBitmap == null) {
                        return;
                    }
                    rect=ArviFaceDetectionProcessor.fixedBox;
                    left = translateX(rect.right);
                    top = translateY(rect.top);
                    //    canvas.drawBitmap(overlayBitmap,left,top,null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case TILT_LEFT:
                try {
                    if (overlayBitmap == null) {
                        return;
                    }
                    rect=new Rect(0,0,200,800);
                    left = translateX(rect.left);
                    top = translateY(rect.top);
                    //      canvas.drawBitmap(overlayBitmap,left,top,null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case TILT_RIGHT:
                try {
                    if (overlayBitmap == null) {
                        return;
                    }
                    rect=ArviFaceDetectionProcessor.fixedBox;
                    left = translateX(rect.left);
                    top = translateY(rect.top);
                    //      canvas.drawBitmap(overlayBitmap,0,top,null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private void drawLandmarkPosition(Canvas canvas, FirebaseVisionFace face, int landmarkID) {
        try {
            FirebaseVisionFaceLandmark landmark = face.getLandmark(landmarkID);
            if (landmark != null) {
                FirebaseVisionPoint point = landmark.getPosition();
                canvas.drawCircle(
                        translateX(point.getX()),
                        translateY(point.getY()),
                        10f, idPaint);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drawBitmapOverLandmarkPosition(Canvas canvas, FirebaseVisionFace face, int landmarkID) {
        try {
            FirebaseVisionFaceLandmark landmark = face.getLandmark(landmarkID);
            if (landmark == null) {
                return;
            }

            FirebaseVisionPoint point = landmark.getPosition();

            if (overlayBitmap != null) {
                float imageEdgeSizeBasedOnFaceSize = (face.getBoundingBox().width() / 4.0f);

                int left = (int) (translateX(point.getX()) - imageEdgeSizeBasedOnFaceSize);
                int top = (int) (translateY(point.getY()) - imageEdgeSizeBasedOnFaceSize);
                int right = (int) (translateX(point.getX()) + imageEdgeSizeBasedOnFaceSize);
                int bottom = (int) (translateY(point.getY()) + imageEdgeSizeBasedOnFaceSize);

                // canvas.drawBitmap(overlayBitmap,
                //       null,
                //     new Rect(left, top, right, bottom),
                //   null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
