package snapgram.snapgram;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Filter;

import snapgram.snapgram.util.FileStorage;

/**
 * Created by Henri on 20/12/2014.
 */
public class SnapGramActivity extends Activity {

    private static final int CAMERA_REQUEST = 1888;
    private SnapGram snapGram;
    private boolean isRecording = false;
    private Context context;

    private void HideNotificationBar() {
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        // Remember that you should never show the action bar if the
        // status bar is hidden, so hide that too if necessary.
        ActionBar actionBar = getActionBar();
        if (getActionBar() != null)
            actionBar.hide();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (snapGram.mCamera != null) {
            snapGram.mCamera.release();
            snapGram.mCamera = null;
        }
        snapGram.mCamera = Camera.open(0);
        snapGram.startPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        snapGram.releaseMediaRecorder();
        snapGram.releaseCamera();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        HideNotificationBar();

        // Create our Preview view and set it as the content of our activity.
        snapGram = new SnapGram(this);
        context = this;
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(snapGram);

        final ImageButton captureButton = (ImageButton) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        //String pictureFilePath = snapGram.takePicture();

                        snapGram.mCamera.takePicture(null, null, new Camera.PictureCallback()
                        {
                            @Override
                            public void onPictureTaken(byte[] arg0, Camera arg1) {
//                                arg1.startPreview();
                                File pictureFile = FileStorage.getOutputMediaFile(1);
                                try {
                                    FileOutputStream fos = new FileOutputStream(pictureFile.getPath());
                                    fos.write(arg0);
                                    fos.close();
                                    FileStorage.updateGallery(pictureFile, context);
                                    if (pictureFile.getPath() != null)
                                    {
                                        Intent intent = new Intent(SnapGramActivity.this, FilterActivity.class);
                                        intent.putExtra("EXTRA_FILE", pictureFile.getPath());
                                        startActivity(intent);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }});

                        Toast.makeText(SnapGramActivity.this, "Photo Taken. Apply filter...", Toast.LENGTH_SHORT).show();
                        //snapGram.restartPreview();
                    }
                }
        );

        final ImageButton frontbackButton = (ImageButton) findViewById(R.id.frontbackbutton);
        final ImageButton videoCapture = (ImageButton) findViewById(R.id.button);
        videoCapture.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isRecording == false) {
                            isRecording = snapGram.takeVideo();
                            frontbackButton.setEnabled(false);
                            captureButton.setEnabled(false);
                            videoCapture.setImageResource(R.drawable.onpausemovie);
                            Toast.makeText(SnapGramActivity.this, "Video begin recording", Toast.LENGTH_SHORT).show();
                        } else {
                            snapGram.stopVideo();
                            isRecording = false;
                            frontbackButton.setEnabled(true);
                            captureButton.setEnabled(true);
                            videoCapture.setImageResource(R.drawable.onplaymovie);
                            Toast.makeText(SnapGramActivity.this, "Video end recording", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        frontbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snapGram.changeCamera();
                if (snapGram.getFrontCamera()) {
                    videoCapture.setEnabled(false);
                    Toast.makeText(SnapGramActivity.this, "Front camera", Toast.LENGTH_SHORT).show();
                }
                else {
                    videoCapture.setEnabled(true);
                    Toast.makeText(SnapGramActivity.this, "Back camera", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}