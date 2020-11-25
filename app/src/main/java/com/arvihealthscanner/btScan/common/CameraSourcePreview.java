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
package com.arvihealthscanner.btScan.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import com.google.android.gms.common.images.Size;
import com.arvihealthscanner.btScan.common.preference.PreferenceUtils;

import java.io.IOException;

/**
 * Preview the camera image in the screen.
 */
public class CameraSourcePreview extends ViewGroup {
    private static final String TAG = "MIDemoApp:Preview";

    private final Context context;
    private final SurfaceView surfaceView;
    private boolean startRequested;
    private boolean surfaceAvailable;
    private CameraSource cameraSource;

    private GraphicOverlay overlay;

    public CameraSourcePreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        startRequested = false;
        surfaceAvailable = false;

        surfaceView = new SurfaceView(context);
        surfaceView.getHolder().addCallback(new SurfaceCallback());
        addView(surfaceView);
    }

    private void start(CameraSource cameraSource) throws IOException {
        try {
            if (cameraSource == null) {
                stop();
            }

            this.cameraSource = cameraSource;

            if (this.cameraSource != null) {
                startRequested = true;
                startIfReady();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start(CameraSource cameraSource, GraphicOverlay overlay) throws IOException {
        try {
            this.overlay = overlay;
            start(cameraSource);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            if (cameraSource != null) {
                cameraSource.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void release() {
        try {
            if (cameraSource != null) {
                cameraSource.release();
                cameraSource = null;
            }
            surfaceView.getHolder().getSurface().release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    private void startIfReady() throws IOException {
        try {
            if (startRequested && surfaceAvailable) {
                if (PreferenceUtils.isCameraLiveViewportEnabled(context)) {
                    cameraSource.start(surfaceView.getHolder());
                } else {
                    cameraSource.start();
                }
                requestLayout();

                if (overlay != null) {
                    Size size = cameraSource.getPreviewSize();
                    int min = Math.min(size.getWidth(), size.getHeight());
                    int max = Math.max(size.getWidth(), size.getHeight());
                    if (isPortraitMode()) {
                        // Swap width and height sizes when in portrait, since it will be rotated by
                        // 90 degrees
                        overlay.setCameraInfo(min, max, cameraSource.getCameraFacing());
                    } else {
                        overlay.setCameraInfo(max, min, cameraSource.getCameraFacing());
                    }
                    overlay.clear();
                }
                startRequested = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class SurfaceCallback implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder surface) {
            surfaceAvailable = true;
            try {
                startIfReady();
            } catch (IOException e) {
                Log.e(TAG, "Could not start camera source.", e);
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surface) {
            surfaceAvailable = false;
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        try {
            int width = 320;
            int height = 240;
            if (cameraSource != null) {
                Size size = cameraSource.getPreviewSize();
                if (size != null) {
                    width = size.getWidth();
                    height = size.getHeight();
                }
            }

            // Swap width and height sizes when in portrait, since it will be rotated 90 degrees
            if (isPortraitMode()) {
                int tmp = width;
                width = height;
                height = tmp;
            }

            final int layoutWidth = right - left;
            final int layoutHeight = bottom - top;

            // Computes height and width for potentially doing fit width.
            int childWidth = layoutWidth;
            int childHeight = (int) (((float) layoutWidth / (float) width) * height);

            // If height is too tall using fit width, does fit height instead.
            if (childHeight > layoutHeight) {
                childHeight = layoutHeight;
                childWidth = (int) (((float) layoutHeight / (float) height) * width);
            }

            for (int i = 0; i < getChildCount(); ++i) {
                getChildAt(i).layout(0, 0, childWidth, childHeight);
                Log.d(TAG, "Assigned view: " + i);
            }

            try {
                startIfReady();
            } catch (IOException e) {
                Log.e(TAG, "Could not start camera source.", e);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isPortraitMode() {
        int orientation = context.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return false;
        }
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            return true;
        }

        Log.d(TAG, "isPortraitMode returning false by default");
        return false;
    }
}
