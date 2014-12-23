package snapgram.snapgram;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import snapgram.snapgram.util.FileStorage;
import snapgram.snapgram.util.SnapGramFilter;

/**
 * Created by Henri on 21/12/2014.
 */
public class FilterActivity extends Activity {

    private String lastFile = null;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);


        final ImageButton cancelButton = (ImageButton) findViewById(R.id.cancel);
        cancelButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FilterActivity.this.finish();
                    }
                }
        );


        final ImageButton validate = (ImageButton) findViewById(R.id.confirm);
        validate.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ImageView imgView = (ImageView) findViewById(R.id.image_view);

                        Drawable drawable = imgView.getDrawable();
                        Rect bounds = drawable.getBounds();
                        Bitmap bitmap = Bitmap.createBitmap(bounds.width(),bounds.height(), Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(bitmap);
                        drawable.draw(canvas);
                        OutputStream fos = null;
                        File newFile = FileStorage.getOutputMediaFile(1);
                        try {
                            fos = new FileOutputStream(newFile.getPath());
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos);
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(FilterActivity.this, "Image saved", Toast.LENGTH_SHORT).show();
                        FilterActivity.this.finish();
                    }
                }
        );

    }

    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO Auto-generated method stub
        super.onWindowFocusChanged(hasFocus);

        Intent intent = getIntent();
        if (intent != null) {
            ImageView img = (ImageView) findViewById(R.id.image_view);
            String filepath = intent.getStringExtra("EXTRA_FILE");
            File imgFile = new File(filepath);
            if (imgFile.exists())
            {
                // Get the dimensions of the View
                int targetW = img.getWidth();
                int targetH = img.getHeight();

                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bmOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(filepath, bmOptions);
                int photoW = bmOptions.outWidth;
                int photoH = bmOptions.outHeight;

                int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

                // Decode the image file into a Bitmap sized to fill the View
                bmOptions.inJustDecodeBounds = false;
                bmOptions.inSampleSize = scaleFactor;
                bmOptions.inPurgeable = true;

                Bitmap bmp = BitmapFactory.decodeFile(imgFile.getAbsolutePath(), bmOptions);
                Matrix matrix = new Matrix();

                matrix.postRotate(90);
                Bitmap rotatedBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
                //Bitmap operation = SnapGramFilter.doRetroEffect(rotatedBitmap, 1, 130, 30, 45);
                img.setImageBitmap(rotatedBitmap);


                int highlightColor = getResources().getColor(R.color.highlight_color_filter);
                PorterDuffColorFilter colorFilter = new PorterDuffColorFilter(highlightColor, PorterDuff.Mode.SRC_ATOP);

                img.setColorFilter(colorFilter);
            }
        }

    }

}
