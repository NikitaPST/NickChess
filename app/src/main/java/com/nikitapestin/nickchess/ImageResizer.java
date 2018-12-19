package com.nikitapestin.nickchess;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * Created by UserX on 12/17/2018.
 */

public class ImageResizer {
    public static Drawable resize(Drawable img, int width, int height, Context ctx) {
        Bitmap b = ((BitmapDrawable)img).getBitmap();
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, 50, 50, false);
        return new BitmapDrawable(ctx.getResources(), bitmapResized);
    }
}
