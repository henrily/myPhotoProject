package snapgram.snapgram.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;

/**
 * Created by Henri on 21/12/2014.
 */
public class SnapGramFilter {

    private static Bitmap vignette(Bitmap image)
    {
        float radius = (float)(image.getWidth()/1);
        RadialGradient gradient = new RadialGradient(image.getWidth()/2, image.getHeight()/2, radius, Color.TRANSPARENT, Color.BLACK, Shader.TileMode.CLAMP);
        Canvas canvas = new Canvas(image);
        canvas.drawARGB(1, 0, 0, 0);

        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setShader(gradient);

        final Rect rect = new Rect(0,0, image.getWidth(), image.getHeight());
        final RectF rectf = new RectF(rect);

        canvas.drawRect(rectf, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(image, rect, rect, paint);

        return image;
    }

    public static Bitmap doRetroEffect(Bitmap src, double depth, double red, double blue, double green) {
        // constant factors
        final double GS_RED = 0.299;
        final double GS_GREEN = 0.587;
        final double GS_BLUE = 0.114;

        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        // pixel information
        int A, R, G, B;
        int pixel;

        // get image size
        int width = src.getWidth();
        int height = src.getHeight();

        // scan through every single pixel
        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                // get one pixel color
                pixel = src.getPixel(x, y);
                // retrieve color of all channels
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);
                // take conversion up to one single value
                //R = G = B = (int)(GS_RED * R + GS_GREEN * G + GS_BLUE * B);

                R = (int) (GS_RED * R);
                G = (int) (GS_GREEN * G);
                B = (int) (GS_BLUE * B);

                // set new pixel color to output bitmap
                R += (depth * red);
                if(R > 255) { R = 255; }

                G += (depth * green);
                if(G > 255) { G = 255; }

                B += (depth * blue);
                if(B > 255) { B = 255; }
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        bmOut = vignette(bmOut);
        // return final image
        return bmOut;
    }

}
