package mobi.anoda.archinamon.kernel.persefone.ui.widget.shareprovider;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;

/**
 * A version of {@link ColorDrawable} that respects bounds.
 */
public class IcsColorDrawable extends Drawable {

    private int mColor;
    private final Paint mfPaint = new Paint();

    public IcsColorDrawable(ColorDrawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        drawable.draw(c);
        this.mColor = bitmap.getPixel(0, 0);
        bitmap.recycle();
    }

    public IcsColorDrawable(int color) {
        this.mColor = color;
    }

    @Implement
    public void draw(Canvas canvas) {
        if ((mColor >>> 24) != 0) {
            mfPaint.setColor(mColor);
            canvas.drawRect(getBounds(), mfPaint);
        }
    }

    @Implement
    public void setAlpha(int alpha) {
        if (alpha != (mColor >>> 24)) {
            mColor = (mColor & 0x00FFFFFF) | (alpha << 24);
            invalidateSelf();
        }
    }

    @Implement
    public void setColorFilter(ColorFilter colorFilter) {
        //Ignored
    }

    @Implement
    public int getOpacity() {
        return mColor >>> 24;
    }
}
