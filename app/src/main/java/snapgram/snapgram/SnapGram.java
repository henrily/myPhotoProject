package snapgram.snapgram;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import snapgram.snapgram.util.FileStorage;


/**
 * Created by Henri on 20/12/2014.
 */
class SnapGram extends SurfaceView implements SurfaceHolder.Callback {

    private Context mContext = null;
    private Boolean frontCamera = false;
    private SurfaceHolder mHolder = null;
    public Camera mCamera = null;
    private MediaRecorder mRecorder = null;
    private android.hardware.Camera.PictureCallback mPicture = null;
    private Boolean inPreview = false;
    private boolean cameraConfigured = false;

    /** Si une camera existe et est disponible **/
    private boolean checkCamera(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            return true;
        } else {
            return false;
        }
    }

    /** Recupération de l'instance de la caméra . */
    private  Camera getCameraInstance(){
        Camera c = null;
        try {
            if (!frontCamera)
              c = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            else
              c = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);

        }
        catch (Exception e){
            Log.e("ERROR", "Aucune caméra disponible");
        }
        return c;
    }




    /** Constructeur */
    public SnapGram(final Context context) {
        super(context);
        if (checkCamera(context) == false)
            Log.d("ERROR", "Camera failed");
        mCamera = getCameraInstance();
        if (mCamera == null)
            Log.d("ERROR", "Camera failed");

        mContext = context;
        mHolder = getHolder();
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mHolder.addCallback(this);
        mContext = context;
        mRecorder = new MediaRecorder();
    }

    public void startPreview() {
        if (cameraConfigured && mCamera != null) {
            mCamera.startPreview();
            mCamera.setDisplayOrientation(90);
            inPreview = true;
        }
    }
    public boolean takeVideo()
    {
        mCamera.unlock();
        mRecorder.setCamera(mCamera);

        mRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        mRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        mRecorder.setOutputFile(FileStorage.getOutputMediaFile(2).toString());

        mRecorder.setPreviewDisplay(this.getHolder().getSurface());

        try {
            mRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d("VIDEO", "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d("VIDEO", "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        mRecorder.start();
        return true;
    }

    public void stopVideo()
    {
        mRecorder.stop();  // stop the recording
        releaseMediaRecorder(); // release the MediaRecorder object
    }

    public void releaseMediaRecorder(){
        if (mRecorder != null) {
            mRecorder.reset();   // clear recorder configuration
            mRecorder.release(); // release the recorder object
            mRecorder = null;
            mRecorder = new MediaRecorder();
            mCamera.lock();           // lock camera for later use
        }
    }

    public void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    public void changeCamera(){
        if (frontCamera) {
            frontCamera = false;
            if (inPreview)
                mCamera.stopPreview();
            releaseCamera();
            mCamera = getCameraInstance();
            mCamera.setDisplayOrientation(90);
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();
                inPreview = true;
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            frontCamera = true;
            if (inPreview)
                mCamera.stopPreview();
            releaseCamera();
            mCamera = getCameraInstance();
            mCamera.setDisplayOrientation(90);
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();
                inPreview = true;
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Boolean getFrontCamera(){
        return frontCamera;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
            inPreview = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Camera.Size getBestPreviewSize(int width, int height,
                                           Camera.Parameters parameters) {
        Camera.Size result = null;
            width = height;
            height = width;
        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    if (size.width * size.height > resultArea) {
                        result = size;
                    }
                }
            }
        }
        return (result);
    }

    private void configCamera(int width, int height)
    {
        if (!cameraConfigured) {
            Camera.Parameters parameters = mCamera.getParameters();
            Camera.Size size = getBestPreviewSize(width, height, parameters);

            List<String> focusModes = parameters.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
            if (size != null) {
                parameters.setPreviewSize(size.width, size.height);
                mCamera.setParameters(parameters);
                cameraConfigured = true;
            }


            mCamera.setDisplayOrientation(90);
        }
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        try {
            mCamera.stopPreview();
            inPreview = false;
        } catch (Exception e) {

        }

        try {
            mCamera.setPreviewDisplay(mHolder);
            configCamera(width, height);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d("ERROR", "Error starting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

}
